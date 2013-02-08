package com.metaframe.cooma;

import com.metaframe.cooma.internal.utils.ConcurrentHashSet;
import com.metaframe.cooma.internal.utils.Holder;
import com.metaframe.cooma.internal.utils.StringUtils;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Load extension.<p>
 * <ul>
 * <li>Manage extension instance, instance is singleton.
 * <li>query extension
 * <li>inject adaptive instance to the attribute of extension, if the attribute is an extension too.
 * <li>wrap the specified extension wrapper.
 * </ul>
 *
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 * @see Config
 * @see Extension
 * @see Adaptive
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">Service implementation of JDK5</a>
 * @since 0.1.0
 */
public class ExtensionLoader<T> {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*,+\\s*");
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-z0-9_]+");

    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();

    /**
     * Factory Method of {@link ExtensionLoader}.
     *
     * @param type Extension type class
     * @param <T>  Extension type
     * @return {@link ExtensionLoader} instance.
     * @throws IllegalArgumentException type argument is null;
     *                                  or type is not a extension since WITHOUT {@link Extension} Annotation.
     * @since 0.1.0
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("type(" + type +
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
     * @param name 扩展名。
     * @return 指定名字的扩展实例
     * @throws IllegalArgumentException 参数为<code>null</code>或是空字符串。
     * @since 0.1.0
     */
    public T getExtension(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");

        // 引入的Holder是为了下面用Holder作“细粒度锁”，而不是锁整个extInstances
        Holder<T> holder = extInstances.get(name);
        if (holder == null) {
            extInstances.putIfAbsent(name, new Holder<T>());
            holder = extInstances.get(name);
        }

        T instance = holder.get();
        if (instance == null) {
            synchronized (holder) { // 以holder为锁，减小锁粒度
                instance = holder.get();
                if (instance == null) { // double check
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }

        return instance;
    }

    /**
     * 返回缺省的扩展，如果没有设置则返回<code>null</code>。
     *
     * @since 0.1.0
     */
    public T getDefaultExtension() {
        if (null == defaultExtension || defaultExtension.length() == 0) {
            return null;
        }
        return getExtension(defaultExtension);
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
        try {
            return getExtensionClass(name) != null;
        } catch (Throwable t) {
            return false;
        }
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
     * 返回缺省的扩展点名，如果没有设置缺省则返回<code>null</code>。
     *
     * @since 0.1.0
     */
    public String getDefaultExtensionName() {
        return defaultExtension;
    }

    /**
     * @since 0.1.0
     */
    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> classes = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<String>(classes.keySet()));
    }

    /**
     * @since 0.1.0
     */
    public String getExtensionName(T extensionInstance) {
        return getExtensionName(extensionInstance.getClass());
    }

    /**
     * @since 0.1.0
     */
    public String getExtensionName(Class<?> extensionClass) {
        getExtensionClasses(); // 先保证加载了扩展点类
        return extClass2Name.get(extensionClass);
    }

    /**
     * 取得Adaptive实例。
     * <p/>
     * 一般情况不要使用这个方法，ExtensionLoader会把关联扩展的Adaptive实例注入好了。
     * <p/>
     * Thread-safe.
     *
     * @since 0.1.0
     * @deprecated 推荐使用自动注入关联扩展的Adaptive实例的方式。
     */
    @Deprecated
    public T getAdaptiveExtension() {
        if (createAdaptiveInstanceError == null) {
            try {
                T adaptiveInstance = adaptiveInstanceHolder.get();
                if (adaptiveInstance == null) {
                    synchronized (adaptiveInstanceHolder) {
                        adaptiveInstance = adaptiveInstanceHolder.get();
                        if (null == adaptiveInstance) { // double check
                            adaptiveInstance = createAdaptiveInstance();
                        }
                    }
                }

                return adaptiveInstance;
            } catch (Throwable t) {
                createAdaptiveInstanceError = t;
                throw new IllegalStateException("Fail to create adaptive extension " + type +
                        ", cause: " + t.getMessage(), t);
            }
        } else {
            throw new IllegalStateException("Fail to create adaptive extension " + type +
                    ", cause: " + createAdaptiveInstanceError.getMessage(), createAdaptiveInstanceError);
        }
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
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findExtensionClassLoadException(name);
        }
        try {
            T instance = injectExtension((T) clazz.newInstance());
            Set<Class<?>> wrapperClasses = this.wrapperClasses;
            if (wrapperClasses != null && wrapperClasses.size() > 0) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    private T injectExtension(T instance) {
        try {
            for (Method method : instance.getClass().getMethods()) {
                if (method.getName().startsWith("set")
                        && method.getParameterTypes().length == 1
                        && Modifier.isPublic(method.getModifiers())) {
                    Class<?> pt = method.getParameterTypes()[0];
                    if (pt.isInterface() && withExtensionAnnotation(pt)) {
                        try {
                            Object adaptive = getExtensionLoader(pt).getAdaptiveExtension();
                            method.invoke(instance, adaptive);
                        } catch (Exception e) {
                            logger.error("fail to inject via method " + method.getName()
                                    + " of interface " + type.getName() + ", cause: " + e.getMessage(), e);
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

    private final Holder<T> adaptiveInstanceHolder = new Holder<T>();
    private volatile Throwable createAdaptiveInstanceError;

    private final Map<Method, Integer> method2ConfigArgIndex = new HashMap<Method, Integer>();
    private final Map<Method, Method> method2ConfigGetter = new HashMap<Method, Method>();

    private T createAdaptiveInstance() {
        checkAndSetAdaptiveInfo0();

        Object p = Proxy.newProxyInstance(ExtensionLoader.class.getClassLoader(), new Class[]{type}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                if(method.getDeclaringClass().equals(Object.class)) {
//
//                    return
//                }
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

                String[] value = method.getAnnotation(Adaptive.class).value();
                if (value.length == 0) {
                    // 没有设置Key，则使用“扩展点接口名的点分隔 作为Key
                    value = new String[]{StringUtils.toDotSpiteString(type.getSimpleName())};
                }

                String extName = null;
                for (int i = 0; i < value.length; ++i) {
                    if (!config.contains(value[i])) {
                        if (i == value.length - 1)
                            extName = defaultExtension;
                        continue;
                    }
                    extName = config.get(value[i]);
                    break;
                }
                if (extName == null)
                    throw new IllegalStateException("Fail to get extension(" + type.getName() +
                            ") name from config(" + config + ") use keys())");

                return method.invoke(ExtensionLoader.this.getExtension(extName), args);
            }
        });

        T adaptive = type.cast(p);

        adaptiveInstanceHolder.set(adaptive);
        return adaptiveInstanceHolder.get();
    }

    private void checkAndSetAdaptiveInfo0() {
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

        // 收集获取Config的信息：Config是哪个参数；或者是，Config在哪个参数的哪个属性上
        for (Method method : methods) {
            Adaptive annotation = method.getAnnotation(Adaptive.class);
            // 如果不Adaptive方法，不需要收集Config信息
            if (annotation == null) continue;

            // 找类型为Configs的参数
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; ++i) {
                if (Config.class.isAssignableFrom(parameterTypes[i])) {
                    method2ConfigArgIndex.put(method, i);
                    break;
                }
            }
            if (method2ConfigArgIndex.containsKey(method)) continue;

            // 找到参数的Configs属性
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
                throw new IllegalStateException("fail to create adaptive class for interface " + type.getName()
                        + ": not found config parameter or config attribute in parameters of method " + method.getName());

            }
        }
    }

    // ====================================
    // get & load Extension Class
    // ====================================

    // Holder<Map<ext-name, ext-class>>
    private final Holder<Map<String, Class<?>>> extClassesHolder = new Holder<Map<String, Class<?>>>();
    private final ConcurrentMap<Class<?>, String> extClass2Name = new ConcurrentHashMap<Class<?>, String>();

    private volatile Class<?> adaptiveClass = null;

    private Set<Class<?>> wrapperClasses;

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
                    classes = loadExtensionClasses0();
                    extClassesHolder.set(classes);
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

    private Map<String, Class<?>> loadExtensionClasses0() {
        Map<String, Class<?>> extName2Class = new HashMap<String, Class<?>>();
        String fileName = null;
        try {
            ClassLoader classLoader = getClassLoader();
            fileName = SERVICES_DIRECTORY + type.getName();
            Enumeration<java.net.URL> urls;
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }

            if (urls == null) { // 找到的urls为null，或是没有找到文件，即认为是没有找到扩展点
                return extName2Class;
            }

            while (urls.hasMoreElements()) {
                java.net.URL url = urls.nextElement();
                readExtension0(extName2Class, classLoader, url);
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
        return extName2Class;
    }

    private void readExtension0(Map<String, Class<?>> extName2Class, ClassLoader classLoader, URL url) {
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

                    Class<?> clazz = Class.forName(body, true, classLoader);
                    if (!type.isAssignableFrom(clazz)) {
                        throw new IllegalStateException("Error when load extension class(interface: " +
                                type + ", class line: " + clazz.getName() + "), class "
                                + clazz.getName() + "is not subtype of interface.");
                    }

                    if (clazz.isAnnotationPresent(Adaptive.class)) {
                        if (adaptiveClass == null) {
                            adaptiveClass = clazz;
                        } else if (!adaptiveClass.equals(clazz)) {
                            throw new IllegalStateException("More than 1 adaptive class found: "
                                    + adaptiveClass.getClass().getName()
                                    + ", " + clazz.getClass().getName());
                        }
                    } else {
                        if (hasCopyConstructor(clazz)) {
                            Set<Class<?>> wrappers = wrapperClasses;
                            if (wrappers == null) {
                                wrapperClasses = new ConcurrentHashSet<Class<?>>();
                                wrappers = wrapperClasses;
                            }
                            wrappers.add(clazz);
                        } else {
                            clazz.getConstructor();

                            String[] nameList = NAME_SEPARATOR.split(name);
                            for (String n : nameList) {
                                if (!isValidExtName(n)) {
                                    throw new IllegalStateException("name(" + n +
                                            ") of extension " + type.getName() + "is invalid!");
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
                            ") of config file( " + url + ") for extension(" + type +
                            "), cause: " + t.getMessage(), t);
                    logger.warn("", e);
                    extClassLoadExceptions.put(line, e);
                }
            } // end of while read lines
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", class file: " + url + ") in " + url, t);
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

    private boolean hasCopyConstructor(Class<?> clazz) {
        try {
            clazz.getConstructor(type);
            return true;
        } catch (NoSuchMethodException e) {
            // ignore
        }
        return false;
    }

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
