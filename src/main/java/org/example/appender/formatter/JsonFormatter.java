package org.example.appender.formatter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.example.appender.serializer.ThrowableProxyJsonSerializer;
import org.example.appender.util.TimeUtil;

public class JsonFormatter implements Formatter {
    private static final String QUOTE = "\"";
    private static final String COLON = ":";
    private static final String COMMA = ",";

    private int maxDepth;
    private boolean expectJson = false;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ThrowableProxyJsonSerializer throwableProxyJsonSerializer = new ThrowableProxyJsonSerializer();
    private final SimpleModule module = new SimpleModule();

    {
        module.addSerializer(IThrowableProxy.class, throwableProxyJsonSerializer);
        mapper.registerModule(module);
    }

    public String format(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        fieldName("project", sb);
        quote(event.getLoggerContextVO().getName(), sb);
        sb.append(COMMA);
        fieldName("level", sb);
        quote(event.getLevel().levelStr, sb);
        sb.append(COMMA);
        fieldName("logger", sb);
        quote(event.getLoggerName(), sb);
        sb.append(COMMA);
        fieldName("thread", sb);
        quote(event.getThreadName(), sb);
        sb.append(COMMA);
        fieldName("timestamp", sb);
        sb.append(event.getTimeStamp());
        sb.append(COMMA);
        fieldName("date", sb);
        quote(TimeUtil.getDateString(), sb);
        sb.append(COMMA);
        fieldName("message", sb);
        if (this.expectJson) {
            sb.append(event.getFormattedMessage());
        } else {
            quote(event.getFormattedMessage(), sb);
        }
        sb.append(COMMA);
        fieldName("detail", sb);
        try {
            sb.append(mapper.writeValueAsString(event.getThrowableProxy()));
        } catch (JsonProcessingException e) {
            sb.append(mapper.nullNode());
        }
        sb.append("}");
        return sb.toString();
    }

    private static void fieldName(String name, StringBuilder sb) {
        quote(name, sb);
        sb.append(COLON);
    }

    private static void quote(String value, StringBuilder sb) {
        sb.append(QUOTE);
        sb.append(value);
        sb.append(QUOTE);
    }

    public boolean isExpectJson() {
        return expectJson;
    }

    public void setExpectJson(boolean expectJson) {
        this.expectJson = expectJson;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        throwableProxyJsonSerializer.setMaxDepth(maxDepth);
    }
}
