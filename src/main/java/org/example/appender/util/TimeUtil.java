package org.example.appender.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: create by wangshuaiyu
 * @date: 2023/2/27
 */
public class TimeUtil {

    public static String getDateString() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ZoneId zoneId = ZoneId.of("Asia/Shanghai"); // 设置时区为上海
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return zonedDateTime.format(formatter);
    }
}
