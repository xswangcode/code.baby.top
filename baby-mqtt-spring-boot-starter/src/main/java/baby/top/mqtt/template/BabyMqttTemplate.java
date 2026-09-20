package baby.top.mqtt.template;

import baby.top.mqtt.properties.BabyMqttProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import javax.annotation.PreDestroy;

/**
 * Baby MQTT 消息发送模板。
 * <p>
 * 负责业务侧向 MQTT Broker 发布消息。
 *
 * @author baby
 */
@Slf4j
public class BabyMqttTemplate {

    private final MqttPahoMessageHandler messageHandler;

    public BabyMqttTemplate(BabyMqttProperties properties, MqttPahoClientFactory clientFactory) {

        String clientId = properties.getClientId();

        if (clientId == null || clientId.isEmpty()) {
            clientId = "baby-mqtt-client";
        }

        clientId = clientId + "-outbound";

        this.messageHandler = new MqttPahoMessageHandler(clientId, clientFactory);

        // 同步发送
        this.messageHandler.setAsync(false);

        try {
            // 手动创建的对象，不经过 Spring Bean 生命周期，
            // 所以需要手动执行初始化。
            this.messageHandler.afterPropertiesSet();

            // 初始化完成后再启动。
            this.messageHandler.start();

        } catch (Exception e) {
            throw new IllegalStateException("MQTT 出站 Handler 初始化失败", e);
        }

        log.info("[BABY-MQTT] BabyMqttTemplate 创建完成, clientId={}", clientId);
    }

    public void publish(String topic, String payload) {

        publish(topic, payload, 0, false);
    }

    public void publish(String topic, String payload, int qos, boolean retained) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("MQTT Topic 不能为空");
        }

        if (qos < 0 || qos > 2) {
            throw new IllegalArgumentException("MQTT QoS 必须为 0、1 或 2");
        }

        Message<String> message = MessageBuilder.withPayload(payload).setHeader("mqtt_topic", topic).setHeader("mqtt_qos", qos).setHeader("mqtt_retained", retained).build();

        try {

            log.debug("[BABY-MQTT] MQTT 消息发布, topic={}, qos={}, retained={}", topic, qos, retained);

            messageHandler.handleMessage(message);

            log.debug("[BABY-MQTT] MQTT 消息发布成功, topic={}, qos={}, retained={}", topic, qos, retained);

        } catch (Exception e) {

            log.error("[BABY-MQTT] MQTT 消息发布失败, topic={}, qos={}, retained={}", topic, qos, retained, e);

            throw new IllegalStateException("MQTT 消息发送失败", e);
        }
    }

    @PreDestroy
    public void destroy() {

        if (messageHandler != null) {

            try {
                messageHandler.stop();

                log.info("[BABY-MQTT] BabyMqttTemplate 停止完成");

            } catch (Exception e) {

                log.warn("[BABY-MQTT] BabyMqttTemplate 停止失败", e);
            }
        }
    }
}