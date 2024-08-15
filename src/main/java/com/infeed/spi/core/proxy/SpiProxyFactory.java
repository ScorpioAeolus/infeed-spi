package com.infeed.spi.core.proxy;

import com.infeed.spi.api.ISpiProvider;
import org.springframework.cglib.proxy.Enhancer;

import java.lang.reflect.Proxy;

/**
 * SPI代理类工厂实现
 *
 * @author typhoon
 * @date 2024-08-15 20:08 Thursday
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class SpiProxyFactory {

    private static <T extends ISpiProvider> T newInstance(SpiProxy<T> spiProxy) {
        final Class<T> bizSpiClass = spiProxy.getSpiClass();
        return (T) Proxy.newProxyInstance(bizSpiClass.getClassLoader(), new Class[]{bizSpiClass}, spiProxy);
    }

    public static <T extends ISpiProvider> T newInstance(Class<T> spiClass) {
        final SpiProxy<T> spiProxy = new SpiProxy<>(spiClass);
        return newInstance(spiProxy);
    }

    public static <T extends ISpiProvider> T newCglibInstance(Class<T> spiClass) {
        final SpiCglibProxy<T> spiProxy = new SpiCglibProxy<>(spiClass);
        return newCglibInstance(spiProxy);
    }

    private static <T extends ISpiProvider> T newCglibInstance(SpiCglibProxy<T> spiProxy) {
        final Class<T> bizSpiClass = spiProxy.getSpiClass();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(bizSpiClass);
        enhancer.setCallback(spiProxy);
        return bizSpiClass.cast(enhancer.create());
    }
}
