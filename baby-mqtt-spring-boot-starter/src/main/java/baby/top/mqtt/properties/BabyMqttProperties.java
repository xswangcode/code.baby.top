package baby.top.mqtt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "baby.mqtt")
public class BabyMqttProperties {

    private boolean enabled = false;

    private String broker;

    private String username;

    private String password;

    private String clientId;

    private boolean autoReconnect = true;

    private int connectionTimeout = 10;

    private int keepAlive = 60;

    /**
     * MQTT 订阅配置。
     * <p>
     * Topic 与 Handler 通过配置文件关联。
     */
    private List<MqttSubscription> subscriptions = new ArrayList<>();

    /**
     * list：List 匹配
     * trie：Trie 匹配
     */
    private String matcherType = "list";
}