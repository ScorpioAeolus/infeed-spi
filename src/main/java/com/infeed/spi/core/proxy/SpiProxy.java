package com.infeed.spi.core.proxy;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.infeed.spi.api.ISpiChainProvider;
import com.infeed.spi.api.ISpiCommitProvider;
import com.infeed.spi.api.ISpiProvider;
import com.infeed.spi.api.ISpiServiceProvider;
import com.infeed.spi.api.SpiConfig;
import com.infeed.spi.common.CollectionUtil;
import com.infeed.spi.common.ILogInject;
import com.infeed.spi.common.MapUtil;
import com.infeed.spi.common.SpiContext;
import com.infeed.spi.core.IInvoke;
import com.infeed.spi.core.concurrent.CustomExecutorService;
import com.infeed.spi.core.concurrent.ICallbackOperate;
import com.infeed.spi.core.concurrent.IRetrieve;
import com.infeed.spi.core.container.ISpiContainer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SPI代理类
 *
 * @author typhoon
 * @since 2024-08-15 19:58 Thursday
 *
 */
@SuppressWarnings({"all"})
public class SpiProxy<T extends ISpiProvider> implements InvocationHandler, CustomExecutorService, ILogInject {

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
            ILogInject.log.warn("SpiProxy.invoke can not find any spi impl for class:{}",this.spiClass.getSimpleName());
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

        ILogInject.log.warn("SpiProxy.invoke can not find real spi class invoke proxy function for class:{}",this.spiClass.getSimpleName());
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

        Object context = null == args ? null : args[0];
        boolean concurrent = false;
        if(context instanceof SpiContext) {
            concurrent = ((SpiContext) context).concurrent();
        }
        List<IInvoke> invokeList = new ArrayList<>();
        for (Map.Entry<String, ? extends ISpiProvider> entry : spiImplMap.entrySet()) {

            String spiName = entry.getKey();

            ISpiServiceProvider spiImpl = (ISpiServiceProvider) entry.getValue();

            if (args != null && spiImpl.condition(args[0])) {
                invokeList.add(new IInvoke() {
                    @Override
                    public List<?> doInvoke() throws Throwable {
                        try {
                            return (List<?>) method.invoke(spiImpl, args);
                        } catch (InvocationTargetException e) {
                            throw e.getTargetException();
                        }
                    }
                });
                //extracted(method, args, spiImpl, compositeResult);
                if (spiConfigMap.get(spiName).isMutex()) {
                    break;
                }
            }
        }

        //主动设置了并发执行并且满足条件的spi实现有多个,执行并发调用
        if(concurrent && CollectionUtil.sizeOf(invokeList) > 1) {
            CountDownLatch countDown = new CountDownLatch(CollectionUtil.sizeOf(invokeList));
            for (IInvoke iInvoke : invokeList) {
                this.submitWithCallback(new IRetrieve<List<?>>() {
                    @Override
                    public List<?> retrieve() {
                        try {
                            return iInvoke.doInvoke();
                        } catch (Throwable e) {
                            log.error("doInvoke occur error", e);
                            return null;
                        }
                    }
                }, new ICallbackOperate<List<?>, List>() {
                    @Override
                    public void operate(List<?> result, List list) {
                        if (CollectionUtil.isNotEmpty(result)) {
                            list.addAll(result);
                        }
                    }
                },compositeResult,countDown);
            }
            try {
                countDown.await();
            } catch (InterruptedException e) {
                log.error("SpiProxy._invokeService concurrent execute occur error",e);
            }
        } else {
            for (IInvoke iInvoke : invokeList) {
                List<?> result = iInvoke.doInvoke();
                if (CollectionUtil.isNotEmpty(result)) {
                    compositeResult.addAll(result);
                }
            }
        }
        return compositeResult;
    }

    private static void extracted(Method method, Object[] args, ISpiServiceProvider spiImpl, List compositeResult) throws Throwable {
        List<?> result;
        try {
            result = (List<?>) method.invoke(spiImpl, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        if (CollectionUtil.isNotEmpty(result)) {
            compositeResult.addAll(result);
        }
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
