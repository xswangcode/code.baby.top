package baby.top.mqtt.connection;

/**
 * Baby MQTT 连接管理器。
 *
 * @author baby
 */
public interface BabyMqttConnectionManager {

    /**
     * 获取当前连接状态。
     *
     * @return 当前连接状态
     */
    MqttConnectionState getState();

    /**
     * 标记开始连接 MQTT。
     */
    void connecting();

    /**
     * 判断 MQTT 当前是否已连接。
     *
     * @return true 已连接
     */
    boolean isConnected();

    /**
     * 添加连接监听器。
     *
     * @param listener 连接监听器
     */
    void addListener(MqttConnectionListener listener);

    /**
     * 移除连接监听器。
     *
     * @param listener 连接监听器
     */
    void removeListener(MqttConnectionListener listener);
}