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

package com.arctel.oms.service;

import com.arctel.oms.common.base.BaseQueryPage;
import com.arctel.oms.domain.OmsParameter;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;

/**
* @author ryan
* @description 针对表【oms_parameter(系统参数表)】的数据库操作Service
* @createDate 2025-12-14 15:12:52
*/
public interface OmsParameterService extends IService<OmsParameter> {

    BaseQueryPage<OmsParameter> queryPage(OmsParameter omsParameter, int pageNo, int pageSize);

    @Override
    default boolean save(OmsParameter entity) {
        return IService.super.save(entity);
    }

    @Override
    default boolean updateById(OmsParameter entity) {
        return IService.super.updateById(entity);
    }

    @Override
    default OmsParameter getById(Serializable id) {
        return IService.super.getById(id);
    }
}
