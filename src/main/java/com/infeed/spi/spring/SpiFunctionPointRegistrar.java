package com.infeed.spi.spring;

import com.infeed.spi.annotation.EnableInfSpiEngine;
import com.infeed.spi.spring.scanner.SpiFunctionPointScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * 应用启动时扫描@SpiFunctionPoint标识的类，将其注册成代理Bean
 *
 * @author typhoon
 * @date 2024-08-15 Thursday
 */
@Slf4j
public class SpiFunctionPointRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        Map<String, Object> annotationAttributes =
                annotationMetadata.getAnnotationAttributes(EnableInfSpiEngine.class.getName());

        assert annotationAttributes != null;
        String[] basePackages = (String[]) annotationAttributes.get("scanBasePackages");
        if (basePackages.length == 0) {
            throw new RuntimeException("SPI引擎需要指定扫描路径");
        }
        AdviceMode mode =  (AdviceMode) annotationAttributes.get("mode");
        SpiFunctionPointScanner sfpScanner = new SpiFunctionPointScanner(beanDefinitionRegistry,mode);
        sfpScanner.scan(basePackages);
    }
}
