package net.arctel.opkit.service.impl;

import com.arctel.oms.infrastructure.task.DefaultTaskMessage;
import jakarta.annotation.Resource;
import net.arctel.opkit.input.TranslationBaseInput;
import net.arctel.opkit.queue.OpKitProducerQueue;
import net.arctel.opkit.service.TranslationService;
import org.springframework.stereotype.Service;

import static net.arctel.opkit.common.constants.TaskTagConstant.OPKIT_TRANS_TAG;

@Service
public class TranslationServiceImpl extends BaseServiceImpl implements TranslationService {

    @Resource
    OpKitProducerQueue opKitProducerQueue;

    @Override
    public String translate(TranslationBaseInput input) {
        DefaultTaskMessage taskMsg = new DefaultTaskMessage();
        taskMsg = (DefaultTaskMessage) taskMsg.build().buildTaskMessage(OPKIT_TRANS_TAG, "OPKIT", input);
        opKitProducerQueue.push(taskMsg);
        return taskMsg.getTaskId();
    }
}
