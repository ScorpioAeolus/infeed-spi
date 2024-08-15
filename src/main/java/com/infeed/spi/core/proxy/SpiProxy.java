package com.infeed.spi.core.proxy;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.infeed.spi.api.ISpiChainProvider;
import com.infeed.spi.api.ISpiCommitProvider;
import com.infeed.spi.api.ISpiProvider;
import com.infeed.spi.api.ISpiServiceProvider;
import com.infeed.spi.api.SpiConfig;
import com.infeed.spi.common.CollectionUtil;
import com.infeed.spi.common.MapUtil;
import com.infeed.spi.core.container.ISpiContainer;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SPI代理类
 *
 * @author typhoon
 * @date 2024-08-15 19:58 Thursday
 *
 */
@Slf4j
@SuppressWarnings({"all"})
public class SpiProxy<T extends ISpiProvider> implements InvocationHandler {

    private final Class<T> spiClass;

    SpiProxy(Class<T> spiClass) {
        this.spiClass = spiClass;
    }

    Class<T> getSpiClass() {
        return spiClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, ? extends ISpiProvider> spiImplMap = ISpiContainer.DEFAULT_INSTANCE.lookup(spiClass);

        if (MapUtil.isEmpty(spiImplMap)) {
            // TODO log warn: can not find any spi impl for class ${spiClass}
            return Collections.emptyList();
        }

        List<SpiConfig> spiConfigList = ISpiContainer.DEFAULT_INSTANCE.currentSpiConfigList(spiClass);

        if (CollectionUtil.isEmpty(spiConfigList)) {
            return Collections.emptyList();
        }

        Map<String, ? extends ISpiProvider> filterSpiImplMap = filterAndSortSpiImpl(spiImplMap, spiConfigList);

        if (MapUtil.isEmpty(filterSpiImplMap)) {
            return Collections.emptyList();
        }

        Map<String, SpiConfig> spiConfigMap = spiConfigList.stream().collect(Collectors.toMap(SpiConfig::getName, Function.identity(), (a, b) -> a));

        if (ISpiChainProvider.class.isAssignableFrom(spiClass)) {
            return _invokeChain(filterSpiImplMap, spiConfigMap, method, args);
        }
        if (ISpiCommitProvider.class.isAssignableFrom(spiClass)) {
            return _invokeCommit(filterSpiImplMap, spiConfigMap, method, args);
        }
        if (ISpiServiceProvider.class.isAssignableFrom(spiClass)) {
            return _invokeService(filterSpiImplMap, spiConfigMap, method, args);
        }

        // TODO log warn: can not find real spi class invoke proxy function
        return Collections.emptyList();
    }

    private Object _invokeChain(Map<String, ? extends ISpiProvider> spiImplMap, Map<String, SpiConfig> spiConfigMap, Method method, Object[] args) throws Throwable {
        Object currentResult = args == null ? null : args[0];

        for (Map.Entry<String, ? extends ISpiProvider> entry : spiImplMap.entrySet()) {

            String spiName = entry.getKey();

            ISpiChainProvider spiImpl = (ISpiChainProvider) entry.getValue();

            if (args != null && spiImpl.condition(currentResult)) {

                try {
                    currentResult = method.invoke(spiImpl, currentResult);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }

                if (spiConfigMap.get(spiName).isMutex()) {
                    break;
                }
            }
        }

        return currentResult;
    }

    private Object _invokeCommit(Map<String, ? extends ISpiProvider> spiImplMap, Map<String, SpiConfig> spiConfigMap, Method method, Object[] args) throws Throwable {
        List compositeResult = Lists.newArrayList();

        for (Map.Entry<String, ? extends ISpiProvider> entry : spiImplMap.entrySet()) {

            String spiName = entry.getKey();

            ISpiCommitProvider spiImpl = (ISpiCommitProvider) entry.getValue();

            if (args != null && spiImpl.condition(args[0])) {

                List<?> result;

                try {
                    result = (List<?>) method.invoke(spiImpl, args);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }

                if (CollectionUtil.isNotEmpty(result)) {
                    compositeResult.addAll(result);
                }

                if (spiConfigMap.get(spiName).isMutex()) {
                    break;
                }
            }
        }

        return compositeResult;
    }

    private Object _invokeService(Map<String, ? extends ISpiProvider> spiImplMap, Map<String, SpiConfig> spiConfigMap, Method method, Object[] args) throws Throwable {
        List compositeResult = Lists.newArrayList();

        for (Map.Entry<String, ? extends ISpiProvider> entry : spiImplMap.entrySet()) {

            String spiName = entry.getKey();

            ISpiServiceProvider spiImpl = (ISpiServiceProvider) entry.getValue();

            if (args != null && spiImpl.condition(args[0])) {

                List<?> result;

                try {
                    result = (List<?>) method.invoke(spiImpl, args);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }

                if (CollectionUtil.isNotEmpty(result)) {
                    compositeResult.addAll(result);
                }

                if (spiConfigMap.get(spiName).isMutex()) {
                    break;
                }
            }
        }

        return compositeResult;
    }

    private <T extends ISpiProvider> Map<String, T> filterAndSortSpiImpl(Map<String, T> spiImplMap, List<SpiConfig> spiConfigList) {
        Map<String, T> filterSpiImplMap = Maps.newLinkedHashMap();

        spiConfigList.sort(Comparator.comparingInt(SpiConfig::getPriority));

        for (SpiConfig spiConfig : spiConfigList) {

            T spiImpl = spiImplMap.get(spiConfig.getName());

            if (spiImpl == null) {
                continue;
            }

            filterSpiImplMap.put(spiConfig.getName(), spiImpl);
        }

        return filterSpiImplMap;
    }
}
