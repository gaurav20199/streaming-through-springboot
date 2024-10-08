package com.stream.streaming_backend.service.intf;

import com.stream.streaming_backend.dto.VideoMetaData;
import com.stream.streaming_backend.entities.Video;
import com.stream.streaming_backend.repository.VideoRepository;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface VideoService {

    Video save(VideoMetaData videoMetaData, MultipartFile file) throws Exception;

    void saveAndProcessVideo(VideoMetaData videoMetaData, MultipartFile file) throws Exception;

    Video getVideoById(String id);

    List<Video> getVideosByTitle(String title);

    List<Video> getVideosByRating(double rating);

    Resource streamEntireVideo(String videoId);

    byte[] streamVideoInChunks(Resource videoResource,long startRange);

}
