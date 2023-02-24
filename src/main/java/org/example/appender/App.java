package org.example.appender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: create by wangshuaiyu
 * @date: 2023/2/13
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            try {
                System.out.println(1/0);
            }catch (Exception e) {
                logger.error("KKK",e);
            }
        }
        Thread.sleep(10000);

        System.out.println("主线程结束");
    }

}
