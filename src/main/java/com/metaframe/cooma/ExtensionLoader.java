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
 * @author oldratlee
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

    private final Class<T> type;

    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();
    
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<Map<String,Class<?>>>();
    
	private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();
	
    private volatile Class<?> cachedAdaptiveClass = null;
    
	private final Holder<Object> cachedAdaptiveInstance = new Holder<Object>();
	private volatile Throwable createAdaptiveInstanceError;
	
    private Set<Class<?>> cachedWrapperClasses;
    
    private String cachedDefaultName;
    
    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();
    
    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(Extension.class);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if(!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type(" + type + 
            		") is not extension, because WITHOUT @Extension Annotation!");
        }
        
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    private ExtensionLoader(Class<T> type) {
        this.type = type;
    }
    
    public String getExtensionName(T extensionInstance) {
        return getExtensionName(extensionInstance.getClass());
    }

    public String getExtensionName(Class<?> extensionClass) {
        return cachedNames.get(extensionClass);
    }
    
	@SuppressWarnings("unchecked")
	public T getExtension(String name) {
		if (name == null || name.length() == 0)
		    throw new IllegalArgumentException("Extension name == null");
		Holder<Object> reference = cachedInstances.get(name);
		if (reference == null) {
		    cachedInstances.putIfAbsent(name, new Holder<Object>());
		    reference = cachedInstances.get(name);
		}
		Object instance = reference.get();
		if (instance == null) {
		    synchronized (reference) {
	            instance = reference.get();
	            if (instance == null) {
	                instance = createExtension(name);
	                reference.set(instance);
	            }
	        }
		}
		return (T) instance;
	}
	
	/**
	 * 返回缺省的扩展，如果没有设置则返回<code>null</code>。 
	 */
	public T getDefaultExtension() {
        getExtensionClasses();
	    if(null == cachedDefaultName || cachedDefaultName.length() == 0) {
	        return null;
	    }
	    return getExtension(cachedDefaultName);
	}

	public boolean hasExtension(String name) {
	    if (name == null || name.length() == 0)
	        throw new IllegalArgumentException("Extension name == null");
	    try {
	        return getExtensionClass(name) != null;
	    } catch (Throwable t) {
	        return false;
	    }
	}
    
	public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> clazzes = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<String>(clazzes.keySet()));
    }
	
	/**
	 * 返回缺省的扩展点名，如果没有设置缺省则返回<code>null</code>。 
	 */
	public String getDefaultExtensionName() {
	    getExtensionClasses();
	    return cachedDefaultName;
	}
	

    @SuppressWarnings("unchecked")
    public T getAdaptiveExtension() {
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            if(createAdaptiveInstanceError == null) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            instance = createAdaptiveExtension();
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            rethrowAsRuntime(t, "fail to create adaptive instance: ");
                        }
                    }
                }
            }
            else {
                rethrowAsRuntime(createAdaptiveInstanceError, "fail to create adaptive instance: ");
            }
        }
        
        return (T) instance;
    }

    private static void rethrowAsRuntime(Throwable t, String message) {
        if(t instanceof RuntimeException)
            throw (RuntimeException)t;
        else
            throw new IllegalStateException(message + t.toString(), t);
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
            buf.append(i ++);
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
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
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
	
	private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
	}
	
    private Map<String, Class<?>> loadExtensionClasses() {
        final Extension defaultAnnotation = type.getAnnotation(Extension.class);
        if(defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if(value != null && (value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if(names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if(names.length == 1) cachedDefaultName = names[0];
            }
        }
        
        ClassLoader classLoader = findClassLoader();
        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        String fileName = null;
        try {
            fileName = SERVICES_DIRECTORY + type.getName();
            Enumeration<java.net.URL> urls;
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL url = urls.nextElement();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
                        try {
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                final int ci = line.indexOf('#');
                                if (ci >= 0) line = line.substring(0, ci);
                                line = line.trim();
                                if (line.length() > 0) {
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
                                            if(cachedAdaptiveClass == null) {
                                                cachedAdaptiveClass = clazz;
                                            } else if (! cachedAdaptiveClass.equals(clazz)) {
                                                throw new IllegalStateException("More than 1 adaptive class found: "
                                                        + cachedAdaptiveClass.getClass().getName()
                                                        + ", " + clazz.getClass().getName());
                                            }
                                        } else {
                                            try {
                                                clazz.getConstructor(type);
                                                Set<Class<?>> wrappers = cachedWrapperClasses;
                                                if (wrappers == null) {
                                                    cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
                                                    wrappers = cachedWrapperClasses;
                                                }
                                                wrappers.add(clazz);
                                            } catch (NoSuchMethodException e) {
                                                clazz.getConstructor();
                                                if (name == null || name.length() == 0) {
                                                    name = findAnnotationName(clazz);
                                                    if (name == null || name.length() == 0) {
                                                        if (clazz.getSimpleName().length() > type.getSimpleName().length()
                                                                && clazz.getSimpleName().endsWith(type.getSimpleName())) {
                                                            name = clazz.getSimpleName().substring(0, clazz.getSimpleName().length() - type.getSimpleName().length()).toLowerCase();
                                                        } else {
                                                            throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + url);
                                                        }
                                                    }
                                                }
                                                String[] names = NAME_SEPARATOR.split(name);
                                                for (String n : names) {
                                                    if (! cachedNames.containsKey(clazz)) {
                                                        cachedNames.put(clazz, n);
                                                    }
                                                    Class<?> c = extensionClasses.get(n);
                                                    if (c == null) {
                                                        extensionClasses.put(n, clazz);
                                                    } else if (c != clazz) {
                                                        throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                                                    }
                                                }
                                            }
                                        }
                                    } catch (Throwable t) {
                                        IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + url + ", cause: " + t.getMessage(), t);
                                        exceptions.put(line, e);
                                    }
                                }
                            } // end of while read lines
                        } finally {
                            reader.close();
                        }
                    } catch (Throwable t) {
                        logger.error("Exception when load extension class(interface: " +
                                            type + ", class file: " + url + ") in " + url, t);
                    }
                } // end of while urls
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
        return extensionClasses;
    }
    
    private String findAnnotationName(Class<?> clazz) {
        Extension extension = clazz.getAnnotation(Extension.class);
        return extension == null ? null : extension.value();
    }
    
    @SuppressWarnings("unchecked")
    private T createAdaptiveExtension() {
        try {
            return injectExtension(getAdaptiveExtensionInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extension " + type + ", cause: " + e.getMessage(), e);
        }
    }

    volatile T adaptiveInstance;
    final Map<Method, Integer> method2ConfigArgIndex = new HashMap<Method, Integer>();
    final Map<Method, Method> method2ConfigGetter = new HashMap<Method, Method>();

    private T getAdaptiveExtensionInstance() {
        if(null != adaptiveInstance) {
            return adaptiveInstance;
        }
        getExtensionClasses();

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

        Object p = Proxy.newProxyInstance(ExtensionLoader.class.getClassLoader(), new Class[]{type}, new InvocationHandler() {
            // FIXME 添加toString方法支持！
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
                // 没有设置Key，则使用“扩展点接口名的点分隔 作为Key
                if(value.length == 0) {
                    value = new String[]{getDefaultKeyFromType()};
                }

                String extName = null;
                for(int i = 0; i < value.length; ++i) {
                    if(!config.contains(value[i])) {
                        if(i == value.length - 1)
                            extName = cachedDefaultName;
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
        adaptiveInstance = type.cast(p);
        return adaptiveInstance;
    }

    // 扩展点接口名的点分隔
    private String getDefaultKeyFromType() {
        char[] charArray = type.getSimpleName().toCharArray();
        StringBuilder sb = new StringBuilder(128);
        for (int i = 0; i < charArray.length; i++) {
            if(Character.isUpperCase(charArray[i])) {
                if(i != 0) {
                    sb.append(".");
                }
                sb.append(Character.toLowerCase(charArray[i]));
            }
            else {
                sb.append(charArray[i]);
            }
        }
        return sb.toString();
    }

    private static ClassLoader findClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            return classLoader;
        }
        classLoader = ExtensionLoader.class.getClassLoader();
        return classLoader;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + "[" + type.getName() + "]";
    }
    
}