package com.botmasterzzz.social.service.impl;

import com.botmasterzzz.bot.api.impl.methods.send.SendDocument;
import com.botmasterzzz.bot.api.impl.methods.send.SendMessage;
import com.botmasterzzz.bot.api.impl.methods.send.SendPhoto;
import com.botmasterzzz.bot.api.impl.methods.send.SendVideo;
import com.botmasterzzz.bot.api.impl.methods.update.DeleteMessage;
import com.botmasterzzz.bot.api.impl.methods.update.EditMessageReplyMarkup;
import com.botmasterzzz.bot.api.impl.methods.update.EditMessageText;
import com.botmasterzzz.bot.api.impl.objects.OutgoingMessage;
import com.botmasterzzz.social.config.telegram.BotInstanceContainer;
import com.botmasterzzz.social.service.MessageProcess;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class KafkaTelegramConsumerImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTelegramConsumerImpl.class);
    private final ObjectMapper objectMapper;
    private final MessageProcess messageProcess;

    public KafkaTelegramConsumerImpl(ObjectMapper objectMapper, MessageProcess messageProcess) {
        this.objectMapper = objectMapper;
        this.messageProcess = messageProcess;
    }

    @KafkaListener(id = "telegram-message-service", topics = {"tg-outcome-message"}, containerFactory = "singleFactory")
    public void consumeMessage(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) Long kafkaKey, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, OutgoingMessage apiMethod) {
        String logValue = null;
        try {
            logValue = writeValueAsString(apiMethod);
        } catch (IOException e) {
            LOGGER.error("Deserialize error", e);
        }
        LOGGER.info("=> consumed {}", logValue);
        messageProcess.process(apiMethod, kafkaKey);
    }


    private String writeValueAsString(OutgoingMessage apiMethod) throws IOException {
        String type = apiMethod.getTypeMessage();
        switch (type) {
            case "SendPhoto": {
                return objectMapper.writeValueAsString(objectMapper.readValue(apiMethod.getData(), SendPhoto.class));
            }
            case "SendVideo": {
                return objectMapper.writeValueAsString(objectMapper.readValue(apiMethod.getData(), SendVideo.class));
            }
            case "SendDocument": {
                return objectMapper.writeValueAsString(objectMapper.readValue(apiMethod.getData(), SendDocument.class));
            }
            case "EditMessageText": {
                return objectMapper.writeValueAsString(objectMapper.readValue(apiMethod.getData(), EditMessageText.class));
            }
            case "EditMessageReplyMarkup": {
                return objectMapper.writeValueAsString(objectMapper.readValue(apiMethod.getData(), EditMessageReplyMarkup.class));
            }
            case "DeleteMessage": {
                return objectMapper.writeValueAsString(objectMapper.readValue(apiMethod.getData(), DeleteMessage.class));
            }
            default: {
                return objectMapper.writeValueAsString(objectMapper.readValue(apiMethod.getData(), SendMessage.class));
            }
        }
    }

    public List<EditMessageText> loadingAnimation(String chatId, Integer messageId) {
        List<EditMessageText> mailingData = new ArrayList<>();

        EditMessageText editMessageTextOne = new EditMessageText();
        editMessageTextOne.setChatId(chatId);
        editMessageTextOne.setText("^('-')^");
        editMessageTextOne.setMessageId(messageId);
        mailingData.add(editMessageTextOne);

        EditMessageText editMessageTextTwo = new EditMessageText();
        editMessageTextTwo.setChatId(chatId);
        editMessageTextTwo.setText("<('-'<)");
        editMessageTextTwo.setMessageId(messageId);
        mailingData.add(editMessageTextTwo);

        EditMessageText editMessageTextThree = new EditMessageText();
        editMessageTextThree.setChatId(chatId);
        editMessageTextThree.setText("^('-')^");
        editMessageTextThree.setMessageId(messageId);
        mailingData.add(editMessageTextThree);

        EditMessageText editMessageTextFour = new EditMessageText();
        editMessageTextFour.setChatId(chatId);
        editMessageTextFour.setText("(>'-')>");
        editMessageTextFour.setMessageId(messageId);
        mailingData.add(editMessageTextFour);

        return mailingData;
    }
}
