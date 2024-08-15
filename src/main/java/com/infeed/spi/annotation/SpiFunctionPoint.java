package com.infeed.spi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务SPI功能点标记注解
 *
 * @author typhoon
 * @date 2024-08-15 20:16 Thursday
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpiFunctionPoint {

    /**
     * 注入Bean时推荐使用 @Resource(name = "${spiBeanName}")
     */
    String name() default "";

    String scope() default "logic";

    String desc() default "";

    /**
     * 组件所有者
     *
     * @return
     */
    String owner() default "";
}
