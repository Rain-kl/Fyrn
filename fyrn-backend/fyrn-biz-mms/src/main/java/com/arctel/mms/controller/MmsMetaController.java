package com.arctel.mms.controller;

import com.arctel.domain.dao.entity.MmsMeta;
import com.arctel.domain.dto.input.MmsMetaPageInput;
import com.arctel.mms.service.MmsMetaService;
import com.arctel.oms.pub.base.BaseQueryPage;
import com.arctel.oms.pub.utils.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
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

        MmsMeta mmsMeta = new MmsMeta();
        BeanUtils.copyProperties(input, mmsMeta);
        mmsMeta.setId(input.getNovelId());

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
