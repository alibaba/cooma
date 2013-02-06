package com.metaframe.cooma;

import com.metaframe.cooma.internal.utils.ConcurrentHashSet;
import com.metaframe.cooma.internal.utils.Holder;
import com.metaframe.cooma.internal.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Load extension.<p>
 * <ul>
 * <li>inject adaptive instance to the attribute of extension, if the attribute is an extension too.
 * <li>wrap the specified extension wrapper.
 * </ul>
 * 
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 * @since 0.1.0
 * 
 * @see Config
 * @see Extension
 * @see Adaptive
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">Service implementation of JDK5</a>
 */
public class ExtensionLoader<T> {
    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*,+\\s*");

    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<Class<?>, ExtensionLoader<?>>();

    /**
     * Factory Method of {@link ExtensionLoader}.
     *
     * @param type Extension type class
     * @param <T> Extension type
     * @return {@link ExtensionLoader} instance.
     * @throws IllegalArgumentException type argument is null;
     *         or type is not a extension since WITHOUT {@link Extension} Annotation.
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if(!withExtensionAnnotation(type)) {
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
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }

        return instance;
    }

    /**
     * 返回缺省的扩展，如果没有设置则返回<code>null</code>。
     */
    public T getDefaultExtension() {
        getExtensionClasses();
        if(null == defaultExtension || defaultExtension.length() == 0) {
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
     * 返回缺省的扩展点名，如果没有设置缺省则返回<code>null</code>。
     */
    public String getDefaultExtensionName() {
        getExtensionClasses();
        return defaultExtension;
    }

    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> clazzes = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<String>(clazzes.keySet()));
    }

    public String getExtensionName(T extensionInstance) {
        return getExtensionName(extensionInstance.getClass());
    }

    public String getExtensionName(Class<?> extensionClass) {
        // FIXME 要先去加载有哪些类！
        return extensionNames.get(extensionClass);
    }

