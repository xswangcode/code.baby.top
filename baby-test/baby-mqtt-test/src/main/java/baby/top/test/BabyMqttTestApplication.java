package baby.top.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Baby MQTT 测试项目。
 *
 * @author baby
 */
@SpringBootApplication
@ComponentScan("baby.top")
public class BabyMqttTestApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                BabyMqttTestApplication.class,
                args
        );
    }
}