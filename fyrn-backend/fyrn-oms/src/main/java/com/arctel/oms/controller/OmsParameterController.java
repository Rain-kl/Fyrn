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


import cn.hutool.core.bean.BeanUtil;
import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.common.utils.Result;
import com.arctel.oms.domain.OmsParameter;
import com.arctel.oms.domain.input.AddParameterInput;
import com.arctel.oms.domain.input.QueryParameterInput;
import com.arctel.oms.service.OmsParameterService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping("/oms/parameter")
@RestController
public class OmsParameterController {

    @Resource
    OmsParameterService omsParameterService;

    /**
     * 分页查询参数列表
     *
     * @param input
     * @return
     */
    @GetMapping("/page")
    public Result<BaseQueryPage<OmsParameter>> page(QueryParameterInput input) {

        OmsParameter omsParameter = new OmsParameter();
        BeanUtils.copyProperties(input, omsParameter);
        BaseQueryPage<OmsParameter> omsParameterQueryPage = omsParameterService.queryPage(omsParameter, input.getPageNo(), input.getPageSize());

        return Result.success(omsParameterQueryPage);
    }

    /**
     * 修改系统参数
     *
     * @param input 分页输入对象
     * @return 分页结果
     */
    @PostMapping("/edit")
    public Result<String> addParameter(@RequestBody AddParameterInput input) {
        OmsParameter omsParameterOld = omsParameterService.getById(input.getParamCode());
        if(omsParameterOld!=null){
            BeanUtil.copyProperties(input, omsParameterOld);
            omsParameterService.updateById(omsParameterOld);
            return Result.success("修改成功");
        }
        return Result.fail("参数不存在，修改失败");
    }
}
