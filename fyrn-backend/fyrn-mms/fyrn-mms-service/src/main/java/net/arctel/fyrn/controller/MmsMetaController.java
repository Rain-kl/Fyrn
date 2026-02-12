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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.arctel.framework.utils.Result;
import net.arctel.fyrn.entity.MmsMeta;
import net.arctel.fyrn.input.MmsMetaPageInput;
import net.arctel.fyrn.service.MmsMetaService;
import net.arctel.oms.common.base.BaseQueryPage;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/mms/meta")
@RestController
@Validated
public class MmsMetaController {

    @Resource
    MmsMetaService mmsMetaService;

    /**
     * 小说元数据分页列表
     *
     * @param input 分页输入对象
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<BaseQueryPage<MmsMeta>> page(MmsMetaPageInput input) {

        MmsMeta mmsMeta = MmsMetaPageInput.buildMmsMeta(input);

        BaseQueryPage<MmsMeta> mmsNovelQueryPage = mmsMetaService.pageMmsMeta(
                mmsMeta, input.getPageNo(), input.getPageSize());
        return Result.success(mmsNovelQueryPage);
    }

    /**
     * 根据ID查询小说元数据
     *
     * @param id 小说元数据ID
     * @return 小说元数据详情
     */
    @GetMapping("/{id}")
    public Result<MmsMeta> getById(@PathVariable("id") @NotNull(message = "ID不能为空") Long id) {
        MmsMeta mmsMeta = mmsMetaService.getById(id);
        if (mmsMeta == null) {
            return Result.error("小说元数据不存在");
        }
        return Result.success(mmsMeta);
    }

    /**
     * 创建小说元数据
     *
     * @param mmsMeta 小说元数据对象
     * @return 创建结果
     */
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid MmsMeta mmsMeta) {
        boolean success = mmsMetaService.save(mmsMeta);
        if (success) {
            return Result.success(true);
        }
        return Result.error("创建失败");
    }

    /**
     * 更新小说元数据
     *
     * @param mmsMeta 小说元数据对象（必须包含ID）
     * @return 更新结果
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Valid MmsMeta mmsMeta) {
        if (mmsMeta.getId() == null) {
            return Result.error("ID不能为空");
        }
        boolean success = mmsMetaService.updateById(mmsMeta);
        if (success) {
            return Result.success(true);
        }
        return Result.error("更新失败");
    }

    /**
     * 删除小说元数据
     *
     * @param id 小说元数据ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestParam("id") @NotNull(message = "ID不能为空") Long id) {
        boolean success = mmsMetaService.removeById(id);
        if (success) {
            return Result.success(true);
        }
        return Result.error("删除失败");
    }

}
