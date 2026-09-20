package baby.top.mqtt.autoconfigure;

import baby.top.mqtt.handler.MqttHandlerRegistry;
import baby.top.mqtt.handler.MqttMessageContext;
import baby.top.mqtt.properties.BabyMqttProperties;
import baby.top.mqtt.properties.MqttSubscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.List;

@Slf4j
@Configuration
public class BabyMqttInboundConfiguration {

    /**
     * MQTT 入站消息 Channel
     */
    @Bean
    public MessageChannel babyMqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 创建 MQTT 入站 Adapter
     * <p>
     * 一个 Adapter 负责所有 MQTT Topic 的订阅。
     * <p>
     * Topic 与 Handler 的关系由：
     * <p>
     * MqttHandlerRegistry
     * +
     * MqttTopicMatcher
     * <p>
     * 负责处理。
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter babyMqttInboundAdapter(BabyMqttProperties properties, MqttPahoClientFactory clientFactory, MessageChannel babyMqttInputChannel) {

        String[] topics = getSubscriptionTopics(properties);

        log.info("创建 MQTT Inbound Adapter，clientId: {}, topics: {}", properties.getClientId(), topics);

        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(properties.getClientId() + "-inbound", clientFactory, topics);

        adapter.setOutputChannel(babyMqttInputChannel);
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(true);
        adapter.setConverter(converter);
        return adapter;
    }

    /**
     * MQTT 消息统一入口。
     * <p>
     * 所有 Topic 的消息都会先进入这里，
     * 再交给 MqttHandlerRegistry 分发。
     */
    @Bean
    @ServiceActivator(inputChannel = "babyMqttInputChannel")
    public MessageHandler babyMqttMessageHandler(MqttHandlerRegistry registry) {

        return message -> {

            // 获取 MQTT Topic
            Object topicObject = message.getHeaders().get("mqtt_receivedTopic");

            String topic = topicObject == null ? null : topicObject.toString();

            // 获取原始 Payload
            byte[] payload = (byte[]) message.getPayload();

            // 获取 MQTT QoS
            Object qosObject = message.getHeaders().get("mqtt_receivedQos");

            int qos = qosObject instanceof Number ? ((Number) qosObject).intValue() : 0;

            // 获取是否为保留消息
            Object retainedObject = message.getHeaders().get("mqtt_receivedRetained");

            boolean retained = Boolean.TRUE.equals(retainedObject);

            // 获取是否为重复投递
            Object duplicateObject = message.getHeaders().get("mqtt_receivedDuplicate");

            boolean duplicate = Boolean.TRUE.equals(duplicateObject);

            // 构建 MQTT 消息上下文
            MqttMessageContext context = new MqttMessageContext(topic, payload, qos, retained, duplicate);

            log.debug("收到 MQTT 消息，topic: {}, qos: {}", topic, qos);

            // 分发消息
            registry.dispatch(context);
        };
    }

    /**
     * 从配置中提取 Topic。
     * <p>
     * 注意：
     * <p>
     * subscriptions 是：
     * <p>
     * List<MqttSubscription>
     * <p>
     * 不能直接：
     * <p>
     * subscriptions.toArray(new String[...])
     * <p>
     * 必须先把 MqttSubscription 转换成 String Topic。
     */
    private String[] getSubscriptionTopics(BabyMqttProperties properties) {

        List<MqttSubscription> subscriptions = properties.getSubscriptions();

        String[] topics = new String[subscriptions.size()];

        for (int i = 0; i < subscriptions.size(); i++) {

            MqttSubscription subscription = subscriptions.get(i);

            topics[i] = subscription.getTopic();
        }

        return topics;
    }
}