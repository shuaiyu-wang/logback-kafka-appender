package org.example.appender.kafka;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;

import java.util.HashMap;

/**
 * @author wangshuaiyu
 */
public abstract class LazyKafkaProducer extends KafkaAppenderConfig<ILoggingEvent> {

    private volatile Producer<String, String> producer;

    /**
     * Lazy initializer for producer, patterned after commons-lang.
     * @see <a href="https://commons.apache.org/proper/commons-lang/javadocs/api-3.4/org/apache/commons/lang3/concurrent/LazyInitializer.html">LazyInitializer</a>
     */
    public void initProducer() {
        if (this.producer == null) {
            synchronized(this) {
                if(this.producer == null) {
                    this.producer = new KafkaProducer<>(new HashMap<>(producerConfig));
                }
            }
        }
    }

    public void send(String topic, String key, String msg){
        try {
            producer.send(new ProducerRecord<>(topic, key , msg));
        } catch (AuthorizationException e) {
            // We can't recover from these exceptions, so our only option is to close the producer and exit.
            addError("kafka authorization exception", e);
            producer.close();
        } catch (KafkaException e) {
            // For all other exceptions, just abort the transaction and try again.
            addError("kafka exception", e);
            producer.close();
        } catch (Exception e) {
            addError("kafka execute exception", e);
            producer.close();
        }
    }

    public void close() {
        if (producer!=null) {
            producer.close();
        }
    }

}