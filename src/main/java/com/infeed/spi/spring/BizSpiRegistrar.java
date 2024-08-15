package com.infeed.spi.spring;

import com.infeed.spi.annotation.BizSpi;
import com.infeed.spi.common.MapUtil;
import com.infeed.spi.core.container.ISpiContainer;
import com.infeed.spi.core.container.SpiRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * 应用启动时扫描@BizSpi标识的类，将其注册到SPI容器
 *
 * @author typhoon
 * @date 2024-08-15 19:52 Thursday
 */
@Slf4j
public class BizSpiRegistrar implements ApplicationContextAware {

    // 默认的SPI容器实现类
    private final ISpiContainer container = ISpiContainer.DEFAULT_INSTANCE;

    private void registerBizSpiBeans(Map<String, Object> spiBeanMap) {
        SpiRegister spiRegister = new SpiRegister();
        spiRegister.setContainer(container);
        spiRegister.setBizSpiBeans(spiBeanMap.values());
        spiRegister.register();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> spiBeanMap = applicationContext.getBeansWithAnnotation(BizSpi.class);

        // 没有需要注册的Bean
        if (MapUtil.isEmpty(spiBeanMap)) {
            return;
        }

        // 将Bean注册到Spi容器中
        registerBizSpiBeans(spiBeanMap);
    }
}
