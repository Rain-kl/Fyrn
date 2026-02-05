package com.arctel.fyrn.task;

import com.arctel.oms.infrastructure.task.DefaultTaskMessage;
import com.arctel.oms.infrastructure.task.base.BaseThreadPoolListener;
import org.springframework.stereotype.Component;

@Component
public class DataSyncPoolListener extends BaseThreadPoolListener<DefaultTaskMessage> {

}
