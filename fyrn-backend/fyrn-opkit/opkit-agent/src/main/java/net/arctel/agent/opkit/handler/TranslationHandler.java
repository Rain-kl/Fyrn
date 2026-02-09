package net.arctel.agent.opkit.handler;

import com.arctel.oms.infrastructure.task.DefaultTaskMessage;
import com.arctel.oms.infrastructure.task.fast.FastTaskHandler;
import org.springframework.stereotype.Component;

import static net.arctel.opkit.common.constants.TaskTagConstant.OPKIT_TRANS_TAG;

@Component
public class TranslationHandler extends FastTaskHandler<DefaultTaskMessage> {
    @Override
    public String getHandlerId() {
        return OPKIT_TRANS_TAG;
    }

    @Override
    public void handleTask(DefaultTaskMessage taskMsg) throws Exception {
        collectResponse("这是一个翻译任务的示例响应");
    }
}
