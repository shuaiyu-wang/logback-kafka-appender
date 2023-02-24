package org.example.appender.serializer;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author: create by wangshuaiyu
 * @date: 2023/2/23
 */
public class ThrowableProxyJsonSerializer extends JsonSerializer<IThrowableProxy> {

    private int maxDepth = 10;

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public void serialize(IThrowableProxy throwableProxy, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("class", throwableProxy.getClassName());
        jsonGenerator.writeStringField("message", throwableProxy.getMessage());
        StackTraceElementProxy[] stackTrace = throwableProxy.getStackTraceElementProxyArray();
        if (stackTrace != null) {
            jsonGenerator.writeArrayFieldStart("stackTrace");
            int depth = 0;
            for (StackTraceElementProxy element : stackTrace) {
                if (depth++ == maxDepth) {
                    break;
                }
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("class", element.getStackTraceElement().getClassName());
                jsonGenerator.writeStringField("method", element.getStackTraceElement().getMethodName());
                jsonGenerator.writeStringField("file", element.getStackTraceElement().getFileName());
                jsonGenerator.writeNumberField("line", element.getStackTraceElement().getLineNumber());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        IThrowableProxy cause = throwableProxy.getCause();
        if (cause != null) {
            jsonGenerator.writeFieldName("cause");
            serializerProvider.defaultSerializeValue(cause, jsonGenerator);
        }

        jsonGenerator.writeEndObject();
    }
}
