package com.infeed.spi.spring.scanner;

import com.google.common.collect.Sets;
import com.infeed.spi.annotation.SpiFunctionPoint;
import com.infeed.spi.spring.BeanRegisterUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Set;

/**
 * spi注解扫描器
 *
 * @author typhoon
 * @date 2024-08-15 19:51 Thursday
 */
@Slf4j
public class SpiFunctionPointScanner {

    private ClassLoader classLoader;

    private BeanDefinitionRegistry registry;

    private AdviceMode adviceMode;

    public SpiFunctionPointScanner(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    public SpiFunctionPointScanner(BeanDefinitionRegistry registry, AdviceMode adviceMode) {
        this.registry = registry;
        this.adviceMode = adviceMode;
        if(null == this.adviceMode) {
            this.adviceMode = AdviceMode.PROXY;
        }
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void scan(String... scanPackages) {

        Set<BeanDefinitionHolder> beanDefinitions = doScan(scanPackages);

        BeanRegisterUtil.registerBeanDefinitions(beanDefinitions, registry);

        this.processBeanDefinitions(beanDefinitions);
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefHolders) {
        // 暂时留空，后续可能会有用处
    }

    private Set<BeanDefinitionHolder> doScan(String... scanPackages) {
        if (scanPackages == null || scanPackages.length <= 0) {
            log.warn("biz-spi-scan scan packages is empty.");
            return null;
        }
        log.info("biz-spi-scan scan packages: {}", Arrays.toString(scanPackages));
        Set<Class<?>> spiClasses = scanSpiFunctionPointInterfaces(scanPackages);
        if (CollectionUtils.isEmpty(spiClasses)) {
            logScannedSpiInterfacesResultEmpty(scanPackages);
            return null;
        }
        logScannedSpiInterfacesResult(spiClasses);
        Set<BeanDefinitionHolder> beanDefHolders = Sets.newHashSet();
        for (Class<?> spiClass : spiClasses) {
            beanDefHolders.add(BeanRegisterUtil.createSpiFactoryBeanBeanDefinitionHolder(spiClass,this.adviceMode));
        }
        return beanDefHolders;
    }

    private Set<Class<?>> scanSpiFunctionPointInterfaces(String... scanPackages) {
        AbstractClassCandidateScanner fastClassPathScanner = new FastClassPathScanner();
        fastClassPathScanner.addClassTypeFilter(new ISpiProviderClassTypeFilter());
        fastClassPathScanner.addClassLoader(this.classLoader);
        return fastClassPathScanner.scan(SpiFunctionPoint.class, scanPackages);
    }

    private void logScannedSpiInterfacesResult(Set<Class<?>> spiClasses) {
        for (Class<?> spiClass : spiClasses) {
            log.info("biz-spi-scan scanned annotated with @SpiFunctionPoint: {}", spiClass);
        }
    }

    private void logScannedSpiInterfacesResultEmpty(String... scanPackages) {
        log.warn("biz-spi-scan scan packages {} not any interfaces which annotated with @SpiFunctionPoint present.", Arrays.toString(scanPackages));
    }
}
