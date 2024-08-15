package com.infeed.spi.core.container;

import com.infeed.spi.annotation.SpiFunctionPoint;
import com.infeed.spi.api.ISpiProvider;
import com.infeed.spi.common.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;

import java.util.Collection;

/**
 * SPI注册器
 *
 * @author typhoon
 * @date 2024-08-15 19:53 Thursday
 */
@Slf4j
@SuppressWarnings({"unchecked", "rawtypes"})
public class SpiRegister {

    private ISpiContainer container;

    private Collection<Object> bizSpiBeans;

    public void setContainer(ISpiContainer container) {
        this.container = container;
    }

    public void setBizSpiBeans(Collection<Object> bizSpiBeans) {
        this.bizSpiBeans = bizSpiBeans;
    }

    public void register() {
        if (container == null || CollectionUtil.isEmpty(bizSpiBeans)) {
            return;
        }
        for (Object bizSpiBean : bizSpiBeans) {
            addToContainer(bizSpiBean);
        }
    }

    private void addToContainer(Object bean) {
        if (!(bean instanceof ISpiProvider)) {
            return;
        }

        ISpiProvider bizSpiBean = (ISpiProvider) bean;

        Class<?> clazz = AopUtils.getTargetClass(bizSpiBean);

        Class<? extends ISpiProvider> spiClass = findSpiClassForInterface(clazz);

        if (spiClass == null) {
            throw new UnsupportedOperationException(">>> spiClass is undefined.");
        }

        container.addToContainer(spiClass, bizSpiBean);
    }

    private Class<? extends ISpiProvider> findSpiClassForInterface(Class<?> clazz) {
        for (Class<?> inter : clazz.getInterfaces()) {
            if (inter.getAnnotation(SpiFunctionPoint.class) != null) {
                return (Class<? extends ISpiProvider>) inter;
            }
        }

        if (clazz.getSuperclass() == Object.class) {
            return null;
        }

        return findSpiClassForInterface(clazz.getSuperclass());
    }
}
