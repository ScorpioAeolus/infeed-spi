package com.infeed.spi.core.concurrent;

/**
 * @author typhoon
 * @since 2025-04-10 11:04 Thursday
 **/
@FunctionalInterface
public interface IRetrieve<T> {
    T retrieve();
}
