package com.stream.streaming_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Course {
    @Id
    private String id;

    private String description;

    private double avgRating;

//    @OneToMany(mappedBy = "course")
//    private List<Video> courseVideos;
}
