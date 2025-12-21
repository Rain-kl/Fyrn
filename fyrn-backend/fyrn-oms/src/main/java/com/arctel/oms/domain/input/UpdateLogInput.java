package com.arctel.oms.domain.input;

import com.arctel.oms.common.base.BaseInput;
import lombok.Data;

@Data
public class UpdateLogInput extends BaseInput {

    String jobId;
    String logMessage;

}
