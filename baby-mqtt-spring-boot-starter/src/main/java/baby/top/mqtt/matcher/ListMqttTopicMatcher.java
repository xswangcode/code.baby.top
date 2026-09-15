package baby.top.mqtt.matcher;

import baby.top.mqtt.handler.MqttHandler;

import java.util.ArrayList;
import java.util.List;

public class ListMqttTopicMatcher implements MqttTopicMatcher {

    private final List<HandlerMapping> mappings = new ArrayList<HandlerMapping>();

    @Override
    public void register(String topic, MqttHandler handler) {

        if (topic == null || topic.isEmpty() || handler == null) {
            return;
        }

        mappings.add(new HandlerMapping(topic, handler));
    }

    @Override
    public List<MqttHandler> findHandlers(String topic) {

        List<MqttHandler> result = new ArrayList<MqttHandler>();

        if (topic == null || topic.isEmpty()) {
            return result;
        }

        for (HandlerMapping mapping : mappings) {

            if (match(mapping.getTopic(), topic)) {

                result.add(mapping.getHandler());
            }
        }

        return result;
    }

    private boolean match(String pattern, String topic) {

        if (pattern == null || topic == null || pattern.length() == 0 || topic.length() == 0) {

            return false;
        }

        String[] patternLevels = pattern.split("/", -1);

        String[] topicLevels = topic.split("/", -1);

        int patternIndex = 0;
        int topicIndex = 0;

        while (patternIndex < patternLevels.length) {

            String patternLevel = patternLevels[patternIndex];

            if ("#".equals(patternLevel)) {
                return patternIndex == patternLevels.length - 1;
            }

            if (topicIndex >= topicLevels.length) {
                return false;
            }

            if (!"+".equals(patternLevel) && !patternLevel.equals(topicLevels[topicIndex])) {

                return false;
            }

            patternIndex++;
            topicIndex++;
        }

        return topicIndex == topicLevels.length;
    }

    @Override
    public int size() {
        return mappings.size();
    }

    private static class HandlerMapping {

        private final String topic;

        private final MqttHandler handler;

        private HandlerMapping(String topic, MqttHandler handler) {

            this.topic = topic;
            this.handler = handler;
        }

        public String getTopic() {
            return topic;
        }

        public MqttHandler getHandler() {
            return handler;
        }
    }
}