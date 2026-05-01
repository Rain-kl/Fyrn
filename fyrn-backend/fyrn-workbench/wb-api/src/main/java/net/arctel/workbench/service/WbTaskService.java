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

package net.arctel.workbench.service;

import net.arctel.platform.oms.common.base.BaseQueryPage;
import net.arctel.workbench.domain.WbTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author ryan
 * @description 针对表【wb_task(工作台任务表)】的数据库操作Service
 * @createDate 2026-02-20 17:37:20
 */
public interface WbTaskService extends IService<WbTask> {

    BaseQueryPage<WbTask> pageWbTask(
            WbTask wbTaskInput, Integer pageNo, Integer pageSize, String orderBy, String orderDirection);

    /**
     * 创建子任务
     *
     * @param subTask 子任务信息，parentId 必填
     * @return 是否创建成功
     */
    boolean addSubTask(WbTask subTask);

    /**
     * 查询指定父任务下的子任务列表
     *
     * @param parentId 父任务ID
     * @return 子任务列表
     */
    List<WbTask> listSubTasks(String parentId);

}
