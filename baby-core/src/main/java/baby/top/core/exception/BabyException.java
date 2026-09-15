package baby.top.core.exception;

/**
 * Baby 公共运行时异常。
 *
 * @author baby
 */
public class BabyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final String code;

    public BabyException(String message) {
        super(message);
        this.code = null;
    }

    public BabyException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BabyException(String message, Throwable cause) {
        super(message, cause);
        this.code = null;
    }

    public BabyException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}