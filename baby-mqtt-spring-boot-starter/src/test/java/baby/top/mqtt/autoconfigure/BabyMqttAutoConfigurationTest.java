package baby.top.mqtt.autoconfigure;

import baby.top.core.exception.BabyException;
import baby.top.mqtt.properties.BabyMqttProperties;
import baby.top.mqtt.properties.MqttSubscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class BabyMqttAutoConfigurationTest {

    private BabyMqttAutoConfiguration configuration;

    private BabyMqttProperties properties;

    @BeforeEach
    public void setUp() {


        properties =
                new BabyMqttProperties();

        properties.setBroker(
                "tcp://127.0.0.1:1883"
        );


        MqttSubscription subscription =
                new MqttSubscription();

        subscription.setName(
                "testSubscription"
        );

        subscription.setTopic(
                "produce/+/test/+"
        );

        subscription.setHandler(
                "testHandler"
        );

        ArrayList<MqttSubscription> subscriptions =
                new ArrayList<MqttSubscription>();

        subscriptions.add(subscription);

        properties.setSubscriptions(
                subscriptions
        );
        configuration =
                new BabyMqttAutoConfiguration(properties);
    }

    @Test
    public void testValidConfig() {

        configuration.validateProperties(
                properties
        );
    }

    @Test
    public void testBrokerEmpty() {

        properties.setBroker("");

        Assertions.assertThrows(
                BabyException.class,
                () -> configuration.validateProperties(
                        properties
                )
        );
    }

    @Test
    public void testSubscriptionsEmpty() {

        properties.setSubscriptions(
                new ArrayList<MqttSubscription>()
        );

        Assertions.assertThrows(
                BabyException.class,
                () -> configuration.validateProperties(
                        properties
                )
        );
    }

    @Test
    public void testSubscriptionNull() {

        ArrayList<MqttSubscription> subscriptions =
                new ArrayList<MqttSubscription>();

        subscriptions.add(null);

        properties.setSubscriptions(
                subscriptions
        );

        Assertions.assertThrows(
                BabyException.class,
                () -> configuration.validateProperties(
                        properties
                )
        );
    }

    @Test
    public void testSubscriptionTopicEmpty() {

        properties.getSubscriptions()
                .get(0)
                .setTopic("");

        Assertions.assertThrows(
                BabyException.class,
                () -> configuration.validateProperties(
                        properties
                )
        );
    }

    @Test
    public void testSubscriptionHandlerEmpty() {

        properties.getSubscriptions()
                .get(0)
                .setHandler("");

        Assertions.assertThrows(
                BabyException.class,
                () -> configuration.validateProperties(
                        properties
                )
        );
    }

    @Test
    public void testInvalidMatcherType() {

        properties.setMatcherType(
                "abc"
        );

        Assertions.assertThrows(
                BabyException.class,
                () -> configuration.validateProperties(
                        properties
                )
        );
    }

    @Test
    public void testConnectionTimeoutInvalid() {

        properties.setConnectionTimeout(0);

        Assertions.assertThrows(
                BabyException.class,
                () -> configuration.validateProperties(
                        properties
                )
        );
    }

    @Test
    public void testKeepAliveInvalid() {

        properties.setKeepAlive(0);

        Assertions.assertThrows(
                BabyException.class,
                () -> configuration.validateProperties(
                        properties
                )
        );
    }
}