package com.smartcbs.smartbot.dto;

import dev.langchain4j.data.segment.TextSegment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContextWithSources {
    private String context;
    private List<SourceInfo> sources;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceInfo {
        private String id;
        private String filename;
        private String url;
        private String sourceType;
        private Double relevanceScore;
        private String snippet;
        private String uploadDate;
        private String version;
    }
}