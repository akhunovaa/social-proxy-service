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
import com.botmasterzzz.social.service.MessageProcess;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TelegramMessageProcessor implements MessageProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramMessageProcessor.class);
    private static final BotInstanceContainer botInstanceContainer = BotInstanceContainer.getInstanse();

    private static final List<String> chatList = new CopyOnWriteArrayList();
    private final ObjectMapper objectMapper;

    @Value(value = "${telegram.response.messages.topic.name}")
    private String topicName;

    private final KafkaTemplate<Long, Message> kafkaMessageTemplate;

    public TelegramMessageProcessor(ObjectMapper objectMapper, KafkaTemplate<Long, Message> kafkaMessageTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaMessageTemplate = kafkaMessageTemplate;
    }

    @Async
    @Override
    public void process(OutgoingMessage apiMethod, Long kafkaKey) {
        String type = apiMethod.getTypeMessage();
        Long instanceId = kafkaKey;

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
                        process(responseMessage, kafkaKey);
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
                            LOGGER.info("File from local send {}", uploadVideoFile.getAbsolutePath());
                        }
                        Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executeVideo(method);
                        LOGGER.info("Successfully received response message from Telegram: {}", objectMapper.writeValueAsString(responseMessage));
                        process(responseMessage, kafkaKey);
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
                    sendChatAction.setChatId(chatId);
                    try {
                        String fileName = method.getDocument().getAttachName();
                        File uploadDocument = new File(fileName);
                        if (uploadDocument.exists()) {
                            sendChatAction.setAction(ActionType.UPLOADDOCUMENT);
                            method.setDocumentInput(new InputFile(uploadDocument, "report_file_" + method.getChatId() + ".xlsx"));
                            LOGGER.info("File from local send {}", uploadDocument.getAbsolutePath());
                        } else {
                            sendChatAction.setAction(ActionType.UPLOADVIDEO);
                        }
                        botInstanceContainer.getBotInstance(instanceId).execute(sendChatAction);
                        Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executeDocument(method);
                        process(responseMessage, kafkaKey);
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
                        String chatId = method.getChatId();
                        String exceptionMessage = telegramApiException.getMessage();
                        String apiException = ((TelegramApiRequestException) telegramApiException).getApiResponse();
                        Integer errorCode = ((TelegramApiRequestException) telegramApiException).getErrorCode();
                        String exceptionMessageToSend = "Exception Message => " + exceptionMessage + " \n" + "Exception Message => " + apiException + " \n" + "Error Code => " + errorCode;
                        try {
                            botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, exceptionMessageToSend));
                            if (errorCode == 429){
                                Thread.sleep(7000L);
                                botInstanceContainer.getBotInstance(instanceId).execute(method);
                            }
                        } catch (TelegramApiException | InterruptedException exception) {
                            LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, telegramApiException);
                            chatId = method.getChatId();
                            exceptionMessage = telegramApiException.getMessage();
                            apiException = ((TelegramApiRequestException) telegramApiException).getApiResponse();
                            errorCode = ((TelegramApiRequestException) telegramApiException).getErrorCode();
                            exceptionMessageToSend = "Exception Message => " + exceptionMessage + " \n" + "Exception Message => " + apiException + " \n" + "Error Code => " + errorCode;
                            try {
                                botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, exceptionMessageToSend));
                                if (errorCode == 429){
                                    Thread.sleep(7000L);
                                    botInstanceContainer.getBotInstance(instanceId).execute(method);
                                }
                            } catch (TelegramApiException | InterruptedException e) {
                                LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, telegramApiException);
                            }
                        }
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
                case "MailingMessagePhoto": {
                    SendPhoto method = objectMapper.readValue(apiMethod.getData(), SendPhoto.class);
                    String chatId = method.getChatId();
                    boolean chatContains = chatList.contains(chatId);
                    if (chatContains) {
                        LOGGER.info("Already sent to this chat id {}", chatId);
                    } else {
                        try {
                            Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executePhoto(method);
                            process(responseMessage, kafkaKey);
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
                case "MailingTextMessage": {
                    SendMessage method = objectMapper.readValue(apiMethod.getData(), SendMessage.class);
                    String chatId = method.getChatId();
                    boolean chatContains = chatList.contains(chatId);
                    if (chatContains) {
                        LOGGER.info("Already sent to this chat id {}", chatId);
                    } else {
                        try {
                            chatList.add(chatId);
                            Message responseMessage = botInstanceContainer.getBotInstance(instanceId).execute(method);
                            process(responseMessage, kafkaKey);
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
                case "MailingMessageVideo": {
                    SendVideo method = objectMapper.readValue(apiMethod.getData(), SendVideo.class);
                    String chatId = method.getChatId();
                    boolean chatContains = chatList.contains(chatId);
                    if (chatContains) {
                        LOGGER.info("Already sent to this chat id {}", chatId);
                    } else {
                        try {
                            chatList.add(chatId);
                            Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executeVideo(method);
                            process(responseMessage, kafkaKey);
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
                case "MailingMessageDocument": {
                    SendDocument method = objectMapper.readValue(apiMethod.getData(), SendDocument.class);
                    String chatId = method.getChatId();
                    boolean chatContains = chatList.contains(chatId);
                    if (chatContains) {
                        LOGGER.info("Already sent to this chat id {}", chatId);
                    } else {
                        try {
                            chatList.add(chatId);
                            Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executeDocument(method);
                            process(responseMessage, kafkaKey);
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
                case "MailingResetVideo": {
                    SendVideo method = objectMapper.readValue(apiMethod.getData(), SendVideo.class);
                    String chatId = method.getChatId();
                    int size = chatList.size();
                    chatList.clear();
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, "Was: " + size + " \n Done!"));
                    } catch (TelegramApiException exception) {
                        LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, exception);
                    }
                    break;
                }
                case "MailingResetDocument": {
                    SendDocument method = objectMapper.readValue(apiMethod.getData(), SendDocument.class);
                    String chatId = method.getChatId();
                    int size = chatList.size();
                    chatList.clear();
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, "Was: " + size + " \n Done!"));
                    } catch (TelegramApiException exception) {
                        LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, exception);
                    }
                    break;
                }
                case "MailingResetPhoto": {
                    SendPhoto method = objectMapper.readValue(apiMethod.getData(), SendPhoto.class);
                    String chatId = method.getChatId();
                    int size = chatList.size();
                    chatList.clear();
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, "Was: " + size + " \n Done!"));
                    } catch (TelegramApiException exception) {
                        LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, exception);
                    }
                    break;
                }
                case "MailingResetMessage": {
                    SendMessage method = objectMapper.readValue(apiMethod.getData(), SendMessage.class);
                    String chatId = method.getChatId();
                    int size = chatList.size();
                    chatList.clear();
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, "Was: " + size + " \n Done!"));
                    } catch (TelegramApiException exception) {
                        LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, exception);
                    }
                    break;
                }
                default: {
                    SendMessage method = objectMapper.readValue(apiMethod.getData(), SendMessage.class);
                    //boolean loading = kafkaKeyDTO.isLoading();
                    String chatId = method.getChatId();
                    SendChatAction sendChatAction = new SendChatAction();
                    sendChatAction.setAction(ActionType.TYPING);
                    sendChatAction.setChatId(chatId);
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendChatAction);
                        Message responseMessage = botInstanceContainer.getBotInstance(instanceId).execute(method);
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

    @Override
    public void process(Message message, Long kafkaKey) {
        Long instanceId = kafkaKey;
        LOGGER.info("Message response for an instance: {} message: {}", instanceId, message.toString());
        LOGGER.info("<= sending {}", message.toString());
        kafkaMessageTemplate.send(topicName, instanceId, message);
    }

    private SendMessage sendBlockActionToAdmin(String chatId, String cause) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(105239613L);
        String userLink = "<a href=\"tg://user?id=" + chatId + "\">" + "USER" + "</a>";
        sendMessage.setText("User =>" + userLink + " \nChat id =>" + chatId + "\nSent to " + chatList.size() + "  persons. \n" + "And this one sent an exception: \n" + cause);
        sendMessage.setParseMode(ParseMode.HTML);
        return sendMessage;
    }
}
