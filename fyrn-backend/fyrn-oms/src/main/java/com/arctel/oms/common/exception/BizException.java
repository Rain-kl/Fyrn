package com.arctel.oms.common.exception;

public class BizException extends BaseException {

    public BizException(String errorCode) {
        super(errorCode);
    }

    public BizException(String errorCode, String... messages) {
        super(errorCode, messages);
    }

    public BizException(String errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BizException(String errorCode, Throwable cause, String... messages) {
        super(errorCode, cause, messages);
    }
}
