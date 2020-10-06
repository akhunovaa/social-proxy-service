package com.botmasterzzz.social.service.impl;

import com.botmasterzzz.bot.api.impl.methods.ActionType;
import com.botmasterzzz.bot.api.impl.methods.AnswerInlineQuery;
import com.botmasterzzz.bot.api.impl.methods.ParseMode;
import com.botmasterzzz.bot.api.impl.methods.send.*;
import com.botmasterzzz.bot.api.impl.methods.update.DeleteMessage;
import com.botmasterzzz.bot.api.impl.methods.update.EditMessageReplyMarkup;
import com.botmasterzzz.bot.api.impl.methods.update.EditMessageText;
import com.botmasterzzz.bot.api.impl.objects.InputFile;
import com.botmasterzzz.bot.api.impl.objects.Message;
import com.botmasterzzz.bot.api.impl.objects.OutgoingMessage;
import com.botmasterzzz.bot.exceptions.TelegramApiException;
import com.botmasterzzz.bot.exceptions.TelegramApiRequestException;
import com.botmasterzzz.social.config.telegram.BotInstanceContainer;
import com.botmasterzzz.social.dto.KafkaKeyDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class KafkaTelegramConsumerImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTelegramConsumerImpl.class);

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, Message> kafkaMessageTemplate;

    private static List<String> chatList = Collections.synchronizedList(new ArrayList<>());

    @Value(value = "${telegram.message.callback.topic.name}")
    private String topicName;

    private static final BotInstanceContainer botInstanceContainer = BotInstanceContainer.getInstanse();

    public KafkaTelegramConsumerImpl(ObjectMapper objectMapper, KafkaTemplate<String, Message> kafkaMessageTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaMessageTemplate = kafkaMessageTemplate;
    }

    @KafkaListener(id = "telegram-message-service", topics = {"telegram-outcome-messages"}, containerFactory = "singleFactory")
    public void consumeMessage(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) KafkaKeyDTO kafkaKeyDTO, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, OutgoingMessage apiMethod) {
        String logValue = null;
        try {
            logValue = writeValueAsString(apiMethod);
        } catch (IOException e) {
            LOGGER.error("Deserialize error", e);
        }
        LOGGER.info("=> consumed {}", logValue);
        String type = apiMethod.getTypeMessage();
        Long instanceId = kafkaKeyDTO.getInstanceKey();
        try {
            switch (type) {
                case "SendPhoto": {
                    SendPhoto method = objectMapper.readValue(apiMethod.getData(), SendPhoto.class);
                    String chatId = method.getChatId();
                    SendChatAction sendChatAction = new SendChatAction();
                    sendChatAction.setAction(ActionType.UPLOADPHOTO);
                    sendChatAction.setChatId(chatId);
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendChatAction);
                        Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executePhoto(method);
                        LOGGER.info("Successfully received response message from Telegram: {}", objectMapper.writeValueAsString(responseMessage));
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a photo to chat id: {} Telegram", chatId, telegramApiException);
                        String exceptionMessage = telegramApiException.getMessage();
                        String apiException = ((TelegramApiRequestException) telegramApiException).getApiResponse();
                        Integer errorCode = ((TelegramApiRequestException) telegramApiException).getErrorCode();
                        String exceptionMessageToSend = "Exception Message => " + exceptionMessage + " \n" + "Exception Message => " + apiException + " \n" + "Error Code => " + errorCode;
                        try {
                            botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, exceptionMessageToSend));
                        } catch (TelegramApiException exception) {
                            LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, telegramApiException);
                        }
                    }
                    break;
                }
                case "SendVideo": {
                    SendVideo method = objectMapper.readValue(apiMethod.getData(), SendVideo.class);
                    String chatId = method.getChatId();
                    SendChatAction sendChatAction = new SendChatAction();
                    sendChatAction.setAction(ActionType.UPLOADVIDEO);
                    sendChatAction.setChatId(chatId);
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendChatAction);
                        String fileName = method.getVideo().getAttachName();
                        File uploadVideoFile = new File(fileName);
                        if (uploadVideoFile.exists()) {
                            method.setVideoInputFile(new InputFile(uploadVideoFile, "upload_file"));
                            LOGGER.info("File from local send {}", uploadVideoFile);
                        }
                        Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executeVideo(method);
                        LOGGER.info("Successfully received response message from Telegram: {}", objectMapper.writeValueAsString(responseMessage));
