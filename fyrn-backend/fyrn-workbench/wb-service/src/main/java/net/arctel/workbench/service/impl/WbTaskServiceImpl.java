package net.arctel.workbench.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.arctel.oms.common.base.BaseQueryPage;
import net.arctel.oms.common.base.BaseQueryPageInput;
import net.arctel.workbench.domain.WbTask;
import net.arctel.workbench.mapper.WbTaskMapper;
import net.arctel.workbench.service.WbTaskService;
import org.springframework.stereotype.Service;

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

        // 1. 设置分页
        IPage<WbTask> page = new Page<>(pageNo, pageSize);

        // 🔥 关键点：这里改为普通的 QueryWrapper，它支持字符串列名
        QueryWrapper<WbTask> wrapper = new QueryWrapper<>();

        // 2. 动态排序 (QueryWrapper 直接支持字符串 orderBy)
        if (orderBy != null && orderDirection != null) {
            boolean isAsc = BaseQueryPageInput.ASC.equals(orderDirection);
            // 这里的 orderBy 是字符串，例如 "name" 或 "create_time"
            wrapper.orderBy(true, isAsc, orderBy);
        }

        // 3. 条件筛选 (使用 wrapper.lambda() 获取 LambdaQueryWrapper 进行筛选)
        // 这样你就可以继续使用 WbTask::getName 这种写法了
        wrapper.lambda()
                .like(wbTaskInput.getName() != null, WbTask::getName, wbTaskInput.getName())
                .like(wbTaskInput.getDescription() != null, WbTask::getDescription, wbTaskInput.getDescription())
                .eq(wbTaskInput.getType() != null, WbTask::getType, wbTaskInput.getType())
                .like(wbTaskInput.getTag() != null, WbTask::getTag, wbTaskInput.getTag())
                .eq(wbTaskInput.getStatus() != null, WbTask::getStatus, wbTaskInput.getStatus())
                .eq(wbTaskInput.getPriority() != null, WbTask::getPriority, wbTaskInput.getPriority())
                .eq(wbTaskInput.getParentId() != null, WbTask::getParentId, wbTaskInput.getParentId())
                .ge(wbTaskInput.getStartTime() != null, WbTask::getStartTime, wbTaskInput.getStartTime())
                .le(wbTaskInput.getDeadline() != null, WbTask::getDeadline, wbTaskInput.getDeadline())
                .ge(wbTaskInput.getCompleteTime() != null, WbTask::getCompleteTime, wbTaskInput.getCompleteTime());

        // 4. 执行查询
        IPage<WbTask> result = page(page, wrapper);

        List<WbTask> ordersList = result.getRecords();

        return new BaseQueryPage<>(result.getTotal(), pageSize, pageNo, ordersList);
    }
}




