package org.example.appender.formatter;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @author: create by wangshuaiyu
 * @date: 2023/2/15
 */
public interface Formatter {

    String format(ILoggingEvent event);

}
