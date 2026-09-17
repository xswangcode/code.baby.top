package baby.top.mqtt.handler;

import lombok.Data;

/**
 * MQTT 消息上下文。
 *
 * <p>
 * 封装 MQTT 消息的主题、原始消息内容及相关元数据，
 * 供 MqttHandler 处理。
 * </p>
 *
 * @author baby
 */
@Data
public class MqttMessageContext {

    /**
     * MQTT Topic。
     */
    private String topic;

    /**
     * MQTT 原始消息内容。
     *
     * <p>
     * 使用 byte[]，支持 JSON、文本、图片、Protobuf
     * 以及其他二进制数据。
     * </p>
     */
    private byte[] payload;

    /**
     * MQTT QoS 等级。
     *
     * <p>
     * 取值范围：0、1、2。
     * 0： 尽力发送，不保证送达（消息可能丢失）
     * 1： 至少送达一次（可能收到重复消息）
     * 2： 按 MQTT 协议进行恰好一次交付（协议开销更大）
     * </p>
     */
    private int qos;

    /**
     * 是否为保留消息。
     */
    private boolean retained;

    /**
     * 是否为重复投递的消息。
     */
    private boolean duplicate;

    /**
     * 创建 MQTT 消息上下文。
     *
     * @param topic     MQTT Topic
     * @param payload   MQTT 消息内容
     * @param qos       MQTT QoS 等级
     * @param retained  是否为保留消息
     * @param duplicate 是否为重复投递
     */
    public MqttMessageContext(String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.retained = retained;
        this.duplicate = duplicate;
    }
}