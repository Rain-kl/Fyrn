package net.arctel.framework.utils;

import net.arctel.framework.exception.BizException;
import org.apache.commons.lang3.StringUtils;

public class ThrowUtils {

    public static void throwIf(boolean condition, Exception exception) throws Exception {
        if (condition) {
            throw exception;
        }
    }

    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BizException(message);
        }
    }

    public static void throwIf(boolean condition, String errorCode, String message) {
        if (condition) {
            throw new BizException(errorCode, message);
        }
    }

    public static void throwIfNull(Object obj, Exception exception) throws Exception {
        if (obj == null) {
            throw exception;
        }
    }

    public static void throwIfNull(Object obj, String message) {
        if (obj == null) {
            throw new BizException(message);
        }
    }

    public static void throwIfNull(Object obj, String errorCode, String message) {
        if (obj == null) {
            throw new BizException(errorCode, message);
        }
    }

    public static void throwIfEmpty(String str, String message) {
        if (StringUtils.isBlank(str)) {
            throw new BizException(message);
        }
    }

    public static void throwIfEmpty(String str, String errorCode, String message) {
        if (StringUtils.isBlank(str)) {
            throw new BizException(errorCode, message);
        }
    }
}
