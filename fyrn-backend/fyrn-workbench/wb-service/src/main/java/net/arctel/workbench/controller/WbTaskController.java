package net.arctel.workbench.controller;

import jakarta.annotation.Resource;
import net.arctel.framework.utils.Result;
import net.arctel.oms.common.base.BaseQueryPage;
import net.arctel.workbench.domain.WbTask;
import net.arctel.workbench.input.WbTaskPageInput;
import net.arctel.workbench.service.WbTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author ryan
 * @description 针对表【wb_task(工作台任务表)】的数据库操作Mapper
 * @createDate 2026-02-20 17:37:20
 * @Entity generator.domain.WbTask
 */
@RequestMapping("/wb")
@RestController
public class WbTaskController {

    @Resource
    WbTaskService wbTaskService;

    @PostMapping("/page")
    public Result<BaseQueryPage<WbTask>> page(@RequestBody WbTaskPageInput input) {
        WbTask wbTaskInput = WbTask.buildPageInput(input.getWbTask());

        BaseQueryPage<WbTask> wbTaskBaseQueryPage = wbTaskService.pageWbTask(
                wbTaskInput, input.getPageNo(), input.getPageSize(), input.getOrderBy(), input.getOrderDirection());
        return Result.success(wbTaskBaseQueryPage);
    }

    @GetMapping("/get")
    public Result<WbTask> get(String id) {
        return Result.success(wbTaskService.getById(id));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody WbTask input) {
        return Result.success(wbTaskService.save(input));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody WbTask input) {
        return Result.success(wbTaskService.updateById(input));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody List<String> ids) {
        return Result.success(wbTaskService.removeByIds(ids));
    }
}
