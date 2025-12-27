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

package com.arctel.oms.biz;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.arctel.oms.biz.mapper.OmsParameterMapper;
import com.arctel.oms.biz.ppc.OmsParameterService;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.domain.OmsParameter;
import com.arctel.oms.pub.domain.input.AddParameterInput;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ryan
 * @description 针对表【oms_parameter(系统参数表)】的数据库操作Service实现
 * @createDate 2025-12-14 15:12:52
 */
@Service
@Slf4j
public class OmsParameterServiceImpl extends ServiceImpl<OmsParameterMapper, OmsParameter>
        implements OmsParameterService {

    @Resource
    private OmsParameterMapper omsParameterMapper;

    @Override
    public BaseQueryPage<OmsParameter> queryPage(OmsParameter omsParameter, int pageNo, int pageSize) {
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
        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }

    /**
     * 通过参数代码获取参数值
     *
     * @param paramCode 参数代码
     * @return 参数值
     */
    @Cacheable(value = "OmsParameter", key = "'ParamValueByCode:'+#paramCode", unless = "#result == null")
    @Override
    public Object getParamValueByCode(int paramCode) {
        OmsParameter omsParameter = omsParameterMapper.selectById(paramCode);
        if (ObjectUtil.isNull(omsParameter)) {
            return null;
        }
        String paramValue = omsParameter.getParamValue();
        if (StringUtils.isBlank(paramValue)) {
            return null;
        }
        return paramValue;
    }


    @CacheEvict(value = "OmsParameter", allEntries = true)
    @Override
    public boolean clearCache() {
        log.info("Cleared OmsParameter cache");
        return true;
    }

    @CacheEvict(value = "OmsParameter", allEntries = true)
    @Override
    public boolean editParameter(AddParameterInput input) {
        OmsParameter omsParameterOld = getById(input.getParamCode());
        if (omsParameterOld != null) {
            BeanUtil.copyProperties(input, omsParameterOld);
            updateById(omsParameterOld);
            return true;
        }
        return false;
    }
}




