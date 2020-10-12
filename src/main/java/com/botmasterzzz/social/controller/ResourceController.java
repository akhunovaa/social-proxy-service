package com.botmasterzzz.social.controller;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@SuppressWarnings("deprecation")
public class ResourceController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceController.class);

    @Value("${video.file.upload.path}")
    private String videoPath;

    @RequestMapping(value = "/video/{videoId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> mediaGet(@PathVariable String videoId) {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<byte[]> responseEntity;

        String fullPath = videoPath + "/" + videoId + ".mp4";
        LOGGER.info("User path location directory {}", fullPath);
        File fileX = new File(fullPath);
        LOGGER.info("User file {} in directory {}", fileX.getName(), fullPath);

        Path videoPath = fileX.toPath();
        if (Files.notExists(videoPath)) {
            LOGGER.info("File {} not found", videoPath.toString());
            throw new RuntimeException("Данный файл отсутствует");
        }
        LOGGER.info("File path {} loaded", videoPath.toString());
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        byte[] media = getByteArrayOfTheMedia(videoPath.toFile(), headers);
        responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);
        return responseEntity;
    }

    @RequestMapping(value = "/cover/{coverId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> coverGet(@PathVariable String coverId) {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<byte[]> responseEntity;

        String fullPath = videoPath + "/" + "cover" + "/" + coverId + ".jpeg";
        LOGGER.info("User path location directory {}", fullPath);
        File coverFile = new File(fullPath);
        LOGGER.info("User file {} in directory {}", coverFile.getName(), fullPath);

        Path coverPath = coverFile.toPath();
        if (Files.notExists(coverPath)) {
            LOGGER.info("File {} not found", coverPath.toString());
            throw new RuntimeException("Данный файл отсутствует");
        }
        LOGGER.info("File path {} loaded", coverPath.toString());
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        byte[] media = getByteArrayOfTheMedia(coverPath.toFile(), headers);
        responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);
        return responseEntity;
    }

    private byte[] getByteArrayOfTheMedia(File video, HttpHeaders headers) {
        MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
        String mimeType = fileTypeMap.getContentType(video);
        switch (mimeType) {
            case "image/jpg":
                headers.setContentType(MediaType.parseMediaType("image/jpg"));
                break;
            case "image/jpeg":
                headers.setContentType(MediaType.parseMediaType("image/jpeg"));
                break;
            case "video/mp4":
                headers.setContentType(MediaType.parseMediaType("video/mp4"));
                break;
            default:
                headers.setContentType(MediaType.parseMediaType("video/mp4"));
        }
        byte[] bytes = new byte[0];
        try {
            bytes = FileUtils.readFileToByteArray(video);
        } catch (IOException exception) {
            LOGGER.error("Error occurs during writing {} to {}", video.getName(), video.getAbsolutePath(), exception);
        }
        return bytes;
    }

}
