package com.arctel.oms.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationInfoDTO<T> {
    private String queueName;
    private String description;
    private Class<T> clazz;
}
