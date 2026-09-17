package baby.top.test.handler;

import baby.top.mqtt.handler.MqttHandler;
import baby.top.mqtt.handler.MqttMessageContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

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
    public void handle(MqttMessageContext context) {
        String topic = context.getTopic();
        byte[] _payload = context.getPayload();
        String payload = new String(_payload, StandardCharsets.UTF_8);
        count++;
        log.info("count:{}, topic: {}，payload: {}", count, topic, payload);
    }
}