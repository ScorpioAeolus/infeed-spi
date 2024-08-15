package com.infeed.spi.spring.scanner;


import com.infeed.spi.api.ISpiProvider;

/**
 * spi类型过滤器
 *
 * @author typhoon
 * @date 2024-08-15 20:05 Thursday
 */
public class ISpiProviderClassTypeFilter implements IClassTypeFilter {

    @Override
    public boolean match(Class<?> clazz) {
        return ISpiProvider.class.isAssignableFrom(clazz);
    }
}
