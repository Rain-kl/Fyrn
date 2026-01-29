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

package com.arctel.fyrn.service.impl;


import com.arctel.fyrn.entity.MmsMeta;
import com.arctel.fyrn.mapper.MmsMetaMapper;
import com.arctel.fyrn.service.MmsMetaService;
import com.arctel.oms.common.base.BaseQueryPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author ryan
* @description 针对表【mms_meta(小说元数据表)】的数据库操作Service实现
* @createDate 2026-01-25 21:02:27
*/
@Service
public class MmsMetaServiceImpl extends ServiceImpl<MmsMetaMapper, MmsMeta>
    implements MmsMetaService {

    @Override
    public BaseQueryPage<MmsMeta> pageMmsMeta(MmsMeta mmsMeta, Integer pageNo, Integer pageSize) {
//        设置分页
        IPage<MmsMeta> page = new Page<>(pageNo, pageSize);

        IPage<MmsMeta> result = page(
                page,
                new LambdaQueryWrapper<MmsMeta>()
                        .eq(mmsMeta.getId() != null,
                                MmsMeta::getId, mmsMeta.getId())
                        .like(mmsMeta.getTitle() != null,
                                MmsMeta::getTitle, mmsMeta.getTitle())
                        .like(mmsMeta.getAuthor() != null,
                                MmsMeta::getAuthor, mmsMeta.getAuthor())
                        .orderByDesc(MmsMeta::getId)
        );

        List<MmsMeta> ordersList = result.getRecords();


        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }
}




