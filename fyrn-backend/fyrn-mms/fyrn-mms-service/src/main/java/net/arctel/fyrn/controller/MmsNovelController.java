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

package net.arctel.fyrn.controller;


import jakarta.annotation.Resource;
import net.arctel.fyrn.entity.MmsNovel;
import net.arctel.fyrn.entity.MmsNovelFile;
import net.arctel.fyrn.input.MmsPageInput;
import net.arctel.fyrn.service.MmsNovelService;
import net.arctel.oms.common.base.BaseQueryPage;
import net.arctel.platform.framework.utils.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/mms")
@RestController
@Validated
public class MmsNovelController {

    @Resource
    MmsNovelService mmsNovelService;

    /**
     * 小说分页列表
     *
     * @param input 分页输入对象
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<BaseQueryPage<MmsNovel>> page(MmsPageInput input) {

        MmsNovel mmsNovel = new MmsNovel();
        BeanUtils.copyProperties(input, mmsNovel);
        mmsNovel.setId(input.getNovelId());

        BaseQueryPage<MmsNovel> mmsNovelQueryPage = mmsNovelService.pageMmsNovel(
                mmsNovel, input.getPageNo(), input.getPageSize());
        return Result.success(mmsNovelQueryPage);
    }

    /**
     * 查询物料文件列表
     */
    @GetMapping("/file")
    public Result<List<MmsNovelFile>> getMmsNovelFile(@RequestParam("mmsNovelId") String mmsNovelId) {
        return Result.success(mmsNovelService.getMmsNovelFile(mmsNovelId));
    }

}
