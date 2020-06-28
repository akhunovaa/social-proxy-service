package com.botmasterzzz.social.service.impl;

import com.botmasterzzz.social.dto.MailDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class KafkaTelegramConsumerImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTelegramConsumerImpl.class);

    private final ObjectMapper objectMapper;

    public KafkaTelegramConsumerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @KafkaListener(id = "telegram-proxy-service", topics = {"telegram-outcome-messages"}, containerFactory = "singleFactory")
    public void consume(MailDTO mailDTO) {
        LOGGER.info("{} => consumed {}", LocalTime.now(), writeValueAsString(mailDTO));
        String recipient = mailDTO.getRecipient();
        String subject = null != mailDTO.getSubject() ? mailDTO.getSubject() : "YourAPI: Testing telegram social proxy service";
        String message = null != mailDTO.getMessage() ? mailDTO.getMessage() : "YourAPI: Testing telegram social proxy service";
    }

    private String writeValueAsString(MailDTO mailDTO) {
        try {
            return objectMapper.writeValueAsString(mailDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Writing value to JSON failed: " + mailDTO.toString());
        }
    }
}
