package com.infeed.spi.api;

import lombok.Data;

import java.util.List;

/**
 * spi接口执行结果
 *
 * @author typhoon
 * @date 2024-08-15 20:15 Thursday
 */
@Data
public class SpiInvokeResult<T, R> {

    private T context;

    private List<R> nodeResponse;

    public SpiInvokeResult(){}

    public SpiInvokeResult(T context){
        this.context = context;
    }

    public SpiInvokeResult(T context, List<R> nodeResponse){
        this.context = context;
        this.nodeResponse = nodeResponse;
    }
}
