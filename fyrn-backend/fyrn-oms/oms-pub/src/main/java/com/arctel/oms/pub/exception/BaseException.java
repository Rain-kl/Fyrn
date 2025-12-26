/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arctel.oms.pub.exception;

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