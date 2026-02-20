package net.arctel.workbench.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.arctel.workbench.domain.WbTask;
import net.arctel.workbench.mapper.WbTaskMapper;
import net.arctel.workbench.service.WbTaskService;
import org.springframework.stereotype.Service;

/**
* @author ryan
* @description 针对表【wb_task(工作台任务表)】的数据库操作Service实现
* @createDate 2026-02-20 17:37:20
*/
@Service
public class WbTaskServiceImpl extends ServiceImpl<WbTaskMapper, WbTask>
    implements WbTaskService{

}




