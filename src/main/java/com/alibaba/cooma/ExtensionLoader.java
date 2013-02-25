/*
 * Copyright 2012-2013 Cooma Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cooma;

import com.alibaba.cooma.internal.utils.Holder;
import com.alibaba.cooma.internal.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * 加载和管理扩展。
 * <p/>
 * <ul>
 * <li>管理的扩展实例是<b>单例</b>。
 * <li>Wrapper实例每次获得扩展实例重新创建，并Wrap到扩展实例上。
 * </ul>
 *
 * @author Jerry Lee(oldratlee AT gmail DOT com)
 * @see Extension
 * @see Adaptive
 * @see Config
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">Service implementation of JDK5</a>
 * @since 0.1.0
 */
public class ExtensionLoader<T> {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final String EXTENSION_CONF_DIRECTORY = "META-INF/extensions/";

    private static final String PREFIX_ADAPTIVE_CLASS = "*";
    private static final String PREFIX_WRAPPER_CLASS = "+";

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*,+\\s*");
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");

    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();

    /**
     * {@link ExtensionLoader}的工厂方法。
     *
     * @param type 扩展点接口类型
     * @param <T>  扩展点类型
     * @return {@link ExtensionLoader}实例
     * @throws IllegalArgumentException 参数为<code>null</code>；
     *                                  或是扩展点接口上没有{@link Extension}注解。
     * @since 0.1.0
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type.getName() + ") is not interface!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("type(" + type.getName() +
                    ") is not a extension, because WITHOUT @Extension Annotation!");
        }

        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    /**
     * 获取指定名字的扩展实例。
     *
     * @param name 扩展名
     * @return 指定名字的扩展实例
     * @throws IllegalArgumentException 参数为<code>null</code>或是空字符串。
     * @throws IllegalStateException    指定的扩展名没有对应的扩展点，异常栈中包含可能的原因。
     * @since 0.1.0
     */
    public T getExtension(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");

        // 先一下加载扩展点类，如果没有这个名字的扩展点类，会抛异常，
        // 这样不用创建不必要的Holder。
        getExtensionClass(name);

        // 引入的Holder是为了下面用Holder作“细粒度锁”，而不是锁整个extInstances
        Holder<T> holder = extInstances.get(name);
        if (holder == null) {
            extInstances.putIfAbsent(name, new Holder<T>());
            holder = extInstances.get(name);
        }
        Holder<Throwable> throwableHolder = createExtInstanceErrors.get(name);
        if (throwableHolder == null) {
            createExtInstanceErrors.put(name, new Holder<Throwable>());
            throwableHolder = createExtInstanceErrors.get(name);
        }

        if (throwableHolder.get() != null) {
            throw new IllegalStateException("Fail to get extension " + name +
                    " of extension point " + type.getName() + ", cause: " +
                    throwableHolder.get().getMessage(), throwableHolder.get());
        }
        if (holder.get() == null) {
            synchronized (holder) {
                holder = extInstances.get(name);
                throwableHolder = createExtInstanceErrors.get(name);
                if (throwableHolder.get() != null) { // double check
                    throw new IllegalStateException("Fail to get extension " + name +
                            " of extension point " + type.getName() + ", cause: " +
                            throwableHolder.get().getMessage(), throwableHolder.get());
                }
                if (holder.get() == null) {
                    try {
                        holder.set(createExtension(name));
                    } catch (Throwable t) {
                        throwableHolder.set(t);
                        throw new IllegalStateException("Fail to get extension " + name +
                                " of extension point " + type.getName() + ", cause: " + t.getMessage(), t);
                    }
                }
            }
        }

        return holder.get();
    }

    /**
     * 获取指定名字的扩展实例。
     *
     * @param name     扩展名
     * @param wrappers 返回的实例上，要启用的Wrapper。
     * @return 指定名字的扩展实例
     * @throws IllegalArgumentException 参数为<code>null</code>或是空字符串。
     * @throws IllegalStateException    指定的扩展名没有对应的扩展点，异常栈中包含可能的原因。
     * @since 0.2.0
     */
    public T getExtension(String name, List<String> wrappers) {
        T instance = getExtension(name);
        return createWrapper(instance, wrappers);
    }

    /**
     * 返回缺省的扩展。
     *
     * @throws IllegalStateException 指定的扩展没有设置缺省扩展点
     * @since 0.1.0
     */
    public T getDefaultExtension() {
        if (null == defaultExtension || defaultExtension.length() == 0) {
            throw new IllegalStateException("No default extension on extension " + type.getName());
        }
        return getExtension(defaultExtension);
    }

