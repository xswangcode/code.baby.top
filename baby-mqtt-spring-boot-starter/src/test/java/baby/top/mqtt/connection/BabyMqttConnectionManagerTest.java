package baby.top.mqtt.connection;

import baby.top.mqtt.autoconfigure.BabyMqttAutoConfiguration;
import baby.top.mqtt.handler.MqttHandler;
import baby.top.mqtt.handler.MqttMessageContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Baby MQTT 连接管理测试。
 *
 * @author baby
 */
@SpringBootTest(classes = BabyMqttConnectionManagerTest.TestApplication.class)
@TestPropertySource(properties = {"baby.mqtt.enabled=true", "baby.mqtt.broker=tcp://1.95.78.244:1883", "baby.mqtt.client-id=baby-mqtt-test", "baby.mqtt.auto-reconnect=false", "baby.mqtt.connection-timeout=5", "baby.mqtt.keep-alive=30", "baby.mqtt.matcher-type=trie", "baby.mqtt.subscriptions[0].name=robotSleep", "baby.mqtt.subscriptions[0].topic=produce/+/robot_sleep_mode/+", "baby.mqtt.subscriptions[0].handler=testMqttHandler"})
public class BabyMqttConnectionManagerTest {

    @Autowired
    private BabyMqttConnectionManager connectionManager;

    /**
     * 测试连接管理器 Bean 是否正常创建。
     */
    @Test
    public void testConnectionManager() {

        assertNotNull(connectionManager);

        System.out.println("当前 MQTT 连接状态: " + connectionManager.getState());
    }

    /**
     * 测试 MQTT 连接是否成功。
     *
     * <p>
     * Spring Boot 启动后，等待 MQTT Inbound Adapter
     * 完成连接和 Topic 订阅。
     */
    @Test
    public void testMqttConnected() throws InterruptedException {

        // 等待 MQTT 连接和订阅完成
        long timeout = 10000L;
        long startTime = System.currentTimeMillis();

        while (connectionManager.getState() != MqttConnectionState.CONNECTED) {

            if (System.currentTimeMillis() - startTime >= timeout) {

                break;
            }

            Thread.sleep(200L);
        }

        System.out.println("MQTT 最终连接状态: " + connectionManager.getState());

        assertEquals(MqttConnectionState.CONNECTED, connectionManager.getState());
    }

    @Test
    void testConnectionState() throws InterruptedException {

        System.out.println("当前 MQTT 连接状态: " + connectionManager.getState());

        Thread.sleep(30000);

        System.out.println("30 秒后 MQTT 连接状态: " + connectionManager.getState());
    }


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

    @SpringBootApplication
    @Import(BabyMqttAutoConfiguration.class)
    @ComponentScan("baby.top")
    static class TestApplication {
    }
}