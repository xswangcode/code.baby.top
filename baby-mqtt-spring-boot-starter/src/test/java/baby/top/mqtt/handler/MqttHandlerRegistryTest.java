package baby.top.mqtt.handler;

import baby.top.core.exception.BabyException;
import baby.top.mqtt.matcher.ListMqttTopicMatcher;
import baby.top.mqtt.matcher.MqttTopicMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MqttHandlerRegistryTest {

    private MqttHandler createHandler(
            final String name) {

        return new MqttHandler() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public void handle(
                    String topic,
                    String payload) {
            }
        };
    }

    @Test
    public void testRegisterSuccess() {

        MqttTopicMatcher matcher =
                new ListMqttTopicMatcher();

        MqttHandlerRegistry registry =
                new MqttHandlerRegistry(matcher);

        MqttHandler handler =
                createHandler("testHandler");

        registry.register(handler);

        Assertions.assertEquals(
                0,
                registry.size()
        );

        Assertions.assertSame(
                handler,
                registry.getHandler(
                        "testHandler"
                )
        );
    }

    @Test
    public void testDuplicateHandlerName() {

        MqttHandlerRegistry registry =
                new MqttHandlerRegistry(
                        new ListMqttTopicMatcher()
                );

        registry.register(
                createHandler("testHandler")
        );

        Assertions.assertThrows(
                BabyException.class,
                () -> registry.register(
                        createHandler("testHandler")
                )
        );
    }

    @Test
    public void testRegisterSubscription() {

        MqttHandlerRegistry registry =
                new MqttHandlerRegistry(
                        new ListMqttTopicMatcher()
                );

        MqttHandler handler =
                createHandler("testHandler");

        registry.register(handler);

        registry.registerSubscription(
                "produce/+/robot_sleep_mode/+",
                "testHandler"
        );

        Assertions.assertEquals(
                1,
                registry.size()
        );

        List<MqttHandler> handlers =
                registry.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertEquals(
                1,
                handlers.size()
        );

        Assertions.assertEquals(
                "testHandler",
                handlers.get(0).getName()
        );
    }

    @Test
    public void testMultipleSubscriptionsForSameHandler() {

        MqttHandlerRegistry registry =
                new MqttHandlerRegistry(
                        new ListMqttTopicMatcher()
                );

        MqttHandler handler =
                createHandler("testHandler");

        registry.register(handler);

        registry.registerSubscription(
                "produce/+/robot_sleep_mode/+",
                "testHandler"
        );

        registry.registerSubscription(
                "produce/+/robot_sleep_mode_v2/+",
                "testHandler"
        );

        Assertions.assertEquals(
                2,
                registry.size()
        );

        Assertions.assertEquals(
                1,
                registry.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                ).size()
        );

        Assertions.assertEquals(
                1,
                registry.findHandlers(
                        "produce/2310/robot_sleep_mode_v2/BS10L_R01"
                ).size()
        );
    }

    @Test
    public void testRegisterSubscriptionHandlerNotFound() {

        MqttHandlerRegistry registry =
                new MqttHandlerRegistry(
                        new ListMqttTopicMatcher()
                );

        Assertions.assertThrows(
                BabyException.class,
                () -> registry.registerSubscription(
                        "produce/+/robot_sleep_mode/+",
                        "notExists"
                )
        );
    }

    @Test
    public void testRegisterSubscriptionTopicEmpty() {

        MqttHandlerRegistry registry =
                new MqttHandlerRegistry(
                        new ListMqttTopicMatcher()
                );

        registry.register(
                createHandler("testHandler")
        );

        Assertions.assertThrows(
                BabyException.class,
                () -> registry.registerSubscription(
                        "",
                        "testHandler"
                )
        );
    }

    @Test
    public void testRegisterSubscriptionHandlerEmpty() {

        MqttHandlerRegistry registry =
                new MqttHandlerRegistry(
                        new ListMqttTopicMatcher()
                );

        registry.register(
                createHandler("testHandler")
        );

        Assertions.assertThrows(
                BabyException.class,
                () -> registry.registerSubscription(
                        "produce/test",
                        ""
                )
        );
    }
}