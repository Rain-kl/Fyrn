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

package com.arctel.oms.support;

import cn.hutool.core.util.ObjectUtil;
import com.arctel.oms.api.PublicParamSupportAPI;
import com.arctel.oms.domain.OmsParameter;
import com.arctel.oms.mapper.OmsParameterMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class PublicParamSupport extends PublicParamSupportAPI {

    @Resource
    private OmsParameterMapper omsParameterMapper;

    /**
     * 通过参数代码获取参数值
     *
     * @param paramCode
     * @return
     */
    @Override
    @Cacheable(value = "OmsParameter", key = "'ParamValueByCode:'+#paramCode", unless = "#result == null")
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

    @Override
    @Cacheable(value = "OmsParameter", key = "'ParamValueByCode:'+#paramCode", unless = "#result == null")
    public Object getParamValueByCode(String paramCode) {
        return getParamValueByCode(Integer.parseInt(paramCode));
    }

    @CacheEvict(value = "OmsParameter", allEntries = true)
    public boolean clearCache() {
        log.info("Cleared OmsParameter cache");
        return true;
    }

}
