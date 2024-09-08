package com.stream.streaming_backend.service.impl;

import com.stream.streaming_backend.dto.VideoMetaData;
import com.stream.streaming_backend.entities.Video;
import com.stream.streaming_backend.repository.VideoRepository;
import com.stream.streaming_backend.service.intf.VideoService;
import jakarta.annotation.PostConstruct;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository repository;

    @PostConstruct
    public void init(){
        File directory = new File(directoryLocation);
        if(!directory.exists())
            directory.mkdir();
    }
    @Value("${video.directory}")
    private String directoryLocation;

    @Override
    public void save(VideoMetaData videoMetaData, MultipartFile file) throws Exception{
        Video videoObj = new Video();
        try {
            String fileName = file.getOriginalFilename();
            Path path = Path.of(StringUtils.cleanPath(directoryLocation),StringUtils.cleanPath(fileName));
            file.transferTo(path);
            videoObj.setFilePath(path.toString());
        }catch (IOException e) {
            //TODO: Introduce logging and handle exception properly
            System.out.println("Error occurred while saving the file");
            throw new Exception("Error occurred while saving the file");
        }

        BeanUtils.copyProperties(videoMetaData,videoObj);
        videoObj.setId(UUID.randomUUID().toString());
        //TODO: add logic for calculating video duration and ratings
        repository.save(videoObj);
    }

    @Override
    public Optional<Video> getVideoById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<Video> getVideosByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getVideosByRating(double rating) {
        return null;
    }
}
