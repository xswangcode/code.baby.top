package baby.top.core.common;

/**
 * Baby 组件状态。
 *
 * @author baby
 */
public enum ComponentState {

    /**
     * 新建
     */
    NEW,

    /**
     * 启动中
     */
    STARTING,

    /**
     * 运行中
     */
    RUNNING,

    /**
     * 停止中
     */
    STOPPING,

    /**
     * 已停止
     */
    STOPPED,

    /**
     * 启动或运行失败
     */
    FAILED
}