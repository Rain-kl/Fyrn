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

package com.arctel.oms.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arctel.oms.common.utils.Result;
import com.arctel.oms.input.UserCreateInput;
import com.arctel.oms.service.OmsAuthService;
import com.arctel.oms.service.OmsUserService;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/oms/auth")
public class OmsAuthController {

    @Resource
    OmsAuthService omsAuthService;

    @Resource
    OmsUserService omsUserService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(UserCreateInput input) {
        return Result.success(omsUserService.createUser(input));
    }
}
