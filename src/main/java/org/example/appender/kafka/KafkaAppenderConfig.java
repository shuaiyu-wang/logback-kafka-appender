package org.example.appender.kafka;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.appender.formatter.Formatter;
import org.example.appender.formatter.MessageFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: create by wangshuaiyu
 * @date: 2023/2/15
 */
public abstract class KafkaAppenderConfig<E> extends UnsynchronizedAppenderBase<E> {

    String topic="errorMsgTopic";
    boolean neverBlock = false;
    int queueSize = 256;
    int maxTimeout = 5000;
    Formatter formatter=new MessageFormatter();

    protected Map<String,Object> producerConfig = new HashMap<String, Object>();

    // 在XML配置中设置属性值
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public void setNeverBlock(boolean neverBlock) {
        this.neverBlock = neverBlock;
    }
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }
    public void setMaxTimeout(int maxTimeout) {
        this.maxTimeout = maxTimeout;
    }

    // 在XML配置中设置多个属性
    public void addProducerConfig(String keyValue) {
        String[] split = keyValue.split("=", 2);
        addInfo(keyValue);
        if(split.length == 2) {
            addProducerConfigValue(split[0], split[1]);
        }
    }

    public void addProducerConfigValue(String key, Object value) {
        this.producerConfig.put(key, value);
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }
}
