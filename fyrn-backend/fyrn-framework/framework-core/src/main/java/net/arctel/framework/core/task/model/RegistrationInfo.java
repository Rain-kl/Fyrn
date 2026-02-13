package net.arctel.framework.core.task.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationInfo<T> {
    private String queueName;
    private String description;
    private Class<T> taskMessageClazz;
}