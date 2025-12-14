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

package com.arctel.oms.service.impl;

import com.arctel.common.baseDTO.QueryPage;
import com.arctel.oms.domain.entity.OmsParameter;
import com.arctel.oms.domain.mapper.OmsParameterMapper;
import com.arctel.oms.service.OmsParameterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ryan
 * @description 针对表【oms_parameter(系统参数表)】的数据库操作Service实现
 * @createDate 2025-12-14 15:12:52
 */
@Service
public class OmsParameterServiceImpl extends ServiceImpl<OmsParameterMapper, OmsParameter>
        implements OmsParameterService {

    @Override
    public QueryPage<OmsParameter> queryPage(OmsParameter omsParameter, int pageNo, int pageSize) {
        IPage<OmsParameter> page = new Page<>(pageNo, pageSize);

        IPage<OmsParameter> result = page(
                page,
                new LambdaQueryWrapper<OmsParameter>()
                        .like(omsParameter.getParamCode() != null,
                                OmsParameter::getParamCode, omsParameter.getParamCode())
                        .like(omsParameter.getParamName() != null,
                                OmsParameter::getParamName, omsParameter.getParamName())
                        .eq(omsParameter.getEnabledFlag() != null,
                                OmsParameter::getEnabledFlag, omsParameter.getEnabledFlag())
                        .orderByDesc(OmsParameter::getParamCode)
        );
        List<OmsParameter> ordersList = result.getRecords();
        return new QueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }
}




