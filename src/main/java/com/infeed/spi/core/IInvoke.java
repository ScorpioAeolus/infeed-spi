package com.infeed.spi.core;

import java.util.List;

@FunctionalInterface
public interface IInvoke {


    List<?> doInvoke() throws Throwable;
}
