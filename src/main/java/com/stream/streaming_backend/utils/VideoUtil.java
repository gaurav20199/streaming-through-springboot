package com.stream.streaming_backend.utils;

import com.stream.streaming_backend.entities.Video;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import java.nio.file.Path;

public class VideoUtil {

    public static Resource getVideoResource(Video videoMetaData) {
        String filePath = videoMetaData.getFilePath();
        Path path = Path.of(filePath);
        Resource resource = new FileSystemResource(path);
        return resource;
    }

    public static HttpHeaders getHeadersForStreamingVideos() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Connection", "keep-alive");
        headers.add("Cache-Control","no-cache, no-store, must-revalidate");
        headers.add("Pragma","no-cache");
        headers.add("Expires","0");
        headers.add("X-Content-Type-Options","nosniff");
        return headers;
    }
}
