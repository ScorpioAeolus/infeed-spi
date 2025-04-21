package com.infeed.spi.common;

public interface SpiContext {


    /**
     * spi执行的时候是否需要并发执行,默认给false
     *
     * 并且与com.infeed.spi.api.SpiConfig#mutex中的互斥,如果配置中mutex是true,
     * 那么SpiService多个实现中本身最多只有一个满足条件,此时concurrent忽略,
     * 如果mutex是false,那么根据此时concurrent忽略来判断是否需要并发执行.
     *
     * 并且,程序中不强制用户 SpiExecutor.executeInvoke(xxxSpi, context)中的context实现SpiContext接口
     * ,如果不实现,就认为是不需要并发执行,如果实现了,再做后续的concurrent值判断是否并发执行
     *
     *
     * @return
     */
    default boolean concurrent() {
        return false;
    }
}
