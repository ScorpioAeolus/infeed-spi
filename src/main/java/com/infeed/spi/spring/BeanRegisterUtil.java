package com.infeed.spi.spring;

import com.infeed.spi.annotation.SpiFunctionPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.util.Set;

/**
 * Bean注册工具类
 *
 * @author typhoon
 * @date 2024-08-15 20:06 Thursday
 */
@Slf4j
public class BeanRegisterUtil {

    public static void registerBeanDefinitions(Set<BeanDefinitionHolder> beanDefHolders, BeanDefinitionRegistry registry) {
        if (CollectionUtils.isEmpty(beanDefHolders)) {
            return;
        }
        for (BeanDefinitionHolder beanDefHolder : beanDefHolders) {
            BeanDefinitionReaderUtils.registerBeanDefinition(beanDefHolder, registry);
            log.info("biz-spi-bean registered. bean name: {}, bean class: {}, spi interface: {}.",
                    beanDefHolder.getBeanName(),
                    beanDefHolder.getBeanDefinition().getBeanClassName(),
                    beanDefHolder.getBeanDefinition().getConstructorArgumentValues().getGenericArgumentValues().get(0).getValue());
        }
    }

    public static BeanDefinitionHolder createSpiFactoryBeanBeanDefinitionHolder(Class<?> spiClass, AdviceMode adviceMode) {
        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        beanDef.setBeanClass(SpiFactoryBean.class);
        beanDef.getConstructorArgumentValues().addGenericArgumentValue(spiClass);
        beanDef.getConstructorArgumentValues().addGenericArgumentValue(adviceMode);
        String beanName = generateSpiFactoryBeanName(spiClass);
        log.info("biz-spi-bean created. bean name: {}, bean class: {}, spi interface: {}.",
                beanName, SpiFactoryBean.class.getName(), spiClass.getName());
        return new BeanDefinitionHolder(beanDef, beanName);
    }

    private static String generateSpiFactoryBeanName(Class<?> spiClass) {
        SpiFunctionPoint sfpAnn = spiClass.getAnnotation(SpiFunctionPoint.class);
        if (sfpAnn != null && StringUtils.hasText(sfpAnn.name())) {
            return sfpAnn.name();
        }
        return BeanRegisterUtil.generateDefaultBeanName(spiClass);
    }

    private static String generateDefaultBeanName(Class<?> spiClass) {
        final String shortClassName = ClassUtils.getShortName(spiClass);
        return Introspector.decapitalize(shortClassName);
    }

}
