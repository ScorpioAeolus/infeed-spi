package com.infeed.spi.api;


import com.infeed.spi.common.CollectionUtil;
import com.infeed.spi.core.container.ISpiContainer;

import java.util.Collections;
import java.util.List;

/**
 * SPI执行器
 *
 * @author typhoon
 * @date 2024-08-15 20:10 Thursday
 */
@SuppressWarnings({"rawtypes", "unused"})
public class SpiExecutor {

    private SpiExecutor() {
    }

    @SuppressWarnings("unchecked")
    private static <T extends ISpiProvider> Class<? extends ISpiProvider> clazz(T spiBean) {
        return (Class<? extends ISpiProvider>) spiBean.getClass().getInterfaces()[0];
    }

    public static <In, Out> List<Out> executeInvoke(ISpiServiceProvider<In, Out> spiBean, List<SpiConfig> spiConfigList, In context) {
        if (CollectionUtil.isEmpty(spiConfigList)) {
            return Collections.emptyList();
        }
        Class<? extends ISpiProvider> clazz = clazz(spiBean);
        try {
            // 设置SPI执行配置信息到SPI容器
            ISpiContainer.DEFAULT_INSTANCE.setCurrentSpiConfigList(clazz, spiConfigList);
            // 执行SPI并返回执行结果
            return spiBean.invoke(context);
        } finally {
            // 清除SPI容器中的SPI执行配置信息
            ISpiContainer.DEFAULT_INSTANCE.clearCurrentSpiConfigList(clazz);
        }
    }

    public static <In, Out> List<Out> executeInvoke(ISpiServiceProvider<In, Out> spiBean, In context) {
        Class<? extends ISpiProvider> clazz = clazz(spiBean);
        try {
            // 执行SPI并返回执行结果
            return spiBean.invoke(context);
        } finally {
            // 清除SPI容器中的SPI执行配置信息
            ISpiContainer.DEFAULT_INSTANCE.clearCurrentSpiConfigList(clazz);
        }
    }

    public static <T> T executeInvoke(ISpiChainProvider<T> spiBean, List<SpiConfig> spiConfigList, T context) {
        if (CollectionUtil.isEmpty(spiConfigList)) {
            return context;
        }
        Class<? extends ISpiProvider> clazz = clazz(spiBean);
        try {
            // 设置SPI执行配置信息到SPI容器
            ISpiContainer.DEFAULT_INSTANCE.setCurrentSpiConfigList(clazz, spiConfigList);
            // 执行SPI并返回执行结果
            return spiBean.invoke(context);
        } finally {
            // 清除SPI容器中的SPI执行配置信息
            ISpiContainer.DEFAULT_INSTANCE.clearCurrentSpiConfigList(clazz);
        }
    }

    public static <T> T executeInvoke(ISpiChainProvider<T> spiBean, T context) {
        Class<? extends ISpiProvider> clazz = clazz(spiBean);
        try {
            // 执行SPI并返回执行结果
            return spiBean.invoke(context);
        } finally {
            // 清除SPI容器中的SPI执行配置信息
            ISpiContainer.DEFAULT_INSTANCE.clearCurrentSpiConfigList(clazz);
        }
    }


    public static <In, Out> List<Out> executeCommit(ISpiCommitProvider<In, Out> spiBean, List<SpiConfig> spiConfigList, In context) {
        if (CollectionUtil.isEmpty(spiConfigList)) {
            return Collections.emptyList();
        }
        Class<? extends ISpiProvider> clazz = clazz(spiBean);
        try {
            // 设置SPI执行配置信息到SPI容器
            ISpiContainer.DEFAULT_INSTANCE.setCurrentSpiConfigList(clazz, spiConfigList);
            // 执行SPI并返回执行结果
            return spiBean.commit(context);
        } finally {
            // 清除SPI容器中的SPI执行配置信息
            ISpiContainer.DEFAULT_INSTANCE.clearCurrentSpiConfigList(clazz);
        }
    }

    public static <In, Out> List<Out> executeCommit(ISpiCommitProvider<In, Out> spiBean, In context) {
        Class<? extends ISpiProvider> clazz = clazz(spiBean);

        try {
            // 执行SPI并返回执行结果
            return spiBean.commit(context);
        } finally {
            // 设置SPI执行配置信息到SPI容器
            ISpiContainer.DEFAULT_INSTANCE.clearCurrentSpiConfigList(clazz);
        }
    }

    public static <In, Out> List<Out> executeRollback(ISpiCommitProvider<In, Out> spiBean, List<SpiConfig> spiConfigList, In context) {
        if (CollectionUtil.isEmpty(spiConfigList)) {
            return Collections.emptyList();
        }
        Class<? extends ISpiProvider> clazz = clazz(spiBean);
        try {
            // 设置SPI执行配置信息到SPI容器
            ISpiContainer.DEFAULT_INSTANCE.setCurrentSpiConfigList(clazz, spiConfigList);
            // 执行SPI并返回执行结果
            return spiBean.rollback(context);
        } finally {
            // 清除SPI容器中的SPI执行配置信息
            ISpiContainer.DEFAULT_INSTANCE.clearCurrentSpiConfigList(clazz);
        }
    }
}
