package com.arctel.oms.domain.dto;

import lombok.Data;

@Data
public class JobProgressDto {

    Integer current;

    Integer total;

    private double getPercent() {
        if (total == null || total == 0) {
            return 0.0;
        }
        if (current == null) {
            return 0.0;
        }
        return (double) current / total;
    }
}
