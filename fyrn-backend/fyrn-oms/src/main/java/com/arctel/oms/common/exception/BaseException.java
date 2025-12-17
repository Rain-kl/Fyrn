package com.arctel.oms.common.exception;

import com.arctel.oms.common.constants.ExceptionLogTypeEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Getter
@Setter
public class BaseException extends RuntimeException {
    private String returnCode = "";
    private String errorCode = "-1";
    private String errorMessage = "";

    public BaseException(String errorCode) {
        this.errorCode = errorCode;
    }

    public BaseException(String errorCode, String... messages) {
        this.errorCode = errorCode;
        this.errorMessage = StringUtils.join(messages, " ");
    }

    public BaseException(String errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public BaseException(String errorCode, Throwable cause, String... messages) {
        super(cause);
        this.errorCode = errorCode;
        this.errorMessage = StringUtils.join(messages, " ");
    }

    public void setErrorMessage(String... messages) {
        this.errorMessage = StringUtils.join(messages, " ");
    }


    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("[" + this.errorCode + "]");
        if (!StringUtils.isEmpty(this.errorMessage)) {
            sb.append("[").append(this.errorMessage).append("]");
        } else if (!StringUtils.isEmpty(super.getMessage())) {
            sb.append("[").append(super.getMessage()).append("]");
        }
        return sb.toString();
    }
}