package com.stream.streaming_backend.repository;

import com.stream.streaming_backend.entities.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video,Long> {
}
