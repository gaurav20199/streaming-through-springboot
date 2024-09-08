package com.stream.streaming_backend.controller;

import com.stream.streaming_backend.dto.VideoMetaData;
import com.stream.streaming_backend.service.intf.VideoService;
import com.stream.streaming_backend.utils.VideoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/video")
@CrossOrigin("http://localhost:5173") //react app is running at this port
public class VideoController {
    @Autowired
    private VideoService service;

    @Value("${video.chunk-size}")
    private long chunkSize;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file,@RequestParam String title,
                                         @RequestParam String description,@RequestParam("content-type") String contentType) {
        System.out.println(contentType);
        VideoMetaData videoMetaData = VideoMetaData.builder().description(description).title(title).videoType(contentType).build();
        try {
            service.save(videoMetaData, file);
        }catch (Exception e) {
            //TODO Handle custom exceptions properly
            return new ResponseEntity<>("Error occurred while uploading the file",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Video uploaded successfully",HttpStatus.CREATED);
    }

    @GetMapping("/stream/v1/{id}")
    public ResponseEntity<Resource> streamEntireVideo(@PathVariable("id") String videoId) {
        Resource resource = service.streamEntireVideo(videoId);
        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }
    @GetMapping("/stream/v2/{id}")
    public ResponseEntity<?> streamVideoInChunksV4(@PathVariable("id") String videoId,@RequestHeader(value = "Range", required = false) String range) {
        try {
            Resource resource = service.streamEntireVideo(videoId);
            long startingRange = range==null?0:Long.valueOf(range.replace("bytes=", "").split("-")[0]);
            long endingRange = startingRange+chunkSize-1;
            if(endingRange>=resource.contentLength())
                endingRange = resource.contentLength()-1;

            byte[] videoChunks = service.streamVideoInChunks(resource, startingRange);
            HttpHeaders headersForStreamingVideos = VideoUtil.getHeadersForStreamingVideos();
            headersForStreamingVideos.add("Content-Range", "bytes " + startingRange + "-" + endingRange + "/" + resource.contentLength());
            headersForStreamingVideos.setContentLength(videoChunks.length);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(headersForStreamingVideos).contentType(MediaType.parseMediaType("video/mp4")).body(new ByteArrayResource(videoChunks));
        }catch (Exception e) {
                //TODO: Handle exception and add Logging
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred");
    }
}
