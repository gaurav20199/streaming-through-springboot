package com.stream.streaming_backend.service.intf;

import com.stream.streaming_backend.dto.VideoMetaData;
import com.stream.streaming_backend.entities.Video;
import com.stream.streaming_backend.repository.VideoRepository;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface VideoService {

    void save(VideoMetaData videoMetaData, MultipartFile file) throws Exception;

    Optional<Video> getVideoById(long id);

    List<Video> getVideosByTitle(String title);

    List<Video> getVideosByRating(double rating);

}
