package baby.top.test.handler;

import baby.top.mqtt.handler.MqttHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MQTT 测试 Handler。
 *
 * @author baby
 */
@Slf4j
@Component
public class TestMqttHandler implements MqttHandler {

    static Integer count = 0;

    @Override
    public String getName() {
        return "testMqttHandler";
    }


    @Override
    public void handle(String topic, String payload) {
        count++;
        log.info("count:{}, topic: {}，payload: {}", count, topic, payload);
    }
}