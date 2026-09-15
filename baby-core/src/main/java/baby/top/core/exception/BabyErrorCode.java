package baby.top.core.exception;

/**
 * Baby公共错误码。
 *
 * @author baby
 */
public final class BabyErrorCode {

    /**
     * 未知异常
     */
    public static final String UNKNOWN = "BABYNAN-0000";
    /**
     * 参数错误
     */
    public static final String INVALID_ARGUMENT = "BABYNAN-0001";
    /**
     * 配置错误
     */
    public static final String CONFIG_ERROR = "BABYNAN-0002";
    /**
     * 连接错误
     */
    public static final String CONNECTION_ERROR = "BABYNAN-0003";
    /**
     * 超时
     */
    public static final String TIMEOUT = "BABYNAN-0004";
    /**
     * 操作失败
     */
    public static final String OPERATION_FAILED = "BABYNAN-0005";

    private BabyErrorCode() {
    }
}