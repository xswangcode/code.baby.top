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
@EnableConfigurationProperties(BabyMqttProperties.class)
@Import({BabyMqttInboundConfiguration.class})
@ConditionalOnProperty(prefix = "baby.mqtt", name = "enabled", havingValue = "true", matchIfMissing = false)
public class BabyMqttAutoConfiguration {

    private final BabyMqttProperties properties;

    public BabyMqttAutoConfiguration(BabyMqttProperties properties) {
        this.properties = properties;
    }

    /**
     * MQTT 配置校验。
     */
    @PostConstruct
    public void validate() {

        validateProperties(properties);

        log.info("[BABY-MQTT] MQTT 配置校验通过, broker={}", properties.getBroker());
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

        MqttPahoClientFactory clientFactory = client.getClientFactory();

        log.info("[BABY-MQTT] MqttPahoClientFactory 创建完成");

        return clientFactory;
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

            log.info("[BABY-MQTT] MQTT Handler 注册完成, handler={}", handler.getClass().getSimpleName());
        }

        // 2. 根据配置建立 Topic -> Handler 关联
        if (properties.getSubscriptions() != null) {

            for (MqttSubscription subscription : properties.getSubscriptions()) {

                registry.registerSubscription(subscription.getTopic(), subscription.getHandler());

                log.info("[BABY-MQTT] MQTT Subscription 注册完成, topic={}, handler={}", subscription.getTopic(), subscription.getHandler());
            }
        }

        log.info("[BABY-MQTT] MqttHandlerRegistry 创建完成, handler数量={}", registry.size());

        return registry;
    }

    /**
     * 创建 MQTT Topic Matcher。
     */
    @Bean
    @ConditionalOnMissingBean
    public MqttTopicMatcher babyMqttTopicMatcher(BabyMqttProperties properties) {

        String matcherType = properties.getMatcherType();

        MqttTopicMatcher matcher;

        if ("list".equalsIgnoreCase(matcherType)) {

            matcher = new ListMqttTopicMatcher();

        } else if ("trie".equalsIgnoreCase(matcherType)) {

            matcher = new TrieMqttTopicMatcher();

        } else {

            throw new BabyException("BABY-0002", "不支持的 MQTT Matcher 类型: " + matcherType);
        }

        log.info("[BABY-MQTT] MQTT Topic Matcher 创建完成, type={}", matcherType);

        return matcher;
    }

    /**
     * MQTT 连接管理器。
     */
    @Bean
    @ConditionalOnMissingBean
    public BabyMqttConnectionManager babyMqttConnectionManager() {

        BabyMqttConnectionManager connectionManager = new DefaultBabyMqttConnectionManager();

        log.info("[BABY-MQTT] MQTT ConnectionManager 创建完成");

        return connectionManager;
    }

    /**
     * MQTT 连接生命周期监听器。
     */
    @Bean
    public BabyMqttConnectionLifecycle babyMqttConnectionLifecycle(BabyMqttConnectionManager connectionManager) {

        BabyMqttConnectionLifecycle lifecycle = new BabyMqttConnectionLifecycle(connectionManager);

        log.info("[BABY-MQTT] MQTT ConnectionLifecycle 创建完成");

        return lifecycle;
    }

    /**
     * 创建 MQTT Template。
     */
    @Bean
    @ConditionalOnMissingBean
    public BabyMqttTemplate babyMqttTemplate(BabyMqttProperties properties, MqttPahoClientFactory clientFactory) {
        return new BabyMqttTemplate(properties, clientFactory);
    }

    /**
     * 校验 MQTT 配置。
     */
    void validateProperties(BabyMqttProperties properties) {

        if (properties == null) {

            log.error("[BABY-MQTT] MQTT 配置不能为空");

            throw new BabyException("BABY-0002", "MQTT 配置不能为空");
        }

        if (properties.getBroker() == null || properties.getBroker().trim().isEmpty()) {

            log.error("[BABY-MQTT] MQTT broker 不能为空");

            throw new BabyException("BABY-0002", "MQTT broker 不能为空");
        }

        if (properties.getSubscriptions() == null || properties.getSubscriptions().isEmpty()) {

            log.error("[BABY-MQTT] MQTT topics 不能为空");

            throw new BabyException("BABY-0002", "MQTT topics 不能为空");
        }

        for (MqttSubscription subscription : properties.getSubscriptions()) {

            if (subscription == null) {

                log.error("[BABY-MQTT] MQTT subscription 不能为空");

                throw new BabyException("BABY-0002", "MQTT subscription 不能为空");
            }

            if (subscription.getTopic() == null || subscription.getTopic().trim().isEmpty()) {

                log.error("[BABY-MQTT] MQTT subscription topic 不能为空");

                throw new BabyException("BABY-0002", "MQTT subscription topic 不能为空");
            }

            if (subscription.getHandler() == null || subscription.getHandler().trim().isEmpty()) {

                log.error("[BABY-MQTT] MQTT subscription handler 不能为空");

                throw new BabyException("BABY-0002", "MQTT subscription handler 不能为空");
            }
        }

        String matcherType = properties.getMatcherType();

        if (!"list".equalsIgnoreCase(matcherType) && !"trie".equalsIgnoreCase(matcherType)) {

            log.error("[BABY-MQTT] 不支持的 MQTT Matcher 类型, type={}", matcherType);

            throw new BabyException("BABY-0002", "不支持的 MQTT Matcher 类型: " + matcherType);
        }

        if (properties.getConnectionTimeout() <= 0) {

            log.error("[BABY-MQTT] MQTT connection-timeout 必须大于 0");

            throw new BabyException("BABY-0002", "MQTT connection-timeout 必须大于 0");
        }

        if (properties.getKeepAlive() <= 0) {

            log.error("[BABY-MQTT] MQTT keep-alive 必须大于 0");

            throw new BabyException("BABY-0002", "MQTT keep-alive 必须大于 0");
        }
    }
}