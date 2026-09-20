package baby.top.mqtt.autoconfigure;

import baby.top.core.exception.BabyException;
import baby.top.mqtt.client.BabyMqttClient;
import baby.top.mqtt.client.BabyMqttConnectionLifecycle;
import baby.top.mqtt.connection.BabyMqttConnectionManager;
import baby.top.mqtt.connection.DefaultBabyMqttConnectionManager;
import baby.top.mqtt.handler.MqttHandler;
import baby.top.mqtt.handler.MqttHandlerRegistry;
import baby.top.mqtt.matcher.ListMqttTopicMatcher;
import baby.top.mqtt.matcher.MqttTopicMatcher;
import baby.top.mqtt.matcher.TrieMqttTopicMatcher;
import baby.top.mqtt.properties.BabyMqttProperties;
import baby.top.mqtt.properties.MqttSubscription;
import baby.top.mqtt.template.BabyMqttTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Baby MQTT 自动配置。
 *
 * @author baby
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(BabyMqttProperties.class) // 自动配置类显示注册配置文件
@Import({BabyMqttInboundConfiguration.class})
@ConditionalOnProperty(prefix = "baby.mqtt", name = "enabled", havingValue = "true", matchIfMissing = false)
public class BabyMqttAutoConfiguration {

    private final BabyMqttProperties properties;

    public BabyMqttAutoConfiguration(BabyMqttProperties properties) {

        this.properties = properties;
    }

    @PostConstruct
    public void validate() {
        validateProperties(properties);
    }

    /**
     * 创建 Baby MQTT Client。
     */
    @Bean
    @ConditionalOnMissingBean
    public BabyMqttClient babyMqttClient(BabyMqttProperties properties) {
        BabyMqttClient client = new BabyMqttClient(properties);
        client.init();
        return client;
    }

    /**
     * 暴露 Paho Client Factory。
     */
    @Bean
    @ConditionalOnMissingBean
    public MqttPahoClientFactory babyMqttClientFactory(BabyMqttClient client) {
        return client.getClientFactory();
    }

    /**
     * MQTT Handler 注册中心。
     * <p>
     * 负责：
     * 1. 注册 Spring 容器中的 MqttHandler
     * 2. 根据配置建立 Topic -> Handler 关联
     */
    @Bean
    @ConditionalOnMissingBean
    public MqttHandlerRegistry mqttHandlerRegistry(ListableBeanFactory beanFactory, MqttTopicMatcher matcher, BabyMqttProperties properties) {

        MqttHandlerRegistry registry = new MqttHandlerRegistry(matcher);

        // 1. 注册 Spring 容器中的所有 Handler
        Map<String, MqttHandler> handlerMap = beanFactory.getBeansOfType(MqttHandler.class);

        for (MqttHandler handler : handlerMap.values()) {
            registry.register(handler);
        }

        // 2. 根据配置建立 Topic -> Handler 关联
        if (properties.getSubscriptions() != null) {

            for (MqttSubscription subscription : properties.getSubscriptions()) {

                registry.registerSubscription(subscription.getTopic(), subscription.getHandler());
            }
        }

        log.info("Baby MQTT Handler 注册完成，数量: {}", registry.size());

        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttTopicMatcher babyMqttTopicMatcher(BabyMqttProperties properties) {

        if ("list".equalsIgnoreCase(properties.getMatcherType())) {

            return new ListMqttTopicMatcher();
        }


        if ("trie".equalsIgnoreCase(properties.getMatcherType())) {

            return new TrieMqttTopicMatcher();
        }

        throw new IllegalArgumentException("不支持的 MQTT Matcher 类型: " + properties.getMatcherType());
    }

    /**
     * MQTT 连接管理器。
     */
    @Bean
    @ConditionalOnMissingBean
    public BabyMqttConnectionManager babyMqttConnectionManager() {
        return new DefaultBabyMqttConnectionManager();
    }

    @Bean
    public BabyMqttConnectionLifecycle babyMqttConnectionLifecycle(BabyMqttConnectionManager connectionManager) {

        return new BabyMqttConnectionLifecycle(connectionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public BabyMqttTemplate babyMqttTemplate(BabyMqttProperties properties, MqttPahoClientFactory clientFactory) {
        return new BabyMqttTemplate(properties, clientFactory);
    }

    void validateProperties(BabyMqttProperties properties) {

        if (properties == null) {
            throw new BabyException("BABY-0002", "MQTT 配置不能为空");
        }

        if (properties.getBroker() == null || properties.getBroker().trim().isEmpty()) {

            throw new BabyException("BABY-0002", "MQTT broker 不能为空");
        }

        if (properties.getSubscriptions() == null || properties.getSubscriptions().isEmpty()) {

            throw new BabyException("BABY-0002", "MQTT topics 不能为空");
        }

        for (MqttSubscription subscription : properties.getSubscriptions()) {

            if (subscription == null) {
                throw new BabyException("BABY-0002", "MQTT subscription 不能为空");
            }

            if (subscription.getTopic() == null || subscription.getTopic().trim().isEmpty()) {

                throw new BabyException("BABY-0002", "MQTT subscription topic 不能为空");
            }

            if (subscription.getHandler() == null || subscription.getHandler().trim().isEmpty()) {

                throw new BabyException("BABY-0002", "MQTT subscription handler 不能为空");
            }
        }

        String matcherType = properties.getMatcherType();

        if (!"list".equalsIgnoreCase(matcherType) && !"trie".equalsIgnoreCase(matcherType)) {

            throw new BabyException("BABY-0002", "不支持的 MQTT Matcher 类型: " + matcherType);
        }

        if (properties.getConnectionTimeout() <= 0) {

            throw new BabyException("BABY-0002", "MQTT connection-timeout 必须大于 0");
        }

        if (properties.getKeepAlive() <= 0) {

            throw new BabyException("BABY-0002", "MQTT keep-alive 必须大于 0");
        }
    }
}