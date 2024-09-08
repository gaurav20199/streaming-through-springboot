package com.stream.streaming_backend.controller;

import com.stream.streaming_backend.dto.VideoMetaData;
import com.stream.streaming_backend.service.intf.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/video")
public class VideoController {
    @Autowired
    private VideoService service;

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

}
