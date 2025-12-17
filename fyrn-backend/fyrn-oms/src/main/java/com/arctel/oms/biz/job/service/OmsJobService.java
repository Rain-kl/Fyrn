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

package com.arctel.oms.biz.job.service;

import com.arctel.oms.biz.job.domain.OmsJob;
import com.arctel.oms.common.base.BaseQueryPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author hspcadmin
* @description 针对表【oms_job(任务表)】的数据库操作Service
* @createDate 2025-12-16 11:27:56
*/
public interface OmsJobService extends IService<OmsJob> {

    BaseQueryPage<OmsJob> pageJob(OmsJob omsJob, Integer pageNo, Integer pageSize);

}
