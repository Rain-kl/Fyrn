package net.arctel.workbench.input;

import lombok.Data;
import net.arctel.platform.oms.common.base.BaseQueryPageInput;
import net.arctel.workbench.domain.WbTask;

@Data
public class WbTaskPageInput extends BaseQueryPageInput {
    WbTask wbTask;
}
