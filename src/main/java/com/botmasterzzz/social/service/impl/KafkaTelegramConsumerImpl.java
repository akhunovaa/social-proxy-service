package com.botmasterzzz.social.service.impl;

import com.botmasterzzz.bot.api.impl.methods.send.SendDocument;
import com.botmasterzzz.bot.api.impl.methods.send.SendMessage;
import com.botmasterzzz.bot.api.impl.methods.send.SendPhoto;
import com.botmasterzzz.bot.api.impl.methods.send.SendVideo;
import com.botmasterzzz.bot.api.impl.methods.update.EditMessageReplyMarkup;
import com.botmasterzzz.bot.api.impl.methods.update.EditMessageText;
import com.botmasterzzz.bot.api.impl.objects.OutgoingMessage;
import com.botmasterzzz.bot.exceptions.TelegramApiException;
import com.botmasterzzz.social.config.telegram.BotInstanceContainer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalTime;

@Service
public class KafkaTelegramConsumerImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTelegramConsumerImpl.class);

    private final ObjectMapper objectMapper;

    private static BotInstanceContainer botInstanceContainer = BotInstanceContainer.getInstanse();

    public KafkaTelegramConsumerImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(id = "telegram-message-service", topics = {"telegram-outcome-messages"}, containerFactory = "singleFactory")
    public void consumeMessage(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) Long key, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, OutgoingMessage apiMethod) {
        LOGGER.info("{} => consumed {}", LocalTime.now(), writeValueAsString(apiMethod));
        String type = apiMethod.getTypeMessage();
        try {
            switch (type) {
                case "SendPhoto": {
                    SendPhoto method = objectMapper.readValue(apiMethod.getData(), SendPhoto.class);
                    botInstanceContainer.getBotInstance(key).executePhoto(method);
                    break;
                }
                case "SendVideo": {
                    SendVideo method = objectMapper.readValue(apiMethod.getData(), SendVideo.class);
                    botInstanceContainer.getBotInstance(key).executeVideo(method);
                    break;
                }
                case "SendDocument": {
                    SendDocument method = objectMapper.readValue(apiMethod.getData(), SendDocument.class);
                    botInstanceContainer.getBotInstance(key).executeDocument(method);
                    break;
                }
                case "EditMessageText": {
                    EditMessageText method = objectMapper.readValue(apiMethod.getData(), EditMessageText.class);
                    botInstanceContainer.getBotInstance(key).execute(method);
                    break;
                }
                case "EditMessageReplyMarkup": {
                    EditMessageReplyMarkup method = objectMapper.readValue(apiMethod.getData(), EditMessageReplyMarkup.class);
                    botInstanceContainer.getBotInstance(key).execute(method);
                    break;
                }
                default: {
                    SendMessage method = objectMapper.readValue(apiMethod.getData(), SendMessage.class);
                    botInstanceContainer.getBotInstance(key).execute(method);
                    break;
                }
            }
        } catch (TelegramApiException | IOException ex) {
            LOGGER.error("ERROR TelegramApiException", ex);
        }
    }


    private String writeValueAsString(OutgoingMessage apiMethod) {
        try {
            return objectMapper.writeValueAsString(apiMethod);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Writing value to JSON failed: " + apiMethod.toString());
        }
    }
}
