package baby.top.mqtt.connection;

/**
 * MQTT 连接状态。
 */
public enum MqttConnectionState {

    /**
     * 初始状态
     */
    DISCONNECTED,

    /**
     * 正在连接
     */
    CONNECTING,

    /**
     * 已连接
     */
    CONNECTED,

    /**
     * 正在断开
     */
    DISCONNECTING,

    /**
     * 连接失败
     */
    FAILED
}