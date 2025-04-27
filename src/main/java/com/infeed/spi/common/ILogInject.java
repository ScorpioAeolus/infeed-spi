package com.infeed.spi.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志管理抽象
 *
 * @author typhoon
 * @date 2025-04-27 19:29 Sunday
 **/
public interface ILogInject {

    Logger log = LoggerFactory.getLogger(ILogInject.class);

}
