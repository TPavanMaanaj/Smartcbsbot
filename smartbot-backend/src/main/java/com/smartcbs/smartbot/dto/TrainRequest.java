package com.smartcbs.smartbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainRequest {
    private List<String> urls;
    private boolean includeDefaultUrls;
}
