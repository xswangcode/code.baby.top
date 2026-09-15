package baby.top.test.runner;

import baby.top.mqtt.template.BabyMqttTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * MQTT 发布测试。
 * <p>
 * Spring Boot 启动完成后自动发送一条 MQTT 消息。
 *
 * @author baby
 */
@Slf4j
@Component
public class MqttPublishTestRunner implements CommandLineRunner {

    private final BabyMqttTemplate mqttTemplate;

    public MqttPublishTestRunner(BabyMqttTemplate mqttTemplate) {

        this.mqttTemplate = mqttTemplate;
    }

    @Override
    public void run(String... args) {

        String topic = "produce/2310/robot_sleep_mode/BS10L_R01";

        String payload = "{" + "\"deviceName\":\"BS10L_R01\"," + "\"SleepStatus\":false," + "\"time\":1789380060000" + "}";

        log.info("========== 开始 MQTT 发布测试 ==========");

        mqttTemplate.publish(topic, payload, 0, false);

        log.info("MQTT 发布测试执行完成，topic: {}", topic);
    }
}