package com.infeed.spi.spring;

import com.infeed.spi.api.ISpiProvider;
import com.infeed.spi.core.proxy.SpiProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.AdviceMode;

/**
 * spi工厂bean
 *
 * <ul>
 *     <li>spi接口注入的时候通过FactoryBean的getObject返回</li>
 * </ul>
 *
 * @author typhoon
 * @date 2024-08-15 20:06 Thursday
 */
@SuppressWarnings({"rawtypes", "unused"})
public class SpiFactoryBean<T extends ISpiProvider> implements FactoryBean<T>, InitializingBean {

    private Class<T> spiClass;

    private AdviceMode adviceMode;

    private SpiFactoryBean() {}

    public SpiFactoryBean(Class<T> spiClass,AdviceMode adviceMode) {
        this.spiClass = spiClass;
        this.adviceMode = adviceMode;
    }

    @Override
    public T getObject() throws Exception {
        if(AdviceMode.ASPECTJ == this.adviceMode) {
            return SpiProxyFactory.newCglibInstance(spiClass);
        }
        return SpiProxyFactory.newInstance(spiClass);
    }

    @Override
    public Class<?> getObjectType() {
        return spiClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
