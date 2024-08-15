package com.infeed.spi.annotation;

import com.infeed.spi.spring.BizSpiRegistrar;
import com.infeed.spi.spring.SpiFunctionPointRegistrar;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SPI引擎启动注解
 *
 * @author typhoon
 * @date 2024-08-15 19:49 Thursday
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SpiFunctionPointRegistrar.class, BizSpiRegistrar.class})
public @interface EnableInfSpiEngine {

    /**
     * SPI扫描路径
     *
     * @return
     */
    String[] scanBasePackages();

    /**
     * 生成代理模式;默认jdk动态代理
     * @return
     */
    AdviceMode mode() default AdviceMode.PROXY;
}
