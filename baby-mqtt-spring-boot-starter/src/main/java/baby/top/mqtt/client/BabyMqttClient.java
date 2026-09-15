package baby.top.mqtt.client;

import baby.top.mqtt.properties.BabyMqttProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

/**
 * Baby MQTT Client。
 * <p>
 * 负责创建和管理 Spring Integration MQTT 使用的 Paho Client Factory。
 *
 * @author baby
 */
@Slf4j
public class BabyMqttClient {

    private final BabyMqttProperties properties;

    private MqttPahoClientFactory clientFactory;

    public BabyMqttClient(BabyMqttProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化 MQTT Client Factory。
     */
    public void init() {

        if (clientFactory != null) {
            return;
        }

        DefaultMqttPahoClientFactory factory =
                new DefaultMqttPahoClientFactory();

        MqttConnectOptions options =
                new MqttConnectOptions();

        options.setServerURIs(
                new String[]{properties.getBroker()}
        );

        options.setAutomaticReconnect(
                properties.isAutoReconnect()
        );

        options.setConnectionTimeout(
                properties.getConnectionTimeout()
        );

        options.setKeepAliveInterval(
                properties.getKeepAlive()
        );

        if (properties.getUsername() != null
                && properties.getUsername().length() > 0) {

            options.setUserName(
                    properties.getUsername()
            );
        }

        if (properties.getPassword() != null
                && properties.getPassword().length() > 0) {

            options.setPassword(
                    properties.getPassword().toCharArray()
            );
        }

        factory.setConnectionOptions(options);

        this.clientFactory = factory;

        log.info(
                "Baby MQTT Paho Client 初始化成功，broker: {}",
                properties.getBroker()
        );
    }

    /**
     * 获取 Paho Client Factory。
     */
    public MqttPahoClientFactory getClientFactory() {

        if (clientFactory == null) {
            init();
        }

        return clientFactory;
    }

    /**
     * 获取 MQTT Broker 地址。
     */
    public String getBroker() {
        return properties.getBroker();
    }
}