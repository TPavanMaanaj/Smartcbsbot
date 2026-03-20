package com.smartcbs.smartbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainResponse {
    private String status;
    private String message;
    private int documentsProcessed;
    private int chunksEmbedded;
    private long durationMs;
}
