package baby.top.mqtt.matcher;

import baby.top.mqtt.handler.MqttHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MQTT Topic Trie 匹配器
 * <p>
 * 支持：
 * +：单层通配符
 * #：多层通配符，只允许出现在 Topic 最后一层
 * <p>
 * 例如：
 * produce/+/robot_sleep_mode/+
 * produce/2310/robot_sleep_mode/BS10L_R01
 */
public class TrieMqttTopicMatcher implements MqttTopicMatcher {

    private final Node root = new Node();

    /**
     * 注册的 Topic-Handler 映射数量
     */
    private int handlerCount = 0;

    @Override
    public void register(
            String topic,
            MqttHandler handler) {

        if (topic == null
                || topic.length() == 0
                || handler == null) {
            return;
        }

        String[] levels =
                topic.split("/", -1);

        Node current = root;

        for (String level : levels) {

            /**
             * # 多层通配符
             */
            if ("#".equals(level)) {

                if (current.hashNode == null) {
                    current.hashNode = new Node();
                }

                current = current.hashNode;

                // # 必须是最后一层
                break;
            }

            /**
             * + 单层通配符
             */
            if ("+".equals(level)) {

                if (current.plusNode == null) {
                    current.plusNode = new Node();
                }

                current = current.plusNode;

                continue;
            }

            /**
             * 普通 Topic 层级
             */
            Node next =
                    current.children.get(level);

            if (next == null) {
                next = new Node();
                current.children.put(level, next);
            }

            current = next;
        }

        current.handlers.add(handler);

        handlerCount++;
    }

    @Override
    public List<MqttHandler> findHandlers(
            String topic) {

        List<MqttHandler> result =
                new ArrayList<MqttHandler>();

        if (topic == null
                || topic.length() == 0) {
            return result;
        }

        String[] levels =
                topic.split("/", -1);

        find(
                root,
                levels,
                0,
                result
        );

        return result;
    }

    /**
     * Trie 递归匹配
     */
    private void find(
            Node node,
            String[] topicLevels,
            int index,
            List<MqttHandler> result) {

        if (node == null) {
            return;
        }

        /**
         * Topic 已经匹配完成
         */
        if (index == topicLevels.length) {

            // 精确匹配
            result.addAll(node.handlers);

            // # 可以匹配剩余任意层
            if (node.hashNode != null) {
                result.addAll(
                        node.hashNode.handlers
                );
            }

            return;
        }

        String currentLevel =
                topicLevels[index];

        /**
         * 1. 普通字符串匹配
         */
        Node normalNode =
                node.children.get(currentLevel);

        if (normalNode != null) {

            find(
                    normalNode,
                    topicLevels,
                    index + 1,
                    result
            );
        }

        /**
         * 2. + 单层通配符匹配
         */
        if (node.plusNode != null) {

            find(
                    node.plusNode,
                    topicLevels,
                    index + 1,
                    result
            );
        }

        /**
         * 3. # 多层通配符匹配
         */
        if (node.hashNode != null) {

            result.addAll(
                    node.hashNode.handlers
            );
        }
    }

    @Override
    public int size() {
        return handlerCount;
    }

    /**
     * Trie 节点
     */
    private static class Node {

        /**
         * 普通 Topic 层级
         */
        private final Map<String, Node> children =
                new HashMap<String, Node>();
        /**
         * 当前节点对应的 Handler
         */
        private final List<MqttHandler> handlers =
                new ArrayList<MqttHandler>();
        /**
         * + 单层通配符节点
         */
        private Node plusNode;
        /**
         * # 多层通配符节点
         */
        private Node hashNode;
    }
}