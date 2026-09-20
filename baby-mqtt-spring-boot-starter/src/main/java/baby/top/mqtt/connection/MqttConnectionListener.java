package baby.top.mqtt.connection;

/**
 * MQTT 连接状态监听器。
 *
 * @author baby
 */
public interface MqttConnectionListener {

    /**
     * MQTT 连接成功。
     *
     * @param event 连接事件
     */
    void onConnected(MqttConnectionEvent event);

    /**
     * MQTT 连接断开。
     *
     * @param event 连接事件
     */
    void onDisconnected(MqttConnectionEvent event);

    /**
     * MQTT 连接失败。
     *
     * @param event 连接事件
     */
    void onConnectFailed(MqttConnectionEvent event);
}