    /**
     * 取得Adaptive实例。
     * 一般情况不要使用这个方法，ExtensionLoader会把关联扩展的Adaptive实例注入好了。
     *
     * @deprecated 推荐使用自动注入关联扩展的Adaptive实例的方式。
     */
    @Deprecated
    public T getAdaptiveExtension() {
        T instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            if(createAdaptiveInstanceError == null) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            instance = createAdaptiveInstance0();
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            throw new IllegalStateException("Can not create adaptive extension " + type +
                                    ", cause: " + t.getMessage(), t);
                        }
                    }
                }
            }
            else {
                throw new IllegalStateException("Can not create adaptive extension " + type +
                        ", cause: " + createAdaptiveInstanceError.getMessage(), createAdaptiveInstanceError);
            }
        }

        return instance;
    }

    private final Class<T> type;
    private final String defaultExtension;

    private final ConcurrentMap<String, Holder<T>> extInstances = new ConcurrentHashMap<String, Holder<T>>();

    private ExtensionLoader(Class<T> type) {
        this.type = type;

        String defaultExt = null;
        final Extension annotation = type.getAnnotation(Extension.class);
        if(annotation != null) {
            String value = annotation.value();
            if(value != null && (value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if(names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if(names.length == 1 && names[0].trim().length() > 0) {
                    defaultExt = names[0].trim();
                }
            }
        }
        defaultExtension = defaultExt;
    }

    private IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name + ", possible causes: ");
        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(StringUtils.toString(entry.getValue()));
        }
        return new IllegalStateException(buf.toString());
    }

    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
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
                    if (pt.isInterface() && withExtensionAnnotation(pt) && getExtensionLoader(pt).getSupportedExtensions().size() > 0) {
                        try {
                            Object adaptive = getExtensionLoader(pt).getAdaptiveExtension();
                            method.invoke(instance, adaptive);
                        } catch (Exception e) {
                            logger.error("fail to inject via method " + method.getName()
                                    + " of interface " + type.getName() + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instance;
    }

    private Class<?> getExtensionClass(String name) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if (name == null)
            throw new IllegalArgumentException("Extension name == null");
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null)
            throw new IllegalStateException("No such extension \"" + name + "\" for " + type.getName() + "!");
        return clazz;
    }

    // ====================================
    // get & create Adaptive Instance
    // ====================================

    private final Holder<T> cachedAdaptiveInstance = new Holder<T>();
    private volatile Throwable createAdaptiveInstanceError;

    private final Holder<T> adaptiveInstanceHolder = new Holder<T>();
    private final Map<Method, Integer> method2ConfigArgIndex = new HashMap<Method, Integer>();
    private final Map<Method, Method> method2ConfigGetter = new HashMap<Method, Method>();

    /**
     * Thread-safe.
     */
    private T createAdaptiveInstance0() {
        if(null != adaptiveInstanceHolder.get()) {
            return adaptiveInstanceHolder.get();
        }

        getExtensionClasses();

        synchronized (adaptiveInstanceHolder) {
            checkAndSetAdaptiveInfo0();

            Object p = Proxy.newProxyInstance(ExtensionLoader.class.getClassLoader(), new Class[]{type}, new InvocationHandler() {
                // FIXME 添加toString方法支持！ #13
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    //                if(method.getDeclaringClass().equals(Object.class)) {
    //
    //                    return
    //                }
                    if(!method2ConfigArgIndex.containsKey(method)) {
                        throw new UnsupportedOperationException("method " + method.getName() + " of interface "
                                + type.getName() + " is not adaptive method!");
                    }

                    int confArgIdx = method2ConfigArgIndex.get(method);
                    Object confArg = args[confArgIdx];
                    Config config;
                    if(method2ConfigGetter.containsKey(method)) {
                        if(confArg == null) {
                            throw new IllegalArgumentException(method.getParameterTypes()[confArgIdx].getName() +
                                    " argument == null");
                        }
                        Method configGetter = method2ConfigGetter.get(method);
                        config = (Config) configGetter.invoke(confArg);
                        if(config == null) {
                            throw new IllegalArgumentException(method.getParameterTypes()[confArgIdx].getName() +
                                    " argument " + configGetter.getName() + "() == null");
                        }
                    }
                    else {
                        if(confArg == null) {
                            throw new IllegalArgumentException("config == null");
                        }
                        config = (Config) confArg;
                    }

                    String[] value = method.getAnnotation(Adaptive.class).value();
                    if(value.length == 0) {
                        // 没有设置Key，则使用“扩展点接口名的点分隔 作为Key
                        value = new String[]{StringUtils.toDotSpiteString(type.getSimpleName())};
                    }

                    String extName = null;
                    for(int i = 0; i < value.length; ++i) {
                        if(!config.contains(value[i])) {
                            if(i == value.length - 1)
                                extName = defaultExtension;
                            continue;
                        }
                        extName = config.get(value[i]);
                        break;
                    }
                    if(extName == null)
                        throw new IllegalStateException("Fail to get extension(" + type.getName() +
                            ") name from config(" + config + ") use keys())");

                    return  method.invoke(ExtensionLoader.this.getExtension(extName), args);
                }
            });

            T adaptive = type.cast(p);

//            try {
//                injectExtension(adaptive);
//            } catch (Exception e) {
//                // FIXME 出错的异常没有记录！
//                throw new IllegalStateException("Can not create adaptive extension " + type + ", cause: " + e.getMessage(), e);
//            }

            adaptiveInstanceHolder.set(adaptive);
            return adaptiveInstanceHolder.get();
        }
    }

    private void checkAndSetAdaptiveInfo0() {
        Method[] methods = type.getMethods();
        boolean hasAdaptiveAnnotation = false;
        for(Method m : methods) {
            if(m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }
        // 接口上没有Adaptive方法，则不需要生成Adaptive类
        if(! hasAdaptiveAnnotation)
            throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");

        // 收集获取Config的信息：Config是哪个参数；或者是，Config在哪个参数的哪个属性上
        for(Method method : methods) {
            Adaptive annotation = method.getAnnotation(Adaptive.class);
            // 如果不Adaptive方法，不需要收集Config信息
            if(annotation == null) continue;

            // 找类型为Configs的参数
            Class<?>[] parameterTypes = method.getParameterTypes();
            for(int i = 0; i < parameterTypes.length; ++i) {
                if(Config.class.isAssignableFrom(parameterTypes[i])) {
                    method2ConfigArgIndex.put(method, i);
                    break;
                }
            }
            if(method2ConfigArgIndex.containsKey(method)) continue;

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

            if(!method2ConfigArgIndex.containsKey(method)) {
                throw new IllegalStateException("fail to create adaptive class for interface " + type.getName()
                        + ": not found config parameter or config attribute in parameters of method " + method.getName());

            }
        }
    }

    // ====================================
    // get & load Extension Class
    // ====================================

    private final Holder<Map<String, Class<?>>> extClassesHolder = new Holder<Map<String,Class<?>>>();

    private volatile Class<?> adaptiveClass = null;

    private Set<Class<?>> wrapperClasses;

    private final ConcurrentMap<Class<?>, String> extensionNames = new ConcurrentHashMap<Class<?>, String>();

    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();

    /**
     * Thread-safe
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

            if(urls == null) { // FIXME throw exception to notify no extension found?
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
                // delete comments
                final int ci = line.indexOf('#');
                if (ci >= 0) line = line.substring(0, ci);
                line = line.trim();
                if (line.length() == 0) continue;

                try {
                    String name = null;
                    int i = line.indexOf('=');
                    if (i > 0) {
                        name = line.substring(0, i).trim();
                        line = line.substring(i + 1).trim();
                    }
                    Class<?> clazz = Class.forName(line, true, classLoader);
                    if (! type.isAssignableFrom(clazz)) {
                        throw new IllegalStateException("Error when load extension class(interface: " +
                                type + ", class line: " + clazz.getName() + "), class "
                                + clazz.getName() + "is not subtype of interface.");
                    }

                    if (clazz.isAnnotationPresent(Adaptive.class)) {
                        if(adaptiveClass == null) {
                            adaptiveClass = clazz;
                        }
                        else if (! adaptiveClass.equals(clazz)) {
                            throw new IllegalStateException("More than 1 adaptive class found: "
                                    + adaptiveClass.getClass().getName()
                                    + ", " + clazz.getClass().getName());
                        }
                    }
                    else {
                        if(hasCopyConstructor(clazz)) {
                            Set<Class<?>> wrappers = wrapperClasses;
                            if (wrappers == null) {
                                wrapperClasses = new ConcurrentHashSet<Class<?>>();
                                wrappers = wrapperClasses;
                            }
                            wrappers.add(clazz);
                        }
                        else {
                            clazz.getConstructor();

                            // 没有配置文件中没有扩展点名，从实现类的Extension注解上读取。
                            if (name == null || name.length() == 0) {
                                name = findAnnotationName(clazz);
                                if(name == null || name.length() == 0) {
                                    throw new IllegalStateException(
                                            "No such extension name for the class " +
                                            clazz.getName() + " in the config " + url);
                                }
                            }

                            String[] nameList = NAME_SEPARATOR.split(name);
                            for (String n : nameList) {
                                if (! extensionNames.containsKey(clazz)) {
                                    extensionNames.put(clazz, n); // FIXME 实现类的扩展点名，只记录了一个！
                                }

                                Class<?> c = extName2Class.get(n);
                                if (c == null) {
                                    extName2Class.put(n, clazz);
                                }
                                else if (c != clazz) {
                                    throw new IllegalStateException("Duplicate extension " +
                                            type.getName() + " name " + n +
                                            " on " + c.getName() + " and " + clazz.getName());
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                    exceptions.put(line, e);
                }
            } // end of while read lines
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", class file: " + url + ") in " + url, t);
        }
        finally {
            if(reader != null) {
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

    private String findAnnotationName(Class<?> clazz) {
        Extension extension = clazz.getAnnotation(Extension.class);
        return extension == null ? null : extension.value().trim();
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        classLoader = ExtensionLoader.class.getClassLoader();
        if(classLoader != null) {
            return classLoader;
        }
        return classLoader;
    }

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(Extension.class);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "<" + type.getName() + ">";
    }

}
