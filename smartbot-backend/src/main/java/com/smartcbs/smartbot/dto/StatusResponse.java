package com.smartcbs.smartbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {
    private String ollamaStatus;
    private String modelName;
    private int embeddingsCount;
    private boolean isReady;
    private String lastTrainingDate;
}
