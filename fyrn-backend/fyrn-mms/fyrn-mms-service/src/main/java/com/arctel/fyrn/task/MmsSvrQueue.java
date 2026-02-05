package com.arctel.fyrn.task;

import com.arctel.oms.infrastructure.task.DefaultTaskMessage;
import com.arctel.oms.infrastructure.task.OmsTaskQueue;
import com.arctel.oms.infrastructure.task.base.BaseTaskQueue;
import com.arctel.oms.infrastructure.task.base.BaseThreadPoolListener;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class MmsSvrQueue extends OmsTaskQueue<DefaultTaskMessage> {

    @Resource
    MmsSvrPoolListener dataSyncPoolListener;

    @Override
    public BaseThreadPoolListener<DefaultTaskMessage> getPoolListener() {
        return dataSyncPoolListener;
    }

    @Override
    public RegistrationInfo<DefaultTaskMessage> getRegistrationInfo() {
        return new RegistrationInfo<>("dataSyncQueue", "Data Synchronization Task Queue",
                DefaultTaskMessage.class);
    }
}
