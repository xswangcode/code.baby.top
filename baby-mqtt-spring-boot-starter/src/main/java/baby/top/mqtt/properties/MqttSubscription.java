package baby.top.mqtt.properties;


import lombok.Data;

/**
 * MQTT Topic 订阅配置。
 */
@Data
public class MqttSubscription {

    /**
     * 订阅名称。
     */
    private String name;

    /**
     * MQTT Topic。
     * <p>
     * 支持：
     * + 单层通配符
     * # 多层通配符
     */
    private String topic;

    /**
     * Handler 名称。
     * <p>
     * 对应 MqttHandler#getName()
     */
    private String handler;
}