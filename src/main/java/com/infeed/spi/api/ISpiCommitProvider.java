package com.infeed.spi.api;

import java.util.List;

/**
 * 提交类型spi
 *
 * @author typhoon
 * @date 2024-08-15 20:12 Thursday
 */
public interface ISpiCommitProvider<T, R> extends ISpiProvider<T, R> {

    List<R> commit(T context);

    List<R> rollback(T context);
}