    /**
     * 返回缺省的扩展。
     *
     * @param wrappers 返回的实例上，要启用的Wrapper。
     * @throws IllegalStateException 指定的扩展没有设置缺省扩展点
     * @since 0.2.1
     */
    public T getDefaultExtension(List<String> wrappers) {
        if (null == defaultExtension || defaultExtension.length() == 0) {
            throw new IllegalStateException("No default extension on extension " + type.getName());
        }
        return getExtension(defaultExtension, wrappers);
    }

    /**
     * 检查是否有指定名字的扩展。
     *
     * @param name 扩展名
     * @return 有指定名字的扩展，则<code>true</code>，否则<code>false</code>。
     * @throws IllegalArgumentException 参数为<code>null</code>或是空字符串。
     * @since 0.1.0
     */
    public boolean hasExtension(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");
        return getExtensionClasses().get(name) != null;
    }

    /**
     * 检查是否有指定缺省的扩展。
     *
     * @return 有指定缺省的扩展，则<code>true</code>，否则<code>false</code>。
     * @since 0.1.0
     */
    public boolean hasDefaultExtension() {
        return !(null == defaultExtension || defaultExtension.length() == 0);

    }

    /**
     * 获取扩展点实现的所有扩展点名。
     *
     * @since 0.1.0
     */
    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> classes = getExtensionClasses();
        return Collections.unmodifiableSet(new HashSet<String>(classes.keySet()));
    }

    /**
     * 返回缺省的扩展点名，如果没有设置缺省则返回<code>null</code>。
     *
     * @since 0.1.0
     */
    public String getDefaultExtensionName() {
        return defaultExtension;
    }

    /**
     * 取得Adaptive实例。
     * <p/>
     * 一般情况不要使用这个方法，ExtensionLoader会把关联扩展的Adaptive实例注入好了。<br />
     * 推荐使用自动注入关联扩展的Adaptive实例的方式。
     * <p/>
     * Thread-safe.
     *
     * @since 0.1.0
     */
    public T getAdaptiveInstance() {
        getExtensionClasses(); // 加载扩展点，保证会发现手写的AdaptiveClass

        Throwable createError = createAdaptiveInstanceError.get();
        T adaptiveInstance = this.adaptiveInstance.get();
        if (null != createError) {
            throw new IllegalStateException("Fail to create adaptive extension for extension point " +
                    type.getName() + ", cause: " + createError.getMessage(), createError);
        }
        if (null != adaptiveInstance) {
            return adaptiveInstance;
        }

        synchronized (this.adaptiveInstance) {
            createError = createAdaptiveInstanceError.get();
            adaptiveInstance = this.adaptiveInstance.get();
            if (null != createError) { // double check
                throw new IllegalStateException("Fail to create adaptive extension for extension point " +
                        type.getName() + ", cause: " + createError.getMessage(), createError);
            }
            if (null != adaptiveInstance) {
                return adaptiveInstance;
            }

            try {
                this.adaptiveInstance.set(createAdaptiveInstance());
                return this.adaptiveInstance.get();
            } catch (Throwable t) {
                createAdaptiveInstanceError.set(t);
                throw new IllegalStateException("Fail to create adaptive extension for extension point " +
                        type.getName() + ", cause: " + t.getMessage(), t);
            }
        }
    }

    /**
     * 取得Adaptive实例。
     * <p/>
     * 一般情况不要使用这个方法，ExtensionLoader会把关联扩展的Adaptive实例注入好了。<br />
     * 推荐使用自动注入关联扩展的Adaptive实例的方式。
     *
     * @param wrappers 返回的实例上，要启用的Wrapper。
     * @since 0.2.1
     */
    public T getAdaptiveInstance(List<String> wrappers) {
        T instance = getAdaptiveInstance();
        return createWrapper(instance, wrappers);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "<" + type.getName() + ">";
    }

    // ==============================
    // internal methods
    // ==============================

    private final Class<T> type;
    private final String defaultExtension;

    private final ConcurrentMap<String, Holder<T>> extInstances = new ConcurrentHashMap<String, Holder<T>>();
    private final ConcurrentMap<String, Holder<Throwable>> createExtInstanceErrors = new ConcurrentHashMap<String, Holder<Throwable>>();

    private ExtensionLoader(Class<T> type) {
        this.type = type;

        String defaultExt = null;
        final Extension annotation = type.getAnnotation(Extension.class);
        if (annotation != null) {
            String value = annotation.value();
            if (value != null && (value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " +
                            type.getName() + ": " + Arrays.toString(names));
                }
                if (names.length == 1 && names[0].trim().length() > 0) {
                    defaultExt = names[0].trim();
                }
                if (!isValidExtName(defaultExt)) {
                    throw new IllegalStateException("default name(" + defaultExt +
                            ") of extension " + type.getName() + " is invalid!");
                }
            }
        }
        defaultExtension = defaultExt;
    }

    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClass(name);
        try {
            return injectExtension((T) clazz.newInstance());
        } catch (Throwable t) {
            throw new IllegalStateException("Fail to create extension " + name +
                    " of extension point " + type.getName() + ", cause: " + t.getMessage(), t);
        }
    }

    private T createWrapper(T instance, List<String> wrappers) {
        for (String name : wrappers) {
            try {
                instance = injectExtension(name2Wrapper.get(name).getConstructor(type).newInstance(instance));
            } catch (Throwable e) {
                throw new IllegalStateException("Fail to create wrapper(" + name + ") for extension point " + type);
            }
        }

        return instance;
    }

    private T injectExtension(T instance) {
        try {
            for (Method method : instance.getClass().getMethods()) {
                if (method.getName().startsWith("set")
                        && method.getParameterTypes().length == 1
                        && Modifier.isPublic(method.getModifiers())) {
                    Class<?> pt = method.getParameterTypes()[0];
                    if (pt.isInterface() && withExtensionAnnotation(pt)) {
                        if (pt.equals(type)) { // avoid obvious dead loop TODO avoid complex nested loop setting?
                            logger.warn("Ignore self set(" + method + ") for class(" +
                                    instance.getClass() + ") when inject.");
                            continue;
                        }

                        try {
                            Object adaptive = getExtensionLoader(pt).getAdaptiveInstance();
                            method.invoke(instance, adaptive);
                        } catch (Exception e) {
                            logger.error("Fail to inject via method " + method.getName()
                                    + " of interface to extension implementation " + instance.getClass() +
                                    " for extension point " + type.getName() + ", cause: " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instance;
    }

    // ====================================
    // get & create Adaptive Instance
    // ====================================

    private final Holder<T> adaptiveInstance = new Holder<T>();
    private volatile Holder<Throwable> createAdaptiveInstanceError = new Holder<Throwable>();

    private final Map<Method, Integer> method2ConfigArgIndex = new HashMap<Method, Integer>();
    private final Map<Method, Method> method2ConfigGetter = new HashMap<Method, Method>();
    private final Map<Method, String[]> method2AdaptiveKeys = new HashMap<Method, String[]>();

    private T createAdaptiveInstance() throws IllegalAccessException, InstantiationException {
        checkAndCollectAdaptiveInfo0();

        if (adaptiveClass != null) {
            return type.cast(adaptiveClass.newInstance());
        }

        Object p = Proxy.newProxyInstance(ExtensionLoader.class.getClassLoader(), new Class[]{type}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass().equals(Object.class)) {
                    String methodName = method.getName();
                    if (methodName.equals("toString")) {
                        return "Adaptive Instance for " + type.getName();
                    }
                    if (methodName.equals("hashCode")) {
                        return 1;
                    }
                    throw new UnsupportedOperationException("not support method " + method +
                            " of Adaptive Instance for " + type.getName());
                }

                if (!method2ConfigArgIndex.containsKey(method)) {
                    throw new UnsupportedOperationException("method " + method.getName() + " of interface "
                            + type.getName() + " is not adaptive method!");
                }

                int confArgIdx = method2ConfigArgIndex.get(method);
                Object confArg = args[confArgIdx];
                Config config;
                if (method2ConfigGetter.containsKey(method)) {
                    if (confArg == null) {
                        throw new IllegalArgumentException(method.getParameterTypes()[confArgIdx].getName() +
                                " argument == null");
                    }
                    Method configGetter = method2ConfigGetter.get(method);
                    config = (Config) configGetter.invoke(confArg);
                    if (config == null) {
                        throw new IllegalArgumentException(method.getParameterTypes()[confArgIdx].getName() +
                                " argument " + configGetter.getName() + "() == null");
                    }
                } else {
                    if (confArg == null) {
                        throw new IllegalArgumentException("config == null");
                    }
                    config = (Config) confArg;
                }

                String[] adaptiveKeys = method2AdaptiveKeys.get(method);
                String extName = null;
                for (int i = 0; i < adaptiveKeys.length; ++i) {
                    if (!config.contains(adaptiveKeys[i])) {
                        if (i == adaptiveKeys.length - 1)
                            extName = defaultExtension;
                        continue;
                    }
                    extName = config.get(adaptiveKeys[i]);
                    break;
                }
                if (extName == null)
                    throw new IllegalStateException("Fail to get extension(" + type.getName() +
                            ") name from config(" + config + ") use keys(" + Arrays.toString(adaptiveKeys) + ")");

                return method.invoke(ExtensionLoader.this.getExtension(extName), args);
            }
        });

        return type.cast(p);
    }

    /**
     * 收到Adaptive Instance需要的信息。
     * 1. Configs在方法的参数的位置
     * 2. Method的Adaptive Keys
     */
    private void checkAndCollectAdaptiveInfo0() {
        Method[] methods = type.getMethods();
        boolean hasAdaptiveAnnotation = false;
        for (Method m : methods) {
            if (m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }
        // 接口上没有Adaptive方法，则不需要生成Adaptive类
        if (!hasAdaptiveAnnotation)
            throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");

        // 收集获取Config的信息
        for (Method method : methods) {
            Adaptive annotation = method.getAnnotation(Adaptive.class);
            // 如果不Adaptive方法，不需要收集Config信息
            if (annotation == null) continue;

            // 1. 找 Configs类型的参数
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; ++i) {
                if (Config.class.isAssignableFrom(parameterTypes[i])) {
                    method2ConfigArgIndex.put(method, i);
                    break;
                }
            }
            if (!method2ConfigArgIndex.containsKey(method)) {
                // 2. 找 有属性是Configs类型的参数
                LBL_PARAMETER_TYPES:
                for (int i = 0; i < parameterTypes.length; ++i) {
                    Method[] ms = parameterTypes[i].getMethods();
                    for (Method m : ms) {
                        String name = m.getName();
                        if ((name.startsWith("get") || name.length() > 3)
                                && Modifier.isPublic(m.getModifiers())
                                && !Modifier.isStatic(m.getModifiers())
                                && m.getParameterTypes().length == 0
                                && Config.class.isAssignableFrom(m.getReturnType())) {
                            method2ConfigArgIndex.put(method, i);
                            method2ConfigGetter.put(method, m);
                            break LBL_PARAMETER_TYPES;
                        }
                    }
                }

                if (!method2ConfigArgIndex.containsKey(method)) {
                    throw new IllegalStateException("Fail to create adaptive class for extension point " + type.getName() +
                            ", since not found config parameter or config attribute in parameters for adaptive method " + method.getName());

                }
            }

            // 3. 收集方法上的Adaptive Keys
            String[] adaptiveKeys = method.getAnnotation(Adaptive.class).value();
            if (adaptiveKeys.length == 0) {
                // 没有设置Key，则使用“扩展点接口名的点分隔 作为Key
                adaptiveKeys = new String[]{StringUtils.toDotSpiteString(type.getSimpleName())};
            }
            method2AdaptiveKeys.put(method, adaptiveKeys);
        }
    }

    // ====================================
    // get & load Extension Class
    // ====================================

    // Holder<Map<ext-name, ext-class>>
    private final Holder<Map<String, Class<?>>> extClassesHolder = new Holder<Map<String, Class<?>>>();
    private final ConcurrentMap<Class<?>, String> extClass2Name = new ConcurrentHashMap<Class<?>, String>();

    private volatile Class<?> adaptiveClass = null;

    private volatile Map<String, Class<? extends T>> name2Wrapper;

    private Map<String, IllegalStateException> extClassLoadExceptions = new ConcurrentHashMap<String, IllegalStateException>();

    private Class<?> getExtensionClass(String name) {
        if (name == null)
            throw new IllegalArgumentException("Extension name == null");

        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null)
            throw findExtensionClassLoadException(name);
        return clazz;
    }

    /**
     * Thread-safe.
     */
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = extClassesHolder.get();
        if (classes == null) {
            synchronized (extClassesHolder) {
                classes = extClassesHolder.get();
                if (classes == null) { // double check
                    loadExtensionClasses0();
                    classes = extClassesHolder.get();
                }
            }
        }
        return classes;
    }

    private IllegalStateException findExtensionClassLoadException(String name) {
        String msg = "No such extension " + type.getName() + " by name " + name;

        for (Map.Entry<String, IllegalStateException> entry : extClassLoadExceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                IllegalStateException e = entry.getValue();
                return new IllegalStateException(msg + ", cause: " + e.getMessage(), e);
            }
        }

        StringBuilder buf = new StringBuilder(msg);
        if (!extClassLoadExceptions.isEmpty()) {
            buf.append(", possible causes: ");
            int i = 1;
            for (Map.Entry<String, IllegalStateException> entry : extClassLoadExceptions.entrySet()) {
                buf.append("\r\n(");
                buf.append(i++);
                buf.append(") ");
                buf.append(entry.getKey());
                buf.append(":\r\n");
                buf.append(StringUtils.toString(entry.getValue()));
            }
        }
        return new IllegalStateException(buf.toString());
    }

    private void loadExtensionClasses0() {
        Map<String, Class<?>> extName2Class = new HashMap<String, Class<?>>();
        Map<String, Class<? extends T>> tmpName2Wrapper = new LinkedHashMap<String, Class<? extends T>>();
        String fileName = null;
        try {
            ClassLoader classLoader = getClassLoader();
            fileName = EXTENSION_CONF_DIRECTORY + type.getName();
            Enumeration<java.net.URL> urls;
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }

            if (urls != null) { // 找到的urls为null，或是没有找到文件，即认为是没有找到扩展点
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    readExtension0(extName2Class, tmpName2Wrapper, classLoader, url);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension point(interface: " +
                    type.getName() + ", description file: " + fileName + ").", t);
        }

        extClassesHolder.set(extName2Class);
        name2Wrapper = tmpName2Wrapper;
    }

    private void readExtension0(Map<String, Class<?>> extName2Class, Map<String, Class<? extends T>> name2Wrapper, ClassLoader classLoader, URL url) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                String config = line;

                // delete comments
                final int ci = config.indexOf('#');
                if (ci >= 0) config = config.substring(0, ci);
                config = config.trim();
                if (config.length() == 0) continue;

                try {
                    String name = null;
                    String body = null;
                    int i = config.indexOf('=');
                    if (i > 0) {
                        name = config.substring(0, i).trim();
                        body = config.substring(i + 1).trim();
                    }
                    // 没有配置文件中没有扩展点名，从实现类的Extension注解上读取。
                    if (name == null || name.length() == 0) {
                        throw new IllegalStateException(
                                "missing extension name, config value: " + config);
                    }

                    Class<? extends T> clazz = Class.forName(body, true, classLoader).asSubclass(type);
                    if (!type.isAssignableFrom(clazz)) {
                        throw new IllegalStateException("Error when load extension class(interface: " +
                                type.getName() + ", class line: " + clazz.getName() + "), class "
                                + clazz.getName() + "is not subtype of interface.");
                    }

                    if (name.startsWith(PREFIX_ADAPTIVE_CLASS)) {
                        if (adaptiveClass == null) {
                            adaptiveClass = clazz;
                        } else if (!adaptiveClass.equals(clazz)) {
                            throw new IllegalStateException("More than 1 adaptive class found: "
                                    + adaptiveClass.getClass().getName()
                                    + ", " + clazz.getClass().getName());
                        }
                    } else {
                        final boolean isWrapper = name.startsWith(PREFIX_WRAPPER_CLASS);
                        if (isWrapper)
                            name = name.substring(PREFIX_WRAPPER_CLASS.length());

                        String[] nameList = NAME_SEPARATOR.split(name);
                        for (String n : nameList) {
                            if (!isValidExtName(n)) {
                                throw new IllegalStateException("name(" + n +
                                        ") of extension " + type.getName() + "is invalid!");
                            }

                            if (isWrapper) {
                                try {
                                    clazz.getConstructor(type);
                                    name2Wrapper.put(name, clazz);
                                } catch (NoSuchMethodException e) {
                                    throw new IllegalStateException("wrapper class(" + clazz +
                                            ") has NO copy constructor!", e);
                                }
                            } else {
                                try {
                                    clazz.getConstructor();
                                } catch (NoSuchMethodException e) {
                                    throw new IllegalStateException("extension class(" + clazz +
                                            ") has NO default constructor!", e);
                                }
                                if (extName2Class.containsKey(n)) {
                                    if (extName2Class.get(n) != clazz) {
                                        throw new IllegalStateException("Duplicate extension " +
                                                type.getName() + " name " + n +
                                                " on " + clazz.getName() + " and " + clazz.getName());
                                    }
                                } else {
                                    extName2Class.put(n, clazz);
                                }

                                if (!extClass2Name.containsKey(clazz)) {
                                    extClass2Name.put(clazz, n); // 实现类到扩展点名的Map中，记录了一个就可以了
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    IllegalStateException e = new IllegalStateException("Failed to load config line(" + line +
                            ") of config file(" + url + ") for extension(" + type.getName() +
                            "), cause: " + t.getMessage(), t);
                    logger.warn("", e);
                    extClassLoadExceptions.put(line, e);
                }
            } // end of while read lines
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type.getName() + ", class file: " + url + ") in " + url, t);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable t) {
                    // ignore
                }
            }
        }
    }

    // =========================
    // small helper methods
    // =========================

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        classLoader = ExtensionLoader.class.getClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        return classLoader;
    }

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(Extension.class);
    }

    private static boolean isValidExtName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }
}
