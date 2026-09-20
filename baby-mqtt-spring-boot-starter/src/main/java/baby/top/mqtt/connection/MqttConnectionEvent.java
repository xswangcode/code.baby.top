package baby.top.mqtt.connection;

/**
 * MQTT 连接事件。
 *
 * @author baby
 */
public class MqttConnectionEvent {

    /**
     * MQTT Broker 地址
     */
    private final String broker;

    /**
     * 连接状态
     */
    private final MqttConnectionState state;

    /**
     * 异常
     */
    private final Throwable cause;

    public MqttConnectionEvent(String broker, MqttConnectionState state, Throwable cause) {

        this.broker = broker;
        this.state = state;
        this.cause = cause;
    }

    public String getBroker() {
        return broker;
    }

    public MqttConnectionState getState() {
        return state;
    }

    public Throwable getCause() {
        return cause;
    }
}