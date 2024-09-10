package com.stream.streaming_backend.service.impl;

import com.stream.streaming_backend.dto.VideoMetaData;
import com.stream.streaming_backend.entities.Video;
import com.stream.streaming_backend.repository.VideoRepository;
import com.stream.streaming_backend.service.intf.VideoService;
import com.stream.streaming_backend.utils.VideoUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class VideoServiceImpl implements VideoService {

    private final VideoRepository repository;
    public VideoServiceImpl(VideoRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init(){
        File videoDirectory = new File(directoryLocation);
        if(!videoDirectory.exists())
            videoDirectory.mkdir();

        File hlsFolder = new File(hlsDirectory);
        if(!hlsFolder.exists())
            hlsFolder.mkdir();
    }
    @Value("${video.directory}")
    private String directoryLocation;

    @Value("${video.chunk-size}")
    private long chunkSize;

    @Value("${video.hls.segments}")
    private String hlsDirectory;

    @Value("${video.ffmpeg-directory}")
    private String ffmpegPath;

    @Override
    public Video save(VideoMetaData videoMetaData, MultipartFile file) throws Exception{
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
        return repository.save(videoObj);
    }

    @Override
    public void saveAndProcessVideo(VideoMetaData videoMetaData, MultipartFile file) throws Exception {
        boolean isSuccess = true;
        StringBuilder videoId = new StringBuilder();
        try{
            Video savedVideo = save(videoMetaData, file);
            videoId.append(savedVideo.getId());
            Path inputVideoPath = Paths.get(savedVideo.getFilePath());
            //TODO: Use executor service here
            String[] resolutions = {"360", "480", "720", "1080"};
            Path outputBasePath = Paths.get(hlsDirectory,videoId.toString());
            for (String resolution : resolutions) {
                String resolutionPath = outputBasePath + File.separator + resolution + "p";
                String playlistPath = resolutionPath + File.separator + "playlist.m3u8";
                String segmentPath = resolutionPath + File.separator + "segment_%03d.ts";

                File resolutionDir = new File(resolutionPath);
                if (!resolutionDir.exists()) {
                    if (!resolutionDir.mkdirs()) {
                        throw new IOException("Failed to create directory: " + resolutionPath);
                    }
                }

                ProcessBuilder processBuilder = new ProcessBuilder(
                        "cmd.exe", "/c", ffmpegPath,
                        "-i", inputVideoPath.toString(),
                        "-vf", "scale=-2:" + resolution,
                        "-c:v", "libx264",
                        "-c:a", "aac",
                        "-strict", "-2",
                        "-f", "hls",
                        "-hls_time", "10",
                        "-hls_list_size", "0",
                        "-hls_segment_filename", segmentPath,
                        playlistPath
                );
                processBuilder.redirectErrorStream(true);
                //TODO: Integrate this code and logging as it will help in logging ffmpeg output.
//                try (Process process = processBuilder.start();
//                     InputStream inputStream = process.getInputStream();
//                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        System.out.println(line); // Log ffmpeg output for debugging
//                    }
//                    int exitStatus = process.waitFor();
//                    if (exitStatus != 0) {
//                        isSuccess = false;
//                        throw new RuntimeException("video processing failed!!");
//                    }
//                }
                processBuilder.inheritIO();
                Process process = processBuilder.start();
                int exit = process.waitFor();
                if (exit != 0) {
                    isSuccess = false;
                    throw new RuntimeException("video processing failed!!");
                }
        }
        generateMasterPlaylist(resolutions,outputBasePath.toString());

        }catch (Exception e) {
            isSuccess = false;
        }finally {
            if(!isSuccess && !videoId.isEmpty()) {
                //TODO: Remove the saved entry
                repository.deleteById(videoId.toString());
                Files.delete(Path.of(hlsDirectory+File.separator+videoId));
            }
        }
    }

    private void generateMasterPlaylist(String[] resolutions,String outputBasePath) throws IOException {
        String masterPlaylistPath = outputBasePath + File.separator + "master.m3u8";
        try (java.io.PrintWriter writer = new java.io.PrintWriter(masterPlaylistPath, "UTF-8")) {
            writer.println("#EXTM3U");
            writer.println("#EXT-X-VERSION:3");

            for (String resolution : resolutions) {
                String playlistPath = resolution + "p/playlist.m3u8";
                int bandwidth = Integer.parseInt(resolution) * 1000;
                writer.println("#EXT-X-STREAM-INF:BANDWIDTH=" + bandwidth + ",RESOLUTION=640x" + resolution);
                writer.println(playlistPath);
            }
        }
    }


    /*@Override
    public void saveAndProcessVideo(VideoMetaData videoMetaData, MultipartFile file) throws Exception {
        boolean isSuccess = true;
        StringBuilder videoId = new StringBuilder();
        try {
            Video savedVideo = save(videoMetaData, file);
            videoId.append(savedVideo.getId());
            Path savedVideoPath = Paths.get(savedVideo.getFilePath());
            //process video
            Path pathToStoreMaster = Paths.get(hlsDirectory,videoId.toString());
            Path pathToStoreCurrSegments = Paths.get(hlsDirectory,videoId.toString(),"segments");
            Files.createDirectories(pathToStoreCurrSegments);
            String[] command = {
                    "cmd.exe", "/c", ffmpegPath,
                    "-i", savedVideoPath.toString(),
                    "-c:v", "libx264",
                    "-c:a", "aac",
                    "-strict", "-2",
                    "-f", "hls",
                    "-hls_time", "10",
                    "-hls_list_size", "0",
                    "-hls_segment_filename", pathToStoreCurrSegments.toString() + File.separator + "segment_%03d.ts",
                    pathToStoreMaster.toString() + File.separator + "master.m3u8"
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                isSuccess = false;
                throw new RuntimeException("video processing failed!!");
            }
        }catch (Exception e) {
            isSuccess = false;
        }finally {
            if(!isSuccess && !videoId.isEmpty()) {
               //TODO: Remove the saved entry
               repository.deleteById(videoId.toString());
               Files.delete(Path.of(hlsDirectory+File.separator+videoId));
               //Files.delete(pathToStoreCurrSegments);
            }
        }
    }*/

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
            long endingRange = startRange+chunkSize-1;
            if(endingRange>=fileLength-1)
                endingRange = fileLength-1;

            InputStream inputStream = videoResource.getInputStream();
            inputStream.skip(startRange);
            long contentLength = endingRange-startRange+1;
            nextChunk = new byte[(int) contentLength];
            inputStream.read(nextChunk,0,nextChunk.length);
        }catch (IOException e) {
                //TODO: Handle exceptions gracefully
        }catch (Exception e) {
            //TODO: Handle exceptions gracefully
        }
        return nextChunk;
    }
}
