package baby.top.mqtt.connection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.mqtt.core.MqttPahoComponent;
import org.springframework.integration.mqtt.event.MqttConnectionFailedEvent;
import org.springframework.integration.mqtt.event.MqttIntegrationEvent;
import org.springframework.integration.mqtt.event.MqttSubscribedEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Baby MQTT 默认连接管理器。
 *
 * <p>
 * 通过监听 Spring Integration MQTT 连接事件，
 * 管理 MQTT 当前连接状态，并通知业务监听器。
 * </p>
 *
 * <p>
 * 连接状态主要包括：
 * <ul>
 *     <li>DISCONNECTED：未连接</li>
 *     <li>CONNECTING：正在连接</li>
 *     <li>CONNECTED：已连接</li>
 *     <li>FAILED：本次连接尝试失败</li>
 * </ul>
 * </p>
 *
 * @author baby
 */
@Slf4j
public class DefaultBabyMqttConnectionManager implements BabyMqttConnectionManager, ApplicationListener<MqttIntegrationEvent> {

    /**
     * 连接监听器集合。
     *
     * <p>
     * 使用 CopyOnWriteArrayList，保证 MQTT 事件线程通知监听器时的线程安全。
     * </p>
     */
    private final List<MqttConnectionListener> listeners = new CopyOnWriteArrayList<MqttConnectionListener>();

    /**
     * 当前连接状态。
     *
     * <p>
     * volatile 保证多线程环境下状态的可见性。
     * </p>
     */
    private volatile MqttConnectionState state = MqttConnectionState.DISCONNECTED;

    @Override
    public MqttConnectionState getState() {
        return state;
    }

    @Override
    public boolean isConnected() {
        return state == MqttConnectionState.CONNECTED;
    }

    /**
     * 标记开始连接 MQTT。
     *
     * <p>
     * 由 BabyMqttConnectionLifecycle 在 MQTT Inbound Adapter
     * 启动之前调用。
     * </p>
     */
    @Override
    public void connecting() {

        MqttConnectionState oldState = this.state;

        if (oldState == MqttConnectionState.CONNECTING) {
            return;
        }

        this.state = MqttConnectionState.CONNECTING;

        log.info("Baby MQTT 开始连接，state: {} -> {}", oldState, MqttConnectionState.CONNECTING);
    }

    @Override
    public void addListener(MqttConnectionListener listener) {

        if (listener == null) {
            return;
        }

        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(MqttConnectionListener listener) {

        if (listener == null) {
            return;
        }

        listeners.remove(listener);
    }

    /**
     * 接收 Spring Integration MQTT 事件。
     *
     * <p>
     * 当前主要处理：
     * <ul>
     *     <li>MqttSubscribedEvent：MQTT 连接并完成订阅</li>
     *     <li>MqttConnectionFailedEvent：连接失败或连接断开</li>
     * </ul>
     * </p>
     */
    @Override
    public void onApplicationEvent(MqttIntegrationEvent event) {

        if (event == null) {
            return;
        }

        if (event instanceof MqttSubscribedEvent) {
            handleConnected(event);
            return;
        }

        if (event instanceof MqttConnectionFailedEvent) {
            handleConnectionFailed((MqttConnectionFailedEvent) event);
        }
    }

    /**
     * 处理 MQTT 连接成功事件。
     */
    private void handleConnected(MqttIntegrationEvent event) {

        String broker = getBroker(event);

        MqttConnectionState oldState = this.state;

        this.state = MqttConnectionState.CONNECTED;

        log.info("Baby MQTT 连接成功，broker: {}，state: {} -> {}", broker, oldState, MqttConnectionState.CONNECTED);

        MqttConnectionEvent connectionEvent = new MqttConnectionEvent(broker, MqttConnectionState.CONNECTED, null);

        notifyConnected(connectionEvent);
    }

    /**
     * 处理 MQTT 连接失败事件。
     *
     * <p>
     * 根据当前状态区分：
     * </p>
     *
     * <ul>
     *     <li>
     *         CONNECTED -> DISCONNECTED：
     *         已经连接成功后发生连接异常。
     *     </li>
     *     <li>
     *         其他状态 -> FAILED：
     *         当前连接尝试失败。
     *     </li>
     * </ul>
     */
    private void handleConnectionFailed(MqttConnectionFailedEvent event) {

        String broker = getBroker(event);

        /*
         * 已经连接成功后又发生连接异常，
         * 按当前设计进入 DISCONNECTED。
         */
        if (state == MqttConnectionState.CONNECTED) {

            MqttConnectionState oldState = this.state;

            this.state = MqttConnectionState.DISCONNECTED;

            log.warn("Baby MQTT 连接断开，broker: {}，state: {} -> {}", broker, oldState, MqttConnectionState.DISCONNECTED, event.getCause());

            MqttConnectionEvent connectionEvent = new MqttConnectionEvent(broker, MqttConnectionState.DISCONNECTED, event.getCause());

            notifyDisconnected(connectionEvent);

            return;
        }

        /*
         * CONNECTING 状态下连接失败，
         * 表示本次连接尝试失败，进入 FAILED。
         */
        MqttConnectionState oldState = this.state;

        this.state = MqttConnectionState.FAILED;

        log.error("Baby MQTT 连接失败，broker: {}，state: {} -> {}", broker, oldState, MqttConnectionState.FAILED, event.getCause());

        MqttConnectionEvent connectionEvent = new MqttConnectionEvent(broker, MqttConnectionState.FAILED, event.getCause());

        notifyConnectFailed(connectionEvent);
    }

    /**
     * 获取 MQTT Broker 地址。
     */
    private String getBroker(MqttIntegrationEvent event) {

        try {

            MqttPahoComponent source = event.getSourceAsType();

            if (source == null || source.getConnectionInfo() == null) {
                return null;
            }

            String[] serverURIs = source.getConnectionInfo().getServerURIs();

            if (serverURIs == null || serverURIs.length == 0) {
                return null;
            }

            return serverURIs[0];

        } catch (Exception e) {

            log.warn("获取 MQTT Broker 地址失败", e);

            return null;
        }
    }

    /**
     * 通知连接成功监听器。
     */
    private void notifyConnected(MqttConnectionEvent event) {

        for (MqttConnectionListener listener : listeners) {

            try {

                listener.onConnected(event);

            } catch (Exception e) {

                log.error("MQTT 连接成功监听器执行异常", e);
            }
        }
    }

    /**
     * 通知连接断开监听器。
     */
    private void notifyDisconnected(MqttConnectionEvent event) {

        for (MqttConnectionListener listener : listeners) {

            try {

                listener.onDisconnected(event);

            } catch (Exception e) {

                log.error("MQTT 连接断开监听器执行异常", e);
            }
        }
    }

    /**
     * 通知连接失败监听器。
     */
    private void notifyConnectFailed(MqttConnectionEvent event) {

        for (MqttConnectionListener listener : listeners) {

            try {

                listener.onConnectFailed(event);

            } catch (Exception e) {

                log.error("MQTT 连接失败监听器执行异常", e);
            }
        }
    }
}