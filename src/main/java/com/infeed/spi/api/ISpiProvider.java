package com.infeed.spi.api;

/**
 * SpiProvider接口定义
 *
 * @author typhoon
 * @date 2024-08-15 20:13 Thursday
 */
public interface ISpiProvider<In, Out> {

    default SpiConfig config() {
        return SpiConfig.create();
    }

    /**
     * spi执行条件
     *
     * @param context spi上下文
     * @return true:执行spi，false:跳过执行
     */
    boolean condition(In context);
}
