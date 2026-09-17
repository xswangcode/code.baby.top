package baby.top.mqtt.matcher;

import baby.top.mqtt.handler.MqttHandler;
import baby.top.mqtt.handler.MqttMessageContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Trie MQTT Topic Matcher 测试
 */
public class TrieMqttTopicMatcherTest {

    /**
     * 测试精确匹配
     */
    @Test
    public void testExactMatch() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("handler1");

        matcher.register(
                "produce/2310/robot_sleep_mode/BS10L_R01",
                handler
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertEquals(
                1,
                handlers.size()
        );

        Assertions.assertSame(
                handler,
                handlers.get(0)
        );
    }

    /**
     * 测试精确匹配失败
     */
    @Test
    public void testExactMatchNotFound() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("handler1");

        matcher.register(
                "produce/2310/robot_sleep_mode/BS10L_R01",
                handler
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R02"
                );

        Assertions.assertTrue(
                handlers.isEmpty()
        );
    }

    /**
     * 测试 + 单层通配符
     * <p>
     * produce/+/robot_sleep_mode/+
     */
    @Test
    public void testPlusWildcard() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("handler1");

        matcher.register(
                "produce/+/robot_sleep_mode/+",
                handler
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertEquals(
                1,
                handlers.size()
        );

        Assertions.assertSame(
                handler,
                handlers.get(0)
        );
    }

    /**
     * 测试 + 不能匹配多层
     */
    @Test
    public void testPlusWildcardDoesNotMatchMultipleLevels() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("handler1");

        matcher.register(
                "produce/+/robot_sleep_mode/+",
                handler
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2310/test/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertTrue(
                handlers.isEmpty()
        );
    }

    /**
     * 测试 # 多层通配符
     * <p>
     * produce/2310/#
     */
    @Test
    public void testHashWildcard() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("handler1");

        matcher.register(
                "produce/2310/#",
                handler
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertEquals(
                1,
                handlers.size()
        );

        Assertions.assertSame(
                handler,
                handlers.get(0)
        );
    }

    /**
     * 测试 # 匹配直接下一级
     */
    @Test
    public void testHashWildcardSingleLevel() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("handler1");

        matcher.register(
                "produce/2310/#",
                handler
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2310/test"
                );

        Assertions.assertEquals(
                1,
                handlers.size()
        );
    }

    /**
     * 测试不同 Topic 可以注册不同 Handler
     */
    @Test
    public void testMultipleHandlers() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler1 =
                new TestMqttHandler("handler1");

        TestMqttHandler handler2 =
                new TestMqttHandler("handler2");

        matcher.register(
                "produce/2310/robot_sleep_mode/+",
                handler1
        );

        matcher.register(
                "produce/2310/robot_sleep_mode/BS10L_R01",
                handler2
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertEquals(
                2,
                handlers.size()
        );

        Assertions.assertTrue(
                handlers.contains(handler1)
        );

        Assertions.assertTrue(
                handlers.contains(handler2)
        );
    }

    /**
     * 测试多个 Handler 使用同一个 Topic
     */
    @Test
    public void testMultipleHandlersForSameTopic() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler1 =
                new TestMqttHandler("handler1");

        TestMqttHandler handler2 =
                new TestMqttHandler("handler2");

        matcher.register(
                "produce/2310/robot_sleep_mode/+",
                handler1
        );

        matcher.register(
                "produce/2310/robot_sleep_mode/+",
                handler2
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertEquals(
                2,
                handlers.size()
        );

        Assertions.assertTrue(
                handlers.contains(handler1)
        );

        Assertions.assertTrue(
                handlers.contains(handler2)
        );
    }

    /**
     * 测试 size
     */
    @Test
    public void testSize() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler1 =
                new TestMqttHandler("handler1");

        TestMqttHandler handler2 =
                new TestMqttHandler("handler2");

        Assertions.assertEquals(
                0,
                matcher.size()
        );

        matcher.register(
                "produce/2310/robot_sleep_mode/+",
                handler1
        );

        Assertions.assertEquals(
                1,
                matcher.size()
        );

        matcher.register(
                "produce/2310/robot_sleep_mode/#",
                handler2
        );

        Assertions.assertEquals(
                2,
                matcher.size()
        );
    }

    /**
     * 测试空 Topic
     */
    @Test
    public void testEmptyTopic() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("handler1");

        matcher.register(
                "",
                handler
        );

        Assertions.assertEquals(
                0,
                matcher.size()
        );

        List<MqttHandler> handlers =
                matcher.findHandlers("");

        Assertions.assertTrue(
                handlers.isEmpty()
        );
    }

    /**
     * 测试空 Handler
     */
    @Test
    public void testNullHandler() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        matcher.register(
                "produce/2310/#",
                null
        );

        Assertions.assertEquals(
                0,
                matcher.size()
        );
    }

    /**
     * 测试 null Topic
     */
    @Test
    public void testNullTopic() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("handler1");

        matcher.register(
                null,
                handler
        );

        Assertions.assertEquals(
                0,
                matcher.size()
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(null);

        Assertions.assertTrue(
                handlers.isEmpty()
        );
    }

    /**
     * 测试普通 Topic + + + 匹配
     */
    @Test
    public void testMultiplePlusWildcard() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("handler1");

        matcher.register(
                "produce/+/+/+",
                handler
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2310/robot/BS10L_R01"
                );

        Assertions.assertEquals(
                1,
                handlers.size()
        );
    }

    /**
     * 测试 # 不匹配错误前缀
     */
    @Test
    public void testHashWildcardNotMatch() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("handler1");

        matcher.register(
                "produce/2310/#",
                handler
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2311/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertTrue(
                handlers.isEmpty()
        );
    }

    /**
     * 测试 Handler name
     */
    @Test
    public void testHandlerName() {

        TrieMqttTopicMatcher matcher =
                new TrieMqttTopicMatcher();

        TestMqttHandler handler =
                new TestMqttHandler("robotSleepHandler");

        matcher.register(
                "produce/+/robot_sleep_mode/+",
                handler
        );

        List<MqttHandler> handlers =
                matcher.findHandlers(
                        "produce/2310/robot_sleep_mode/BS10L_R01"
                );

        Assertions.assertEquals(
                1,
                handlers.size()
        );

        Assertions.assertEquals(
                "robotSleepHandler",
                handlers.get(0).getName()
        );
    }

    /**
     * 测试 Handler
     */
    private static class TestMqttHandler
            implements MqttHandler {

        private final String name;

        private TestMqttHandler(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void handle(MqttMessageContext context) {
            // 测试类无需实际处理消息
        }
    }
}