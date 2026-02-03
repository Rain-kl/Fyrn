package com.arctel.fyrn.task;

import com.arctel.oms.common.quque.BaseTaskQueue;
import com.arctel.oms.domain.dto.DefaultTaskMessageDTO;
import com.arctel.oms.domain.dto.RegistrationInfoDTO;
import org.springframework.stereotype.Component;

@Component
public class DataSyncQueue extends BaseTaskQueue<DefaultTaskMessageDTO> {
    @Override
    public RegistrationInfoDTO<DefaultTaskMessageDTO> getRegistrationInfo() {
        return new RegistrationInfoDTO<>("DATA_SYNC_QUEUE", "Data Synchronization Task Queue", DefaultTaskMessageDTO.class);
    }
}
