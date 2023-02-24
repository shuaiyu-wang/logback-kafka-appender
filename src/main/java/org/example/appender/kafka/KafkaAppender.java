package org.example.appender.kafka;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.appender.formatter.Formatter;
import org.example.appender.formatter.MessageFormatter;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author: create by wangshuaiyu
 * @date: 2023/2/13
 */
public class KafkaAppender extends LazyKafkaProducer {

    private ArrayBlockingQueue<ILoggingEvent> blockingQueue;;
    private volatile boolean started = false;
    Log2KafkaWorker worker = new Log2KafkaWorker();

    @Override
    public void setContext(Context context) {
        // 设置控制台输出
        OnConsoleStatusListener onConsoleStatusListener = new OnConsoleStatusListener();
        onConsoleStatusListener.start();
        context.getStatusManager().add(onConsoleStatusListener);
        super.setContext(context);
    }

    public KafkaAppender() {
        // 默认的参数
        addProducerConfigValue(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        addProducerConfigValue(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    }

    @Override
    public void start() {
        addInfo("启动自定义Kafka Appender");

        blockingQueue = new ArrayBlockingQueue<>(queueSize);

        super.start();

        worker.start();

        // 不加这一行 stop不会调用
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

    }

    @Override
    public void stop() {
        addInfo("停止自定义Kafka Appender");

        worker.interrupt();
        addInfo("通知 log2KafkaWorker 中断");

        try {
            // 在设定的超时时间内 等待worker处理完
            worker.join(maxTimeout);

            // check to see if the thread ended and if not add a warning message
            if (worker.isAlive()) {
                addWarn("Max queue flush timeout (" + maxTimeout + " ms) exceeded. Approximately " + blockingQueue.size()
                        + " queued events were possibly discarded.");
            } else {
                addInfo("Queue flush finished successfully within timeout.");
            }
        } catch (InterruptedException e) {
            int remaining = blockingQueue.size();
            addError("Failed to join worker thread. " + remaining + " queued events may be discarded.", e);
        } finally {
        }

        KafkaAppender.super.close();

        super.stop();

    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        // 发送到kafka之前确保初始化producer
        super.initProducer();
        put(eventObject);

    }

    private void put(ILoggingEvent eventObject) {
        if (neverBlock) {
            blockingQueue.offer(eventObject);
        } else {
            putUninterruptibly(eventObject);
        }
    }

    private void putUninterruptibly(ILoggingEvent eventObject) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    blockingQueue.put(eventObject);
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    class Log2KafkaWorker extends Thread {

        @Override
        public synchronized void start() {
            KafkaAppender.this.started = true;
            super.start();
        }

        @Override
        public void interrupt() {
            KafkaAppender.this.started = false;
            super.interrupt();
        }

        @Override
        public void run() {
            while (started) {
                try {
                    ILoggingEvent e = blockingQueue.take();
                    send(e);
                } catch (InterruptedException e) {
                    // 收到中断信号之后，终止循环，
                    // 当前阻塞任务被丢弃，剩余任务交给下面遍历处理
                    break;
                }
            }

            for (ILoggingEvent e : blockingQueue) {
                send(e);
                blockingQueue.remove(e);
            }
        }

        private void send(ILoggingEvent event) {
            addInfo(">>>>>>发送到kafka："+event.getFormattedMessage());
            String message = KafkaAppender.super.formatter.format(event);
            KafkaAppender.super.send(topic, event.getThreadName(), message);
            addInfo("<<<<<<发送到kafka");
        }
    }
}
