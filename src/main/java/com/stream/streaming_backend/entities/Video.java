package com.stream.streaming_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
public class Video {
    @Id
    private String id;
    private String title;
    private String duration;
    private double avgRating;
    private String description;
    private String videoType;
    private String filePath;
    private String contentType;
//    @ManyToOne
//    private Course course;

}
