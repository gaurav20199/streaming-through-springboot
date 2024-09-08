package com.stream.streaming_backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoMetaData {
    private String title;
    private String duration;
    private double avgRating;
    private String description;
    private String videoType;
}
