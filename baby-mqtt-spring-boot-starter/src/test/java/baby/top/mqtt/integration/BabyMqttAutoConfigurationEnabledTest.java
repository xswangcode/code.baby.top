package baby.top.mqtt.integration;

import baby.top.mqtt.autoconfigure.BabyMqttAutoConfiguration;
import baby.top.mqtt.handler.MqttHandler;
import baby.top.mqtt.handler.MqttHandlerRegistry;
import baby.top.mqtt.matcher.TrieMqttTopicMatcher;
import baby.top.mqtt.properties.BabyMqttProperties;
import baby.top.mqtt.template.BabyMqttTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.test.context.TestPropertySource;

import javax.annotation.Resource;

/**
 * MQTT AutoConfiguration 开启测试
 */
@SpringBootTest(
        classes = BabyMqttAutoConfigurationEnabledTest.TestApplication.class
)
@TestPropertySource(properties = {
        "baby.mqtt.enabled=true",
        "baby.mqtt.broker=tcp://127.0.0.1:1883",
        "baby.mqtt.client-id=baby-mqtt-test",
        "baby.mqtt.auto-reconnect=false",
        "baby.mqtt.connection-timeout=5",
        "baby.mqtt.keep-alive=30",
        "baby.mqtt.matcher-type=trie",
        "baby.mqtt.subscriptions[0].name=robotSleep",
        "baby.mqtt.subscriptions[0].topic=produce/+/robot_sleep_mode/+",
        "baby.mqtt.subscriptions[0].handler=testMqttHandler"
})
public class BabyMqttAutoConfigurationEnabledTest {

    /**
     * 避免测试真正连接 MQTT Broker
     */
    @MockBean
    private MqttPahoMessageDrivenChannelAdapter
            babyMqttInboundAdapter;

    @Resource
    private BabyMqttProperties properties;

    @Resource
    private MqttHandlerRegistry registry;

    @Resource
    private BabyMqttTemplate template;

    @Test
    public void testMqttEnabled() {

        // 1. AutoConfiguration 加载
        Assertions.assertEquals(
                1,
                registry.getClass()
                        .getName()
                        .contains("MqttHandlerRegistry")
                        ? 1 : 0
        );

        // 2. 配置绑定
        Assertions.assertTrue(
                properties.isEnabled()
        );

        Assertions.assertEquals(
                "tcp://127.0.0.1:1883",
                properties.getBroker()
        );

        Assertions.assertEquals(
                "baby-mqtt-test",
                properties.getClientId()
        );

        Assertions.assertEquals(
                "trie",
                properties.getMatcherType()
        );

        // 3. Subscription 配置
        Assertions.assertNotNull(
                properties.getSubscriptions()
        );

        Assertions.assertEquals(
                1,
                properties.getSubscriptions().size()
        );

        Assertions.assertEquals(
                "robotSleep",
                properties.getSubscriptions()
                        .get(0)
                        .getName()
        );

        Assertions.assertEquals(
                "produce/+/robot_sleep_mode/+",
                properties.getSubscriptions()
                        .get(0)
                        .getTopic()
        );

        Assertions.assertEquals(
                "testMqttHandler",
                properties.getSubscriptions()
                        .get(0)
                        .getHandler()
        );

        // 4. Registry
        Assertions.assertNotNull(
                registry
        );

        Assertions.assertEquals(
                1,
                registry.size()
        );

        MqttHandler handler =
                registry.getHandler(
                        "testMqttHandler"
                );

        Assertions.assertNotNull(
                handler
        );

        Assertions.assertEquals(
                "testMqttHandler",
                handler.getName()
        );

        // 5. Topic Matcher
        Assertions.assertEquals(
                1,
                registry.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                ).size()
        );

        Assertions.assertTrue(
                registry.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                ).get(0) == handler
        );

        // 6. Template
        Assertions.assertNotNull(
                template
        );
    }

    /**
     * 测试 Spring Boot Application
     * <p>
     * 这里直接把测试 Handler 声明在当前测试上下文中，
     * 避免 ComponentScan 扫描不到的问题。
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(BabyMqttAutoConfiguration.class)
    static class TestApplication {

        @org.springframework.context.annotation.Bean
        public MqttHandler testMqttHandler() {

            return new MqttHandler() {

                @Override
                public String getName() {
                    return "testMqttHandler";
                }

                @Override
                public void handle(
                        String topic,
                        String payload) {
                    // 测试无需处理实际消息
                }
            };
        }
    }
}