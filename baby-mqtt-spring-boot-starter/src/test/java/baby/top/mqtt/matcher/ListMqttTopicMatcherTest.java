package baby.top.mqtt.matcher;

import baby.top.mqtt.handler.MqttHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ListMqttTopicMatcherTest {

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
    public void testExactTopic() {

        ListMqttTopicMatcher matcher =
                new ListMqttTopicMatcher();

        MqttHandler handler =
                createHandler("testHandler");

        matcher.register(
                "produce/2310/robot_sleep_mode/BS10L_R01",
                handler
        );

        List<MqttHandler> result =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertEquals(
                1,
                result.size()
        );

        Assertions.assertEquals(
                "testHandler",
                result.get(0).getName()
        );
    }

    @Test
    public void testPlusWildcard() {

        ListMqttTopicMatcher matcher =
                new ListMqttTopicMatcher();

        MqttHandler handler =
                createHandler("testHandler");

        matcher.register(
                "produce/+/robot_sleep_mode/+",
                handler
        );

        List<MqttHandler> result =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertEquals(
                1,
                result.size()
        );
    }

    @Test
    public void testPlusWildcardNotMatch() {

        ListMqttTopicMatcher matcher =
                new ListMqttTopicMatcher();

        MqttHandler handler =
                createHandler("testHandler");

        matcher.register(
                "produce/+/robot_sleep_mode/+",
                handler
        );

        List<MqttHandler> result =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode"
                );

        Assertions.assertTrue(
                result.isEmpty()
        );
    }

    @Test
    public void testHashWildcard() {

        ListMqttTopicMatcher matcher =
                new ListMqttTopicMatcher();

        MqttHandler handler =
                createHandler("testHandler");

        matcher.register(
                "produce/+/alarm/#",
                handler
        );

        List<MqttHandler> result =
                matcher.findHandlers(
                        "produce/2310/alarm/device/001"
                );

        Assertions.assertEquals(
                1,
                result.size()
        );
    }

    @Test
    public void testHashWildcardZeroLevel() {

        ListMqttTopicMatcher matcher =
                new ListMqttTopicMatcher();

        MqttHandler handler =
                createHandler("testHandler");

        matcher.register(
                "produce/+/alarm/#",
                handler
        );

        List<MqttHandler> result =
                matcher.findHandlers(
                        "produce/2310/alarm"
                );

        Assertions.assertEquals(
                1,
                result.size()
        );
    }

    @Test
    public void testMultipleHandlers() {

        ListMqttTopicMatcher matcher =
                new ListMqttTopicMatcher();

        MqttHandler handler1 =
                createHandler("handler1");

        MqttHandler handler2 =
                createHandler("handler2");

        matcher.register(
                "produce/+/robot_sleep_mode/+",
                handler1
        );

        matcher.register(
                "produce/+/robot_sleep_mode/+",
                handler2
        );

        List<MqttHandler> result =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertEquals(
                2,
                result.size()
        );
    }

    @Test
    public void testDifferentTopicNotMatch() {

        ListMqttTopicMatcher matcher =
                new ListMqttTopicMatcher();

        MqttHandler handler =
                createHandler("testHandler");

        matcher.register(
                "produce/+/robot_sleep_mode/+",
                handler
        );

        List<MqttHandler> result =
                matcher.findHandlers(
                        "produce/2310/energy_consumption"
                );

        Assertions.assertTrue(
                result.isEmpty()
        );
    }

    @Test
    public void testSize() {

        ListMqttTopicMatcher matcher =
                new ListMqttTopicMatcher();

        MqttHandler handler1 =
                createHandler("handler1");

        MqttHandler handler2 =
                createHandler("handler2");

        matcher.register(
                "topic/1",
                handler1
        );

        matcher.register(
                "topic/2",
                handler2
        );

        Assertions.assertEquals(
                2,
                matcher.size()
        );
    }
}