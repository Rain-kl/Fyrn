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

    /**
     * 分页查询工作台任务列表
     *
     * @param input
     * @return
     */
    @PostMapping("/page")
    public Result<BaseQueryPage<WbTask>> page(@RequestBody WbTaskPageInput input) {
        WbTask wbTask = input.getWbTask();
        if (wbTask == null) {
            wbTask = new WbTask();
        }
        WbTask wbTaskInput = WbTask.buildPageInput(wbTask);

        BaseQueryPage<WbTask> wbTaskBaseQueryPage = wbTaskService.pageWbTask(
                wbTaskInput, input.getPageNo(), input.getPageSize(), input.getOrderBy(), input.getOrderDirection());
        return Result.success(wbTaskBaseQueryPage);
    }

    /**
     * 根据ID查询工作台任务详情
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public Result<WbTask> get(String id) {
        return Result.success(wbTaskService.getById(id));
    }

    /**
     * 创建工作台任务
     *
     * @param input
     * @return
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody WbTask input) {
        return Result.success(wbTaskService.save(input));
    }

    /**
     * 更新工作台任务
     *
     * @param input
     * @return
     */
    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody WbTask input) {
        return Result.success(wbTaskService.updateById(input));
    }

    /**
     * 删除工作台任务
     *
     * @param ids
     * @return
     */
    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody List<String> ids) {
        return Result.success(wbTaskService.removeByIds(ids));
    }

    /**
     * 创建子任务
     *
     * @param input 子任务信息，parentId 必填
     * @return
     */
    @PostMapping("/addSubTask")
    public Result<Boolean> addSubTask(@RequestBody WbTask input) {
        return Result.success(wbTaskService.addSubTask(input));
    }

    /**
     * 查询指定父任务下的子任务列表
     *
     * @param parentId 父任务ID
     * @return
     */
    @GetMapping("/subTasks")
    public Result<List<WbTask>> subTasks(String parentId) {
        return Result.success(wbTaskService.listSubTasks(parentId));
    }
}
