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
public class PingController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingController.class);

    @Value("${video.file.upload.path}")
    private String videoPath;

    @RequestMapping(value = "/ping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void mobileServicePing(HttpServletResponse httpServletResponse) {
        LOGGER.info("Incoming ping...  :-) ADSL");
        returnJsonString("The command was successful", httpServletResponse);
    }

    @RequestMapping(value = "/video/{videoId}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> apiProjectImageGet(@PathVariable String videoId) {
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
        byte[] media = getByteArrayOfTheVideo(videoPath.toFile(), headers);
        responseEntity = new ResponseEntity<>(media, headers, HttpStatus.OK);
        return responseEntity;
    }

    private byte[] getByteArrayOfTheVideo(File video, HttpHeaders headers) {
        MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
        String mimeType = fileTypeMap.getContentType(video);
        switch (mimeType) {
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
