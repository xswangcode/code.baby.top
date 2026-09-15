package baby.top.mqtt.handler;

/**
 * MQTT 消息处理器。
 * <p>
 * 一个 Handler 可以通过 Topic 模式
 * 处理一类 MQTT 消息。
 *
 * @author baby
 */
public interface MqttHandler {

    /**
     * 获取 Handler 名称。
     * <p>
     * 用于配置文件中引用 Handler。
     *
     * @return Handler 名称
     */
    String getName();

    /**
     * 处理 MQTT 消息。
     *
     * @param topic   MQTT Topic
     * @param payload MQTT 消息内容
     */
    void handle(String topic, String payload);
}
