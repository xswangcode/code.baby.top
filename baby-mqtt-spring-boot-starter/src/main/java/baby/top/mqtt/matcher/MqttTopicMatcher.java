package baby.top.mqtt.matcher;

import baby.top.mqtt.handler.MqttHandler;

import java.util.List;

public interface MqttTopicMatcher {


    /**
     * 注册 Topic 与 Handler。
     */
    void register(String topic, MqttHandler handler);

    /**
     * 根据 Topic 查找匹配的 Handler。
     */
    List<MqttHandler> findHandlers(String topic);

    /**
     * 获取已注册 Handler 数量。
     */
    int size();
}