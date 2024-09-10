package com.stream.streaming_backend.repository;

import com.stream.streaming_backend.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video,String> {
    Optional<Video> getVideoById(String videoId);
}
