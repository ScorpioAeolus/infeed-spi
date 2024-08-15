package com.infeed.spi.core.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.infeed.spi.annotation.BizSpi;
import com.infeed.spi.annotation.SpiFunctionPoint;
import com.infeed.spi.api.ISpiProvider;
import com.infeed.spi.api.SpiConfig;
import com.infeed.spi.common.CollectionUtil;
import com.infeed.spi.common.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SPI容器的默认实现
 *
 * @author typhoon
 * @date 2024-08-15 19:54 Thursday
 */
@Slf4j
@SuppressWarnings("rawtypes")
public class DefaultSpiContainer implements ISpiContainer {

    // SPI容器，应用启动时初始化容器
    private static final Map<Class<? extends ISpiProvider>, Map<String, ISpiProvider>> container = Maps.newHashMap();

    // 维护spi定义名称和class的映射关系
    private static final Map<String, Class<? extends ISpiProvider>> spiBeanInterfaceMap = Maps.newHashMap();

    // 外部导入执行的标识
    private static final ThreadLocal<Map<Class<? extends ISpiProvider>, Boolean>> spiConfigExternalMapTL = ThreadLocal.withInitial(Maps::newHashMap);

    // 外部导入执行的配置
    private static final ThreadLocal<Map<Class<? extends ISpiProvider>, List<SpiConfig>>> spiConfigListMapTL = ThreadLocal.withInitial(Maps::newHashMap);

    private DefaultSpiContainer() {
    }

    public static class DefaultSpiContainerInstance {
        static final DefaultSpiContainer INSTANCE = new DefaultSpiContainer();
    }

    public static DefaultSpiContainer getInstance() {
        return DefaultSpiContainerInstance.INSTANCE;
    }

    @Override
    public Map<Class<? extends ISpiProvider>, Map<String, ISpiProvider>> getAllSpiImpl() {
        return Collections.unmodifiableMap(container);
    }

    @Override
    public Map<String, ? extends ISpiProvider> lookup(Class<? extends ISpiProvider> spiClass) {
        return container.get(spiClass);
    }

    @Override
    public Class<? extends ISpiProvider> getSpiClassByName(String spiClassName) {
        return spiBeanInterfaceMap.get(spiClassName);
    }

    @Override
    public void setCurrentSpiConfigList(Class<? extends ISpiProvider> spiClass, List<SpiConfig> spiConfigList) {
        spiConfigExternalMapTL.get().put(spiClass, Boolean.TRUE);
        spiConfigListMapTL.get().put(spiClass, Collections.unmodifiableList(spiConfigList));
    }

    @Override
    public void clearCurrentSpiConfigList(Class<? extends ISpiProvider> spiClass) {
        spiConfigExternalMapTL.get().remove(spiClass);
        spiConfigListMapTL.get().remove(spiClass);
    }

    @Override
    public List<SpiConfig> currentSpiConfigList(Class<? extends ISpiProvider> spiClass) {
        if (Objects.equals(spiConfigExternalMapTL.get().get(spiClass), Boolean.TRUE)) {
            // 加载外部导入的配置
            List<SpiConfig> spiConfigList = spiConfigListMapTL.get().get(spiClass);
            if (CollectionUtil.isEmpty(spiConfigList)) {
                return Collections.emptyList();
            }
            return Lists.newArrayList(spiConfigList);
        } else {
            // 加载容器中所有可以执行的配置
            Map<String, ? extends ISpiProvider> spiImplMap = lookup(spiClass);
            if (MapUtil.isEmpty(spiImplMap)) {
                return Collections.emptyList();
            }
            return spiImplMap.values().stream().map(ISpiProvider::config).collect(Collectors.toList());
        }
    }

    @Override
    public void addToContainer(Class<? extends ISpiProvider> spiClass, ISpiProvider spiImpl) {
        synchronized (container) {
            Map<String, ISpiProvider> spiImplInstances = getRegisteredSpiImplInstances(spiClass);

            //支持将spi实现类的代理注入spi容器
            BizSpi bizSpiAnn;
            if (AopUtils.isAopProxy(spiImpl)) {
                bizSpiAnn = AopUtils.getTargetClass(spiImpl).getAnnotation(BizSpi.class);
            } else {
                bizSpiAnn = spiImpl.getClass().getAnnotation(BizSpi.class);
            }

            if (bizSpiAnn == null) {
                return;
            }

            String spiName = bizSpiAnn.name();

            if (spiImplInstances.get(spiName) != null) {
                return;
            }

            spiImplInstances.put(spiName, spiImpl);

            Map<String, ISpiProvider> unmodifiableInstances = Collections.unmodifiableMap(spiImplInstances);

            container.put(spiClass, unmodifiableInstances);

            loggerSpiRegister(spiClass, spiName, spiImpl);

            SpiFunctionPoint annotation = spiClass.getAnnotation(SpiFunctionPoint.class);
            spiBeanInterfaceMap.put(annotation.name(), spiClass);
        }
    }

    private Map<String, ISpiProvider> getRegisteredSpiImplInstances(Class<? extends ISpiProvider> spiClass) {
        if (container.get(spiClass) == null) {
            return Maps.newHashMap();
        }
        return Maps.newHashMap(container.get(spiClass));
    }

    private void loggerSpiRegister(Class<? extends ISpiProvider> spiClass, String spiName, ISpiProvider spiImpl) {
        log.info(">>> register ISpiProvider: spi interface: [{}], spi name: {}, spi impl: [{}]",
                spiClass.getName(), spiName, spiImpl.getClass().getName());
    }
}
