package baby.top.mqtt.client;

import baby.top.mqtt.connection.BabyMqttConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;

/**
 * Baby MQTT 连接生命周期。
 *
 * <p>
 * 负责在 MQTT Inbound Adapter 真正启动之前，
 * 将连接状态设置为 CONNECTING。
 *
 * <p>
 * MQTT 的实际连接动作仍然由
 * MqttPahoMessageDrivenChannelAdapter 负责，
 * 本类不直接操作 Paho Client。
 */
@Slf4j
public class BabyMqttConnectionLifecycle implements SmartLifecycle {

    /**
     * 连接状态管理器。
     */
    private final BabyMqttConnectionManager connectionManager;

    /**
     * 是否已经启动。
     */
    private volatile boolean running;

    public BabyMqttConnectionLifecycle(BabyMqttConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * Spring 生命周期启动。
     *
     * <p>
     * 这里不负责真正建立 MQTT 连接，
     * 只负责在 MQTT Adapter 启动之前标记为 CONNECTING。
     */
    @Override
    public void start() {
        if (running) {
            return;
        }
        connectionManager.connecting();
        running = true;
    }

    /**
     * Spring 生命周期停止。
     *
     * <p>
     * MQTT Adapter 自己负责真正断开连接，
     * 这里暂时不修改连接状态。
     */
    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Spring 启动时自动执行。
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * 控制 Spring SmartLifecycle 启动顺序。
     *
     * <p>
     * phase 越小越早启动。
     * MQTT Adapter 默认使用正常的生命周期 phase，
     * 因此这里使用较小的值，让状态管理先进入 CONNECTING。
     */
    @Override
    public int getPhase() {
        return -100;
    }
}