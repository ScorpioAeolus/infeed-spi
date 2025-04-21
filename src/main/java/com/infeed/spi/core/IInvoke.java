package com.infeed.spi.core;

import com.infeed.spi.api.ISpiServiceProvider;

import java.lang.reflect.Method;
import java.util.List;

@FunctionalInterface
public interface IInvoke {


    List<?> doInvoke() throws Throwable;
}
