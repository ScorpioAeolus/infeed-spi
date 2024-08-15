package com.infeed.spi.core.container;



import com.infeed.spi.api.ISpiProvider;
import com.infeed.spi.api.SpiConfig;

import java.util.List;
import java.util.Map;

/**
 * SPI容器(接口定义)
 *
 * @author typhoon
 * @date 2024-08-15 19:55 Thursday
 */
@SuppressWarnings("rawtypes")
public interface ISpiContainer {

    // 默认的SPI容器的实例
    ISpiContainer DEFAULT_INSTANCE = DefaultSpiContainer.getInstance();

    /**
     * 获取当前SPI容器中的所有SpiBean
     *
     * @return SpiBean的集合
     */
    Map<Class<? extends ISpiProvider>, Map<String, ISpiProvider>> getAllSpiImpl();

    /**
     * 获取某个SpiClass对应SpiBean
     *
     * @param spiClass the spi interface class
     * @return SpiBean的集合
     */
    Map<String, ? extends ISpiProvider> lookup(Class<? extends ISpiProvider> spiClass);

    /**
     * 根据spi定义name 获取对应的class
     * @param spiClassName
     * @return
     */
    Class<? extends ISpiProvider> getSpiClassByName(String spiClassName);

    /**
     * 设置某个SpiClass对应的执行配置信息
     *
     * @param spiClass the spi interface class
     * @param spiConfigList SPI执行配置信息
     */
    void setCurrentSpiConfigList(Class<? extends ISpiProvider> spiClass, List<SpiConfig> spiConfigList);

    /**
     * 清除某个SpiClass对应的执行配置信息
     *
     * @param spiClass the spi interface class
     */
    void clearCurrentSpiConfigList(Class<? extends ISpiProvider> spiClass);

    /**
     * 获取某个SpiClass对应的执行配置信息
     * @param spiClass the spi interface class
     * @return SPI执行配置信息
     */
    List<SpiConfig> currentSpiConfigList(Class<? extends ISpiProvider> spiClass);

    /**
     * 添加SpiBean到SPI容器
     *
     * @param spiClass the spi interface class
     * @param spiImpl  SpiBean
     */
    void addToContainer(Class<? extends ISpiProvider> spiClass, ISpiProvider spiImpl);
}
