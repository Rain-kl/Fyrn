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

package net.arctel.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import net.arctel.platform.oms.common.constants.ErrorConstant;
import net.arctel.platform.framework.exception.BizException;
import net.arctel.platform.framework.utils.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @Valid 校验失败异常 (用于 @RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数验证失败", e);
        return Result.error(errorMessage);
    }

    /**
     * 处理 @Validated 校验失败异常 (用于普通参数)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.error("参数验证失败", e);
        return Result.error(errorMessage);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        String errorMessage = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数绑定失败", e);
        return Result.error(errorMessage);
    }

    /**
     * 处理参数非法异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数验证失败", e);
        return Result.error(e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public Result<String> handleException(BizException e) {
        log.error("业务异常", e);
        return Result.error("错误: " + e.getMessage());
    }

    /**
     * 登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<String> handlerNotLoginException(NotLoginException nle) {

        // 打印堆栈，以供调试
        log.error("系统异常", nle);

        // 判断场景值，定制化异常信息
        String message = switch (nle.getType()) {
            case NotLoginException.NOT_TOKEN -> "未能读取到有效 token";
            case NotLoginException.INVALID_TOKEN -> "token 无效";
            case NotLoginException.TOKEN_TIMEOUT -> "token 已过期";
            case NotLoginException.BE_REPLACED -> "token 已被顶下线";
            case NotLoginException.KICK_OUT -> "token 已被踢下线";
            case NotLoginException.TOKEN_FREEZE -> "token 已被冻结";
            case NotLoginException.NO_PREFIX -> "未按照指定前缀提交 token";
            default -> "当前会话未登录";
        };

        // 返回给前端
        return Result.error(ErrorConstant.UNAUTHORIZED, message);
    }


    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e);
        // 生产环境建议返回通用错误信息
        return Result.error(ErrorConstant.SYSTEM_ERROR, "系统异常，请稍后重试");
    }
}
