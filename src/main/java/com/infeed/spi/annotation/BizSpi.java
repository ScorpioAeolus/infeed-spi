package com.infeed.spi.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务spi注解，标记一个类为spi接口的实现(能力节点)
 *
 * @author typhoon
 * @date 2024-08-15 19:38 Thursday
 */
@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BizSpi {

    String name();

    String desc() default "";

    /**
     * 组件所有者
     *
     * @return
     */
    String owner() default "";
}
