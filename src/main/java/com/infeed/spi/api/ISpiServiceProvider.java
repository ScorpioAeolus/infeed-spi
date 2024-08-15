package com.infeed.spi.api;

import java.util.List;

/**
 * ISpiServiceProvider接口定义
 *
 * @author typhoon
 * @date 2024-08-15 20:14 Thursday
 */
public interface ISpiServiceProvider<In, Out> extends ISpiProvider<In, Out> {
    /**
     * spi执行内容
     *
     * @param context spi上下文
     * @return spi执行结果集合
     */
    List<Out> invoke(In context);
}
