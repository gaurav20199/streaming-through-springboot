package com.stream.streaming_backend.service.impl;

import com.stream.streaming_backend.dto.VideoMetaData;
import com.stream.streaming_backend.entities.Video;
import com.stream.streaming_backend.repository.VideoRepository;
import com.stream.streaming_backend.service.intf.VideoService;
import com.stream.streaming_backend.utils.VideoUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
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

    @Value("${video.chunk-size}")
    private long chunkSize;

    @Override
    public void save(VideoMetaData videoMetaData, MultipartFile file) throws Exception{
        Video videoObj = new Video();
        try {
            String fileName = file.getOriginalFilename();
            Path path = Path.of(StringUtils.cleanPath(directoryLocation),StringUtils.cleanPath(fileName));
            file.transferTo(path);
            videoObj.setFilePath(path.toString());
            videoObj.setContentType(file.getContentType());
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
    public Video getVideoById(String id) {
        //TODO: Improve Exception Handling
        return repository.getVideoById(id).orElseThrow();
    }

    @Override
    public List<Video> getVideosByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getVideosByRating(double rating) {
        return null;
    }

    public Resource streamEntireVideo(String videoId) {
        Video videoMetaData = getVideoById(videoId);
        return VideoUtil.getVideoResource(videoMetaData);
    }

    @Override
    public byte[] streamVideoInChunks(Resource videoResource,long startRange) {
        byte[] nextChunk = null;
        try {
            long fileLength = videoResource.contentLength();
//            long endingRange = startRange+(1024*1024)-1;
            long endingRange = startRange+chunkSize-1;
            if(endingRange>=fileLength-1)
                endingRange = fileLength-1;

            System.out.println("range start : " + startRange);
            System.out.println("range end : " + endingRange);

            InputStream inputStream = videoResource.getInputStream();
            inputStream.skip(startRange);
            long contentLength = endingRange-startRange+1;
            nextChunk = new byte[(int) contentLength];
            inputStream.read(nextChunk,0,nextChunk.length);
        }catch (IOException e) {

        }catch (Exception e) {

        }
        return nextChunk;
    }
}
