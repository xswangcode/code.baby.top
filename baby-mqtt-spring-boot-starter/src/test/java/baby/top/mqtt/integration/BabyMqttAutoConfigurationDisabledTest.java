package baby.top.mqtt.integration;

import baby.top.mqtt.client.BabyMqttClient;
import baby.top.mqtt.handler.MqttHandlerRegistry;
import baby.top.mqtt.matcher.MqttTopicMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(
        classes =
                BabyMqttAutoConfigurationDisabledTest
                        .TestApplication.class,
        properties =
                "baby.mqtt.enabled=false"
)
public class BabyMqttAutoConfigurationDisabledTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testMqttDisabled() {

        Assertions.assertEquals(
                0,
                applicationContext
                        .getBeansOfType(
                                BabyMqttClient.class
                        )
                        .size()
        );

        Assertions.assertEquals(
                0,
                applicationContext
                        .getBeansOfType(
                                MqttTopicMatcher.class
                        )
                        .size()
        );

        Assertions.assertEquals(
                0,
                applicationContext
                        .getBeansOfType(
                                MqttHandlerRegistry.class
                        )
                        .size()
        );
    }

    @SpringBootApplication
    static class TestApplication {
    }
}