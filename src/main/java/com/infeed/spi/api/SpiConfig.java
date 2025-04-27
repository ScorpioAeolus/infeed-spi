package com.infeed.spi.api;

import com.infeed.spi.common.ILogInject;

import java.io.Serializable;

/**
 * spi配置
 *
 * @author typhoon
 * @since 2024-08-15 Thursday
 */
public class SpiConfig implements Serializable, ILogInject {
    private static final long serialVersionUID = 3238647086847223476L;

    /**
     * SpiProvider的名称
     */
    private String name;

    /**
     * SpiProvider的同组互斥规则,true: 互斥,false: 不互斥。
     */
    private boolean mutex;

    /**
     * SpiProvider的执行顺序,值越大越靠后执行。
     */
    private int priority;

    public SpiConfig() {
    }

    public SpiConfig(String name, boolean mutex, int priority) {
        this.name = name;
        this.mutex = mutex;
        this.priority = priority;
    }

    public static SpiConfig create(String name, boolean mutex, int priority) {
        return new SpiConfig(name, mutex, priority);
    }

    public static SpiConfig create() {
        return new SpiConfig();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMutex() {
        return mutex;
    }

    public void setMutex(boolean mutex) {
        this.mutex = mutex;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
