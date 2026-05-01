package net.arctel.workbench.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.arctel.platform.framework.utils.CustomPaginationUtil;
import net.arctel.oms.common.base.BaseQueryPage;
import net.arctel.oms.common.base.BaseQueryPageInput;
import net.arctel.workbench.domain.WbTask;
import net.arctel.workbench.mapper.WbTaskMapper;
import net.arctel.workbench.service.WbTaskService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * @author ryan
 * @description 针对表【wb_task(工作台任务表)】的数据库操作Service实现
 * @createDate 2026-02-20 17:37:20
 */
@Service
public class WbTaskServiceImpl extends ServiceImpl<WbTaskMapper, WbTask>
        implements WbTaskService {

    @Override
    public BaseQueryPage<WbTask> pageWbTask(
            WbTask wbTaskInput, Integer pageNo, Integer pageSize, String orderBy, String orderDirection) {

        if (orderBy != null) {
            return standardOrderBy(wbTaskInput, pageNo, pageSize, orderBy, orderDirection);
        } else {
            return defaultOrderBy(wbTaskInput, pageNo, pageSize);
        }
    }

    @Override
    public boolean addSubTask(WbTask subTask) {
        if (subTask.getParentId() == null || subTask.getParentId().isEmpty()) {
            throw new IllegalArgumentException("子任务必须指定父任务ID");
        }
        // 校验父任务是否存在
        WbTask parent = getById(subTask.getParentId());
        if (parent == null) {
            throw new IllegalArgumentException("父任务不存在: " + subTask.getParentId());
        }
        return save(subTask);
    }

    @Override
    public List<WbTask> listSubTasks(String parentId) {
        QueryWrapper<WbTask> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(WbTask::getParentId, parentId);
        return list(wrapper);
    }

    private QueryWrapper<WbTask> buildPageWbTaskQueryWrapper(WbTask wbTaskInput) {
        QueryWrapper<WbTask> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .like(wbTaskInput.getName() != null, WbTask::getName, wbTaskInput.getName())
                .like(wbTaskInput.getDescription() != null, WbTask::getDescription, wbTaskInput.getDescription())
                .eq(wbTaskInput.getType() != null, WbTask::getType, wbTaskInput.getType())
                .like(wbTaskInput.getTag() != null, WbTask::getTag, wbTaskInput.getTag())
                .eq(wbTaskInput.getStatus() != null, WbTask::getStatus, wbTaskInput.getStatus())
                .eq(wbTaskInput.getPriority() != null, WbTask::getPriority, wbTaskInput.getPriority())
                .eq(wbTaskInput.getParentId() != null, WbTask::getParentId, wbTaskInput.getParentId())
                .isNull(wbTaskInput.getParentId() == null, WbTask::getParentId)
                .ge(wbTaskInput.getStartTime() != null, WbTask::getStartTime, wbTaskInput.getStartTime())
                .le(wbTaskInput.getDeadline() != null, WbTask::getDeadline, wbTaskInput.getDeadline())
                .ge(wbTaskInput.getCompleteTime() != null, WbTask::getCompleteTime, wbTaskInput.getCompleteTime());
        return wrapper;
    }

    private BaseQueryPage<WbTask> defaultOrderBy(WbTask wbTaskInput, Integer pageNo, Integer pageSize) {
        // 1. 构建查询条件
        QueryWrapper<WbTask> wrapper = buildPageWbTaskQueryWrapper(wbTaskInput);
        // 2. 查询符合条件的所有数据
        List<WbTask> allTasks = getBaseMapper().selectList(wrapper);

        // 3. 定义排序规则：首先按状态排序(未开始0、进行中1优先)，然后按截止日期升序排序
        Comparator<WbTask> taskComparator = Comparator
                // 按状态排序：0和1排前面，2和3排后面
                .comparing((WbTask task) -> {
                    Integer status = task.getStatus();
                    if (status == null)
                        return 2; // null 状态当作已完成处理
                    return (status == 0 || status == 1) ? 0 : 1; // 未开始和进行中优先
                })
                // 再按截止日期升序排序，null值排在最后
                .thenComparing(WbTask::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));

        // 4. 使用 CustomPaginationUtil 进行排序和分页
        CustomPaginationUtil.PageResult<WbTask> pageResult = CustomPaginationUtil.pagination(allTasks, pageNo, pageSize,
                taskComparator);

        // 5. 转换为 BaseQueryPage 返回
        return new BaseQueryPage<>(pageResult.getTotal(), pageSize, pageNo, pageResult.getRows());
    }

    private BaseQueryPage<WbTask> standardOrderBy(WbTask wbTaskInput, Integer pageNo, Integer pageSize, String orderBy,
            String orderDirection) {
        // 1. 设置分页
        IPage<WbTask> page = new Page<>(pageNo, pageSize);

        QueryWrapper<WbTask> wrapper = buildPageWbTaskQueryWrapper(wbTaskInput);

        // 2. 动态排序 (QueryWrapper 直接支持字符串 orderBy)
        if (orderBy != null && orderDirection != null) {
            boolean isAsc = BaseQueryPageInput.ASC.equals(orderDirection);
            // 这里的 orderBy 是字符串，例如 "name" 或 "create_time"
            wrapper.orderBy(true, isAsc, orderBy);
        }

        // 4. 执行查询
        IPage<WbTask> result = page(page, wrapper);
        List<WbTask> ordersList = result.getRecords();
        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }

}
