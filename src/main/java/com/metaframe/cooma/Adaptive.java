package com.metaframe.cooma;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The information used by {@link ExtensionLoader} to generate Adaptive Instance.
 *  
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 * @since 0.1.0
 * @see ExtensionLoader
 * @see Config
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Adaptive {

    /**
     * the key names of the {@link Config}, the corresponding value in {@link Config} used as the extension name which adaptive instance adapt to.
     * <br>
     * If these keys have no corresponding value in the {@link Config}, use the default extension(set by the {@link Extension} on the extension interface).
     * <p>
     * eg. <code>String[] {"key1", "key2"}</code>, means:
     * <ol>
     * <li>use the value of <code>key1</code> as the extension name which adaptive instance adapt to;
     * <li>if <code>key1</code> has no corresponding value in the {@link Config}, use the value of <code>key2</code> as the extension name;
     * <li>if <code>key2</code> has no corresponding value in the {@link Config}, use the default extension;
     * <li>if has no default extension, throw {@link IllegalStateException} when inject the adaptive instance.
     * </ol>
     * <p>
     *
     * if default value of key names is the low case of extension interface name, separate word by dot.
     * eg. Extension interface <code>com.metaframe.cooma.FooBizService</code>, the key names is <code>String[] {"foo.biz.service"}</code>
     * 
     * @see Extension#value()
     * @since 0.1.0
     */
    String[] value() default {};
}
