package baby.top.test.handler;

import baby.top.mqtt.handler.AbstractMqttHandler;
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
public class TestAbsMqttHandler extends AbstractMqttHandler {

    /**
     * 最简入口：只关心 topic + 字符串内容时实现本方法即可。
     *
     * @param topic   主题
     * @param payload 解码后的内容
     */
    @Override
    protected void onMessage(String topic, String payload) {

    }

    /**
     * 获取 Handler 名称。
     * <p>
     * 用于配置文件中引用 Handler。
     *
     * @return Handler 名称
     */
    @Override
    public String getName() {
        return this.getClass().getName();
    }
}