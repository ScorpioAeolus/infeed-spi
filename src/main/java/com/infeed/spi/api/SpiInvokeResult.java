package com.infeed.spi.api;


import java.util.List;

/**
 * spi接口执行结果
 *
 * @author typhoon
 * @since 2024-08-15 20:15 Thursday
 */
public class SpiInvokeResult<T, R> {

    private T context;

    private List<R> nodeResponse;

    public T getContext() {
        return context;
    }

    public void setContext(T context) {
        this.context = context;
    }

    public List<R> getNodeResponse() {
        return nodeResponse;
    }

    public void setNodeResponse(List<R> nodeResponse) {
        this.nodeResponse = nodeResponse;
    }

    public SpiInvokeResult(){}

    public SpiInvokeResult(T context){
        this.context = context;
    }

    public SpiInvokeResult(T context, List<R> nodeResponse){
        this.context = context;
        this.nodeResponse = nodeResponse;
    }
}
