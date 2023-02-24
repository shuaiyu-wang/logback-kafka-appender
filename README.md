# Logback Kafka Appender

基于Logback自定义的一个Kafka Appender，可以实现日志异步自动格式化为JSON字符串并推送到Kafka Broker

## 安装
 
下载代码打包并安装到本地Maven仓库`mvn clean install`

## 使用

* 在业务代码的pom文件中引入依赖
```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>logback-kafka-appender</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
* 在logback配置文件中添加Kafka Appender
```xml
<appender name="error_log2kafka" class="org.example.appender.kafka.KafkaAppender">
        <!--不能有encoder这个标签-->
<!--        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">-->
<!--            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>-->
<!--        </encoder>-->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <!--日志发送topic，默认是errorMsgTopic-->
        <topic>test</topic>
        <!-- each <producerConfig> translates to regular kafka-client config (format: key=value) -->
        <producerConfig>bootstrap.servers=127.0.0.1:9092</producerConfig>
        <formatter class="org.example.appender.formatter.JsonFormatter">
            <!--
            Whether we expect the log message to be JSON encoded or not.
            If set to "false", the log message will be treated as a string,
            and wrapped in quotes. Otherwise it will be treated as a parseable
            JSON object.
            -->
            <expectJson>false</expectJson>
            <!--堆栈信息遍历的最大深度 默认是10-->
            <maxDepth>50</maxDepth>
        </formatter>
    </appender>
```

## 参考项目

* [logback-kafka-appender](https://github.com/danielwegener/logback-kafka-appender)
* [logback-kafka](https://github.com/ptgoetz/logback-kafka)
* [logback](https://github.com/qos-ch/logback)