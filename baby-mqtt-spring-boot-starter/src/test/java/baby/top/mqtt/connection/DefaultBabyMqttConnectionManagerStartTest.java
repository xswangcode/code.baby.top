package baby.top.mqtt.connection;

import baby.top.mqtt.autoconfigure.BabyMqttAutoConfiguration;
import baby.top.mqtt.handler.MqttHandler;
import baby.top.mqtt.handler.MqttMessageContext;
import baby.top.mqtt.properties.BabyMqttProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefaultBabyMqttConnectionManager Spring Boot 启动测试。
 *
 * <p>
 * 使用真实 Spring Boot 容器和真实 MQTT Broker。
 *
 * <p>
 * 测试完整链路：
 *
 * <pre>
 * Spring Boot 启动
 *      ↓
 * MQTT AutoConfiguration
 *      ↓
 * BabyMqttConnectionManager
 *      ↓
 * BabyMqttConnectionLifecycle
 *      ↓
 * MqttPahoMessageDrivenChannelAdapter
 *      ↓
 * 真实 MQTT Broker
 *      ↓
 * MqttSubscribedEvent
 *      ↓
 * DefaultBabyMqttConnectionManager
 *      ↓
 * CONNECTED
 * </pre>
 */
@SpringBootTest(classes = DefaultBabyMqttConnectionManagerStartTest.TestApplication.class)
@TestPropertySource(properties = {"baby.mqtt.enabled=true", "baby.mqtt.broker=tcp://1.95.78.244:1883", "baby.mqtt.client-id=baby-mqtt-test", "baby.mqtt.auto-reconnect=false", "baby.mqtt.connection-timeout=5", "baby.mqtt.keep-alive=30", "baby.mqtt.matcher-type=trie", "baby.mqtt.subscriptions[0].name=robotSleep", "baby.mqtt.subscriptions[0].topic=produce/+/robot_sleep_mode/+", "baby.mqtt.subscriptions[0].handler=testMqttHandler"})
class DefaultBabyMqttConnectionManagerStartTest {

    @Autowired
    private BabyMqttConnectionManager connectionManager;

    @Autowired
    private BabyMqttProperties mqttProperties;


    @Bean
    public MqttHandler testMqttHandler() {
        return new MqttHandler() {
            @Override
            public String getName() {
                return "robotSleep";
            }

            @Override
            public void handle(MqttMessageContext context) {
                System.out.println("测试handler :" + new Date());

            }
        };
    }

    /**
     * Spring Boot 启动后，
     * MQTT 最终应该连接成功。
     */
    @Test
    void shouldConnectSuccessfullyAfterSpringBootStart()
            throws InterruptedException {

        /*
         * Spring Boot 启动完成时，
         * MQTT 连接可能还处于 CONNECTING。
         *
         * 所以这里等待最多 30 秒，
         * 给 Spring Integration 留出连接 Broker 的时间。
         */
        long timeout = System.currentTimeMillis() + 30000L;

        while (System.currentTimeMillis() < timeout) {

            MqttConnectionState state =
                    connectionManager.getState();

            System.out.println(
                    "当前 MQTT 连接状态: " + state
            );

            if (state == MqttConnectionState.CONNECTED) {
                break;
            }

            Thread.sleep(500L);
        }

        /*
         * 最终必须连接成功。
         */
        assertEquals(
                MqttConnectionState.CONNECTED,
                connectionManager.getState()
        );

        assertTrue(
                connectionManager.isConnected()
        );

        System.out.println(
                "MQTT 最终连接状态: "
                        + connectionManager.getState()
        );
    }

    /**
     * 验证测试使用的 MQTT 配置已经被 Spring 加载。
     */
    @Test
    void shouldLoadMqttProperties() {

        assertNotNull(mqttProperties);

        System.out.println(
                "MQTT Broker: "
                        + mqttProperties.getBroker()
        );

        System.out.println(
                "MQTT ClientId: "
                        + mqttProperties.getClientId()
        );
    }

    @SpringBootApplication
    @Import(BabyMqttAutoConfiguration.class)
    @ComponentScan("baby.top")
    static class TestApplication {
    }
}