//                        if (uploadVideoFile.exists()) {
//                            kafkaMessageTemplate.send(topicName, fileName, responseMessage);
//                        }
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a video to chat id: {} Telegram", chatId, telegramApiException);
                        String exceptionMessage = telegramApiException.getMessage();
                        String apiException = ((TelegramApiRequestException) telegramApiException).getApiResponse();
                        Integer errorCode = ((TelegramApiRequestException) telegramApiException).getErrorCode();
                        String exceptionMessageToSend = "Exception Message => " + exceptionMessage + " \n" + "Exception Message => " + apiException + " \n" + "Error Code => " + errorCode;
                        try {
                            botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, exceptionMessageToSend));
                        } catch (TelegramApiException exception) {
                            LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, telegramApiException);
                        }
                    }
                    break;
                }
                case "SendDocument": {
                    SendDocument method = objectMapper.readValue(apiMethod.getData(), SendDocument.class);
                    String chatId = method.getChatId();
                    SendChatAction sendChatAction = new SendChatAction();
                    sendChatAction.setAction(ActionType.UPLOADVIDEO);
                    sendChatAction.setChatId(chatId);
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendChatAction);
                        Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executeDocument(method);
                        LOGGER.info("Successfully received response message from Telegram: {}", objectMapper.writeValueAsString(responseMessage));
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a Document to Telegram", telegramApiException);
                    }
                    break;
                }
                case "EditMessageText": {
                    EditMessageText method = objectMapper.readValue(apiMethod.getData(), EditMessageText.class);
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(method);
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a EditMessageText to Telegram", telegramApiException);
                    }
                    break;
                }
                case "EditMessageReplyMarkup": {
                    EditMessageReplyMarkup method = objectMapper.readValue(apiMethod.getData(), EditMessageReplyMarkup.class);
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(method);
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a EditMessageReplyMarkup to Telegram", telegramApiException);
                    }
                    break;
                }
                case "DeleteMessage": {
                    DeleteMessage method = objectMapper.readValue(apiMethod.getData(), DeleteMessage.class);
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(method);
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a DeleteMessage to Telegram", telegramApiException);
                    }
                    break;
                }
                case "AnswerInlineQuery": {
                    AnswerInlineQuery method = objectMapper.readValue(apiMethod.getData(), AnswerInlineQuery.class);
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(method);
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a AnswerInlineQuery to Telegram", telegramApiException);
                    }
                    break;
                }
                case "MailingMessage": {
                    SendMessage method = objectMapper.readValue(apiMethod.getData(), SendMessage.class);
                    String chatId = method.getChatId();
                    boolean chatContains = chatList.contains(chatId);
                    if (chatContains){
                        LOGGER.info("Already sent to this chat id {}", chatId);
                    }else {
                        try {
                            botInstanceContainer.getBotInstance(instanceId).execute(method);
                            chatList.add(chatId);
                        } catch (TelegramApiException telegramApiException) {
                            LOGGER.error("Error to send a MailingMessage SendMessage to Telegram", telegramApiException);
                            String exceptionMessage = telegramApiException.getMessage();
                            String apiException = ((TelegramApiRequestException) telegramApiException).getApiResponse();
                            Integer errorCode = ((TelegramApiRequestException) telegramApiException).getErrorCode();
                            String exceptionMessageToSend = "Exception Message => " + exceptionMessage + " \n" + "Exception Message => " + apiException + " \n" + "Error Code => " + errorCode;
                            try {
                                botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, exceptionMessageToSend));
                            } catch (TelegramApiException exception) {
                                LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, telegramApiException);
                            }
                        }
                    }
                    break;
                }
                default: {
                    SendMessage method = objectMapper.readValue(apiMethod.getData(), SendMessage.class);
                    boolean loading = kafkaKeyDTO.isLoading();
                    String chatId = method.getChatId();
                    SendChatAction sendChatAction = new SendChatAction();
                    sendChatAction.setAction(ActionType.TYPING);
                    sendChatAction.setChatId(chatId);
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendChatAction);
                        Message responseMessage = botInstanceContainer.getBotInstance(instanceId).execute(method);
                        if (loading) {
                            Integer messageId = responseMessage.getMessageId();
                            for (int i = 0; i < 2; i++) {
                                for (EditMessageText editMessageText : loadingAnimation(chatId, messageId)) {
                                    botInstanceContainer.getBotInstance(instanceId).execute(editMessageText);
                                    try {
                                        Thread.sleep(500L);
                                    } catch (InterruptedException exception) {
                                        LOGGER.error("Loading thread sleep exception", exception);
                                    }
                                }
                            }
                            DeleteMessage deleteMethod = new DeleteMessage();
                            deleteMethod.setMessageId(messageId);
                            deleteMethod.setChatId(chatId);
                            try {
                                botInstanceContainer.getBotInstance(instanceId).execute(deleteMethod);
                            } catch (TelegramApiException telegramApiException) {
                                LOGGER.error("Error to send a loading DeleteMessage to Telegram", telegramApiException);
                            }

                        }
                        LOGGER.info("Successfully received response message from Telegram: {}", objectMapper.writeValueAsString(responseMessage));
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a SendMessage to Telegram", telegramApiException);
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            LOGGER.error("ERROR IOException", ex);
        }
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

    private SendMessage sendBlockActionToAdmin(String chatId, String cause){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(105239613L);
        String userLink = "<a href=\"tg://user?id=" + chatId + "\">" + "USER" + "</a>";
        sendMessage.setText("User =>" + userLink + " \nChat id =>" + chatId + "\nSent to " + chatList.size() + "  persons. \n" + "And this one sent an exception: \n" + cause);
        sendMessage.setParseMode(ParseMode.HTML);
        return sendMessage;
    }

}
