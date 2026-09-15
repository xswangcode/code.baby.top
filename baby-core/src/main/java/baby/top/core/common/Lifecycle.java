package baby.top.core.common;

/**
 * 组件生命周期接口。
 *
 * @author babynan
 */
public interface Lifecycle {

    /**
     * 启动组件。
     */
    void start();

    /**
     * 停止组件。
     */
    void stop();

    /**
     * 判断组件是否正在运行。
     *
     * @return true 正在运行
     */
    boolean isRunning();
}