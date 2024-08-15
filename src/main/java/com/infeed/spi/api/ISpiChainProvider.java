package com.infeed.spi.api;

/**
 * SpiChainProvider接口定义
 * <p>
 * SPI调用链服务实现，区别于普通SPI。
 * 实现该接口的SPI服务，执行时会进行链式调用，上一个节点的结果会作为下一个节点的入参，最终返回最后一个节点的结果。
 *
 * @author typhoon
 * @date 2024-08-15 20:11 Thursday
 */
public interface ISpiChainProvider<T> extends ISpiProvider<T, T> {
    /**
     * spi执行内容
     *
     * @param context spi上下文
     * @return spi执行结果集合
     */
    T invoke(T context);
}
