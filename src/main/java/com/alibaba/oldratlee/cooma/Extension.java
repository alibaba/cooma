package com.alibaba.oldratlee.cooma;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 扩展点接口的标识。
 * 
 * @author oldratlee
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Extension {

    /**
     * 缺省扩展点名。
     */
    String value() default "";

}
