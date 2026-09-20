package baby.top.mqtt.handler;

import java.nio.charset.StandardCharsets;

/**
 * MqttHandler 抽象基类。
 * <p>
 * 默认将 payload 按 UTF-8 解码为 String 后交给 {@link #onMessage}，
 * 并提供统一的异常兜底 {@link #onError} 与扩展入口。
 * <p>
 * 子类通常只需实现 {@link #onMessage(String, String, MqttMessageContext)}，
 * 若需处理二进制或自定义协议，可重写 {@link #decode} 或 {@link #handle}。
 */
public abstract class AbstractMqttHandler implements MqttHandler {

    /**
     * 模板方法：解码 -> 分发 -> 异常兜底。
     * <p>
     * 一般子类无需重写；如需完全自定义（如二进制协议），可覆盖本方法。
     */
    @Override
    public void handle(MqttMessageContext context) {
        if (context == null) {
            return;
        }
        try {
            String payload = decode(context.getPayload());
            onMessage(context.getTopic(), payload, context);
        } catch (Exception e) {
            onError(context, e);
        }
    }

    /**
     * 将原始字节解码为字符串。
     * <p>
     * 默认 UTF-8；如需 GBK 或自定义协议，覆盖本方法。
     *
     * @param payload 原始 payload，可能为 null
     * @return 解码后的字符串，payload 为 null 时返回 null
     */
    protected String decode(byte[] payload) {
        return payload == null ? null : new String(payload, StandardCharsets.UTF_8);
    }

    /**
     * 默认 String 入口。
     * <p>
     * 默认委托给 {@link #onMessage(String, String)}；
     * 若需要访问原始 context（qos、retained、原始 byte[] 等），覆盖本方法。
     *
     * @param topic   主题
     * @param payload UTF-8 解码后的内容
     * @param context 原始上下文
     */
    protected void onMessage(String topic, String payload, MqttMessageContext context) {
        onMessage(topic, payload);
    }

    /**
     * 最简入口：只关心 topic + 字符串内容时实现本方法即可。
     *
     * @param topic   主题
     * @param payload 解码后的内容
     */
    protected abstract void onMessage(String topic, String payload);

    /**
     * 异常兜底钩子。
     * <p>
     * 默认不处理，子类可覆盖做告警、落库、重试等。
     *
     * @param context 出错的上下文
     * @param e       异常
     */
    protected void onError(MqttMessageContext context, Exception e) {
        // 默认不处理，由子类按业务需求扩展。
    }
}