package baby.top.test.runner;

import baby.top.mqtt.template.BabyMqttTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * MQTT 发布压力测试。
 * Spring Boot 启动完成后自动发送指定数量的 MQTT 消息。
 */
@Slf4j
@Component
public class MqttPublishTestRunner implements CommandLineRunner {

    private final BabyMqttTemplate mqttTemplate;

    /**
     * 压力测试总发送数量
     */
    private static final int TOTAL_MESSAGES = 10000;

    /**
     * 每条消息发送间隔，单位毫秒。
     * 0 表示不主动等待，尽可能连续发送。
     */
    private static final long SEND_INTERVAL_MS = 0;

    /**
     * 每隔多少条消息输出一次进度
     */
    private static final int LOG_INTERVAL = 1000;

    private static final String TOPIC =
            "produce/2310/robot_sleep_mode/BS10L_R01";

    public MqttPublishTestRunner(BabyMqttTemplate mqttTemplate) {
        this.mqttTemplate = mqttTemplate;
    }

    @Override
    public void run(String... args) throws InterruptedException {

        log.info("========== MQTT 压力测试开始 ==========");
        log.info("Topic: {}", TOPIC);
        log.info("计划发送数量: {}", TOTAL_MESSAGES);
        log.info("发送间隔: {} ms", SEND_INTERVAL_MS);

        long startTime = System.currentTimeMillis();

        int successCount = 0;
        int failureCount = 0;

        for (int i = 1; i <= TOTAL_MESSAGES; i++) {

            String payload = "{"
                    + "\"deviceName\":\"BS10L_R01\","
                    + "\"SleepStatus\":false,"
                    + "\"time\":" + (1789380060000L + i)
                    + ",\"testSeq\":" + i
                    + "}";

            try {
                mqttTemplate.publish(TOPIC, payload, 0, false);
                successCount++;
            } catch (Exception e) {
                failureCount++;

                log.error("MQTT 发布失败，序号: {}", i, e);
            }

            if (i % LOG_INTERVAL == 0 || i == TOTAL_MESSAGES) {

                long elapsed = System.currentTimeMillis() - startTime;

                double throughput = elapsed > 0
                        ? successCount * 1000.0 / elapsed
                        : 0;

                log.info(
                        "压力测试进度: {}/{}, 成功: {}, 失败: {}, 耗时: {} ms, 平均发送速率: {} 条/秒",
                        i,
                        TOTAL_MESSAGES,
                        successCount,
                        failureCount,
                        elapsed,
                        String.format("%.2f", throughput)
                );
            }

            if (SEND_INTERVAL_MS > 0) {
                Thread.sleep(SEND_INTERVAL_MS);
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;

        double throughput = totalTime > 0
                ? successCount * 1000.0 / totalTime
                : 0;

        log.info("========== MQTT 压力测试结束 ==========");
        log.info("计划发送: {}", TOTAL_MESSAGES);
        log.info("调用成功: {}", successCount);
        log.info("调用失败: {}", failureCount);
        log.info("总耗时: {} ms", totalTime);
        log.info("平均发送速率: {} 条/秒", String.format("%.2f", throughput));
    }
}