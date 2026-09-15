package baby.top.core.utils;

import baby.top.core.exception.BabyErrorCode;
import baby.top.core.exception.BabyException;

/**
 * Babynan 公共参数断言工具。
 *
 * @author babynan
 */
public final class BabyAssert {

    private BabyAssert() {
    }

    /**
     * 判断对象不能为空。
     *
     * @param object  对象
     * @param message 异常信息
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new BabyException(
                    BabyErrorCode.INVALID_ARGUMENT,
                    message
            );
        }
    }

    /**
     * 判断字符串不能为空。
     *
     * @param value   字符串
     * @param message 异常信息
     */
    public static void notEmpty(String value, String message) {
        if (value == null || value.length() == 0) {
            throw new BabyException(
                    BabyErrorCode.INVALID_ARGUMENT,
                    message
            );
        }
    }

    /**
     * 判断条件必须成立。
     *
     * @param condition 条件
     * @param message   异常信息
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new BabyException(
                    BabyErrorCode.INVALID_ARGUMENT,
                    message
            );
        }
    }
}