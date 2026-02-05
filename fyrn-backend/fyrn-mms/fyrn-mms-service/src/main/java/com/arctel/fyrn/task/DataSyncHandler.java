package com.arctel.fyrn.task;

import com.arctel.oms.infrastructure.task.OmsTaskHandler;
import com.arctel.oms.infrastructure.task.DefaultTaskMessage;
import org.springframework.stereotype.Component;

@Component
public class DataSyncHandler extends OmsTaskHandler<DefaultTaskMessage> {

    @Override
    public String getHandlerId() {
        return "666";
    }

    @Override
    public void handleTask(DefaultTaskMessage taskMsg) {
        System.out.println("开始进入耗时处理" );
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("结束耗时处理" );

    }


}
