package com.infeed.spi.core.concurrent;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * google并发操作带回调封装
 *
 * @author typhoon
 * @since 2025-04-10 11:04 Thursday
 **/
public interface CustomExecutorService {

    Logger log = LoggerFactory.getLogger(CustomExecutorService.class);


    int cpuNum = Runtime.getRuntime().availableProcessors();

    ListeningExecutorService defaultExecutorService = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(cpuNum*4, cpuNum*8, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000000), new ThreadFactory() {
                private AtomicInteger count = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    int index = count.incrementAndGet();
                    return new Thread(r, "common-pool" + index);
                }
            },  (r, executor) -> {
                log.error("common pool exhaust. {}, {}, {}", executor.getActiveCount(), executor.getTaskCount(), executor.getQueue().size());
    }));



    /**
     * 提交带带回调的多线程操作
     *
     * @param executorService thread pool
     * @param retrieve buzz
     * @param operate after buzz
     * @param r return
     * @param <T> return type
     * @param <R> target operate data
     * @param countDown  thread count
     */
    default <T,R> void submitWithCallback(ListeningExecutorService executorService, IRetrieve<T> retrieve, ICallbackOperate<T,R> operate, R r, CountDownLatch countDown) {
        ListenableFuture<T> future = executorService.submit(retrieve::retrieve);
        Futures.addCallback(future, new FutureCallback<T>() {
            @Override
            public void onSuccess(@Nullable T t) {
                if(null != operate) {
                    operate.operate(t,r);
                }
                if(null != countDown) {
                    countDown.countDown();
                }
            }
            @Override
            public void onFailure(Throwable throwable) {
                if(null != countDown) {
                    countDown.countDown();
                }
                log.error("CustomExecutorService.submitWithCallback occur error;",throwable);
            }
        },executorService);
    }


    /**
     * 提交带回调的多线程操作
     *
     * @param retrieve buzz
     * @param operate after buzz
     * @param r return
     * @param countDown thread count
     * @param <T> target  type
     * @param <R> return type
     */
    default <T,R> void submitWithCallback(IRetrieve<T> retrieve, ICallbackOperate<T,R> operate, R r, CountDownLatch countDown) {
        this.submitWithCallback(defaultExecutorService,retrieve,operate,r,countDown);
    }

}
