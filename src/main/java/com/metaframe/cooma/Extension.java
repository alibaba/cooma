package com.metaframe.cooma;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a interface as an extension interface.
 * 
 * @author Jerry Lee(oldratlee<at>gmail<dot>com)
 * @since 0.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Extension {

    /**
     * the default extension name.
     */
    String value() default "";

}
