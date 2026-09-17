package baby.top.mqtt.handler;

import baby.top.core.exception.BabyException;
import baby.top.mqtt.matcher.ListMqttTopicMatcher;
import baby.top.mqtt.matcher.MqttTopicMatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MqttHandlerRegistry {

    private final MqttTopicMatcher matcher;

    private final Map<String, MqttHandler> handlerMap = new HashMap<String, MqttHandler>();

    public MqttHandlerRegistry() {
        this(new ListMqttTopicMatcher());
    }

    public MqttHandlerRegistry(MqttTopicMatcher matcher) {

        if (matcher == null) {
            throw new BabyException("BABY-0001", "MQTT Topic Matcher 不能为空");
        }

        this.matcher = matcher;
    }

    /**
     * 注册 Handler。
     */
    public void register(MqttHandler handler) {

        if (handler == null) {
            throw new BabyException("BABY-0001", "MQTT Handler 不能为空");
        }

        if (handler.getName() == null || handler.getName().isEmpty()) {

            throw new BabyException("BABY-0001", "MQTT Handler 名称不能为空");
        }

        if (handlerMap.containsKey(handler.getName())) {

            throw new BabyException("BABY-0001", "MQTT Handler 名称重复: " + handler.getName());
        }

        handlerMap.put(handler.getName(), handler);

        log.info("注册 MQTT Handler，name: {}", handler.getName());
    }

    /**
     * 将 Topic 与已经注册的 Handler 进行关联。
     */
    public void registerSubscription(String topic, String handlerName) {

        if (topic == null || topic.isEmpty()) {

            throw new BabyException("BABY-0001", "MQTT Subscription Topic 不能为空");
        }

        if (handlerName == null || handlerName.isEmpty()) {

            throw new BabyException("BABY-0001", "MQTT Subscription Handler 不能为空");
        }

        MqttHandler handler = handlerMap.get(handlerName);

        if (handler == null) {

            throw new BabyException("BABY-0001", "未找到 MQTT Handler: " + handlerName);
        }

        matcher.register(topic, handler);

        log.info("注册 MQTT Subscription，topic: {}, handler: {}", topic, handlerName);
    }

    public List<MqttHandler> findHandlers(String topic) {

        return matcher.findHandlers(topic);
    }

    public void dispatch(MqttMessageContext context) {

        List<MqttHandler> handlers = findHandlers(context.getTopic());

        if (handlers == null || handlers.isEmpty()) {

            log.warn("未找到 MQTT Handler，topic: {}", context.getTopic());

            return;
        }

        for (MqttHandler handler : handlers) {
            try {
                handler.handle(context);
            } catch (Exception e) {
                log.error("MQTT Handler 执行失败，handler: {}, topic: {}", handler.getName(), context.getTopic(), e);
            }
        }
    }

    public int size() {
        return matcher.size();
    }

    public MqttHandler getHandler(String name) {

        return handlerMap.get(name);
    }
}