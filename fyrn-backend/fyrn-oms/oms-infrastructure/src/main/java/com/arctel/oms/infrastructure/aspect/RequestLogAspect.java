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

package com.arctel.oms.infrastructure.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Aspect
@Component
public class RequestLogAspect {

    /**
     * 敏感参数名称集合
     */
    private static final Set<String> SENSITIVE_KEYS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("password", "pwd", "token", "authorization"))
    );

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller)")
    public void controllerPointcut() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void requestMappingPointcut() {
    }

    @Around("controllerPointcut() && requestMappingPointcut()")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取当前请求的属性
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // 请求参数无法获取时，直接放行
        if (attr == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attr.getRequest();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 记录请求日志
            logRequest(joinPoint, request);
            // 继续执行目标方法
            Object result = joinPoint.proceed();
            stopWatch.stop();
            // 记录响应日志
//            logResponse(joinPoint, request, result, stopWatch);
            return result;
        } catch (Exception e) {
            stopWatch.stop();
            LoggerFactory.getLogger(joinPoint.getTarget().getClass())
                    .error("[OMS Aspect]请求异常 - URI:{}, 耗时: {}ms", request.getRequestURI(), stopWatch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    /**
     * 记录请求日志，敏感参数值会被掩盖为 "***"
     * @param joinPoint 切点信息
     * @param request 当前 HTTP 请求
     */
    private void logRequest(JoinPoint joinPoint, HttpServletRequest request) {
        try {
            MethodSignature ms = (MethodSignature) joinPoint.getSignature();
            Map<String, Object> params = extractParams(ms.getParameterNames(), joinPoint.getArgs());

            LoggerFactory.getLogger(joinPoint.getTarget().getClass())
                    .info("[OMS Aspect]Request uri={}, method={}, ip={}, params={}",
                            request.getRequestURI(), request.getMethod(), getClientIp(request), params);

        } catch (Exception e) {
            LoggerFactory.getLogger(joinPoint.getTarget().getClass())
                    .info("[OMS Aspect]Log request failed", e);
        }
    }

    /**
     * 记录响应日志，包含响应类型和处理耗时
     * @param joinPoint 切点信息
     * @param request 当前 HTTP 请求
     * @param result 目标方法的返回结果
     * @param stopWatch 用于计算处理耗时
     */
    private void logResponse(JoinPoint joinPoint, HttpServletRequest request, Object result, StopWatch stopWatch) {
        try {
            LoggerFactory.getLogger(joinPoint.getTarget().getClass())
                    .info("[OMS Aspect]Response uri={}, cost={}ms, resultType={}",
                            request.getRequestURI(),
                            stopWatch.getTotalTimeMillis(),
                            result == null ? "null" : result.getClass().getSimpleName());
        } catch (Exception e) {
            LoggerFactory.getLogger(joinPoint.getTarget().getClass())
                    .info("[OMS Aspect]Log response failed", e);
        }
    }


    private Map<String, Object> extractParams(String[] names, Object[] args) {
        Map<String, Object> map = new HashMap<>();
        if (names == null) return map;

        for (int i = 0; i < names.length; i++) {
            Object arg = args[i];
            if (isSkippable(arg)) continue;

            String key = names[i].toLowerCase();
            map.put(names[i], SENSITIVE_KEYS.contains(key) ? "***" : safeValue(arg));
        }
        return map;
    }

    private boolean isSkippable(Object arg) {
        return arg == null ||
                arg instanceof HttpServletRequest ||
                arg instanceof HttpServletResponse ||
                arg instanceof MultipartFile ||
                arg instanceof MultipartFile[] ||
                arg instanceof InputStream ||
                arg instanceof OutputStream;
    }

    private Object safeValue(Object arg) {
        if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
            return arg;
        }
        return arg.getClass().getSimpleName();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        return (ip == null || ip.isEmpty()) ? request.getRemoteAddr() : ip.split(",")[0];
    }
}
