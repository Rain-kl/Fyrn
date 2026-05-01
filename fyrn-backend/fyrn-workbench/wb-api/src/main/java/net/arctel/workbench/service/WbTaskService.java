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
