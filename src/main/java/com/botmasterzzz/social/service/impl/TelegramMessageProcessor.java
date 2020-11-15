package com.botmasterzzz.social.service.impl;

import com.botmasterzzz.bot.api.impl.methods.ActionType;
import com.botmasterzzz.bot.api.impl.methods.AnswerCallbackQuery;
import com.botmasterzzz.bot.api.impl.methods.AnswerInlineQuery;
import com.botmasterzzz.bot.api.impl.methods.ParseMode;
import com.botmasterzzz.bot.api.impl.methods.send.*;
import com.botmasterzzz.bot.api.impl.methods.update.DeleteMessage;
import com.botmasterzzz.bot.api.impl.methods.update.EditMessageReplyMarkup;
import com.botmasterzzz.bot.api.impl.methods.update.EditMessageText;
import com.botmasterzzz.bot.api.impl.objects.InputFile;
import com.botmasterzzz.bot.api.impl.objects.Message;
import com.botmasterzzz.bot.api.impl.objects.OutgoingMessage;
import com.botmasterzzz.bot.api.impl.objects.replykeyboard.InlineKeyboardMarkup;
import com.botmasterzzz.bot.api.impl.objects.replykeyboard.buttons.InlineKeyboardButton;
import com.botmasterzzz.bot.exceptions.TelegramApiException;
import com.botmasterzzz.bot.exceptions.TelegramApiRequestException;
import com.botmasterzzz.social.config.telegram.BotInstanceContainer;
import com.botmasterzzz.social.dto.CallBackData;
import com.botmasterzzz.social.service.MessageProcess;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Service
public class TelegramMessageProcessor implements MessageProcess {

    private static final int TO_MANY_REQUESTS_ERROR_CODE = 429;
    private static final long ONE_MINUTE_IN_MILLIS = 60000;
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramMessageProcessor.class);
    private static final BotInstanceContainer botInstanceContainer = BotInstanceContainer.getInstanse();
    private static final List<String> chatList = new CopyOnWriteArrayList();
    private static final List<String> validChatList = new CopyOnWriteArrayList();
    public static Map<Long, Long> blockedList = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<Long, Message> kafkaMessageTemplate;
    @Value(value = "${telegram.response.messages.topic.name}")
    private String topicName;

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
                    Long currentTime = System.currentTimeMillis();
                    Long requestedUserId = Long.valueOf(chatId);
                    Long bannedTime = blockedList.getOrDefault(requestedUserId, currentTime);
                    SendChatAction sendChatAction = new SendChatAction();
                    sendChatAction.setAction(ActionType.UPLOADPHOTO);
                    sendChatAction.setChatId(chatId);
                    try {
                        if (bannedTime <= currentTime) {
                            botInstanceContainer.getBotInstance(instanceId).execute(sendChatAction);
                            Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executePhoto(method);
                            LOGGER.info("Successfully received response message from Telegram: {}", objectMapper.writeValueAsString(responseMessage));
                            process(responseMessage, kafkaKey);
                        } else {
                            blockedList.put(requestedUserId, bannedTime + ONE_MINUTE_IN_MILLIS / 20);
                            botInstanceContainer.getBotInstance(instanceId).execute(sendRestrictedMessage(chatId));
                        }
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a photo to chat id: {} Telegram", chatId, telegramApiException);
                        String exceptionMessage = telegramApiException.getMessage();
                        String exceptionMessageToSend = "Exception Message => " + exceptionMessage;
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
                    Long currentTime = System.currentTimeMillis();
                    Long requestedUserId = Long.valueOf(chatId);
                    Long bannedTime = blockedList.getOrDefault(requestedUserId, currentTime);
                    SendChatAction sendChatAction = new SendChatAction();
                    sendChatAction.setAction(ActionType.UPLOADVIDEO);
                    sendChatAction.setChatId(chatId);
                    try {
                        if (bannedTime <= currentTime) {
                            botInstanceContainer.getBotInstance(instanceId).execute(sendChatAction);
                            String fileName = method.getVideo().getAttachName();
                            File uploadVideoFile = new File(fileName);
                            if (uploadVideoFile.exists()) {
                                method.setVideoInputFile(new InputFile(uploadVideoFile, uploadVideoFile.getName()));
                                LOGGER.info("File from local send {}", uploadVideoFile.getAbsolutePath());
                            }
                            Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executeVideo(method);
                            LOGGER.info("Successfully received response message from Telegram: {}", objectMapper.writeValueAsString(responseMessage));
                            process(responseMessage, kafkaKey);
                        } else {
                            blockedList.put(requestedUserId, bannedTime + ONE_MINUTE_IN_MILLIS / 20);
                            botInstanceContainer.getBotInstance(instanceId).execute(sendRestrictedMessage(chatId));
                        }
//                        if (uploadVideoFile.exists()) {
//                            kafkaMessageTemplate.send(topicName, fileName, responseMessage);
//                        }
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a photo to chat id: {} Telegram", chatId, telegramApiException);
                        String exceptionMessage = telegramApiException.getMessage();
                        String exceptionMessageToSend = "Exception Message => " + exceptionMessage;
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
                    Long currentTime = System.currentTimeMillis();
                    Long requestedUserId = Long.valueOf(chatId);
                    Long bannedTime = blockedList.getOrDefault(requestedUserId, currentTime);
                    SendChatAction sendChatAction = new SendChatAction();
                    sendChatAction.setChatId(chatId);
                    try {
                        String fileName = method.getDocument().getAttachName();
                        File uploadDocument = new File(fileName);
                        if (uploadDocument.exists()) {
                            String mediaName = fileName.contains(".xlsx") ? "report_file_" + method.getChatId() + ".xlsx" : method.getMediaName();
                            method.setDocumentInput(new InputFile(uploadDocument, mediaName));
                            method.setMediaName(null);
                            if (null != method.getThumb() && null != method.getThumb().getAttachName()) {
                                String thumbPath = method.getThumb().getAttachName();
                                File thumbFile = new File(thumbPath);
                                if (thumbFile.exists()) {
                                    String thumbnailsMediaName = mediaName.substring(0, mediaName.lastIndexOf(".mp3")) + ".jpg";
                                    method.setThumb(new InputFile(thumbFile, thumbnailsMediaName));
                                }
                            } else if(null != method.getThumb() && null == method.getThumb().getAttachName()){
                                method.getThumb().setMedia(mediaName);
                            }
                            sendChatAction.setAction(ActionType.UPLOADDOCUMENT);
                            LOGGER.info("File from local send {}", uploadDocument.getAbsolutePath());
                        } else {
                            if(null != method.getThumb() && null == method.getThumb().getAttachName()){
                                method.getThumb().setMedia(fileName);
                            }
                            sendChatAction.setAction(ActionType.UPLOADVIDEO);
                        }

                        if (bannedTime <= currentTime) {
                            botInstanceContainer.getBotInstance(instanceId).execute(sendChatAction);
                            Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executeDocument(method);
                            process(responseMessage, kafkaKey);
                            LOGGER.info("Successfully received response message from Telegram: {}", objectMapper.writeValueAsString(responseMessage));
                        } else {
                            blockedList.put(requestedUserId, bannedTime + ONE_MINUTE_IN_MILLIS / 20);
                            botInstanceContainer.getBotInstance(instanceId).execute(sendRestrictedMessage(chatId));
                        }
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a Document to Telegram", telegramApiException);
                    }
                    break;
                }
                case "EditMessageText": {
                    EditMessageText method = objectMapper.readValue(apiMethod.getData(), EditMessageText.class);
                    try {
                        Message responseMessage = (Message) botInstanceContainer.getBotInstance(instanceId).execute(method);
                        process(responseMessage, kafkaKey);
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a EditMessageText to Telegram", telegramApiException);
                    }
                    break;
                }
                case "EditMessageReplyMarkup": {
                    EditMessageReplyMarkup method = objectMapper.readValue(apiMethod.getData(), EditMessageReplyMarkup.class);
                    Long currentTime = System.currentTimeMillis();
                    String callBackData = method.getReplyMarkup().getKeyboard().get(0).get(0).getCallbackData();
                    Long requestedUserId = objectMapper.readValue(callBackData, CallBackData.class).getUd();
                    Long bannedTime = blockedList.getOrDefault(requestedUserId, currentTime);
                    try {
                        if (bannedTime <= currentTime) {
                            botInstanceContainer.getBotInstance(instanceId).execute(method);
                        } else {
                            blockedList.put(requestedUserId, bannedTime + ONE_MINUTE_IN_MILLIS / 20);
                        }
                        //Message responseMessage = (Message) botInstanceContainer.getBotInstance(instanceId).execute(method);
                        //process(responseMessage, kafkaKey);
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a EditMessageReplyMarkup to Telegram", telegramApiException);
                        Integer errorCode = ((TelegramApiRequestException) telegramApiException).getErrorCode();
                        if (errorCode == TO_MANY_REQUESTS_ERROR_CODE) {
                            bannedTime = blockedList.getOrDefault(requestedUserId, currentTime);
                            blockedList.put(requestedUserId, bannedTime + ONE_MINUTE_IN_MILLIS / 6);
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
                case "AnswerCallbackQuery": {
                    AnswerCallbackQuery method = objectMapper.readValue(apiMethod.getData(), AnswerCallbackQuery.class);
                    Long requestedUserId = method.getRequestedUserId();
                    Long currentTime = System.currentTimeMillis();
                    Long bannedTime = blockedList.getOrDefault(requestedUserId, currentTime);
                    try {
                        if (bannedTime > currentTime) {
                            String warning = "⚠️Слишком частые запросы! \n\n⚠️Very often requests! \n\n⏳Попробуйте через " + TimeUnit.MILLISECONDS.toSeconds(bannedTime - currentTime) + " секунд!" +
                                    "\n\n⏳Try through " + TimeUnit.MILLISECONDS.toSeconds(bannedTime - currentTime) + " seconds!";
                            method.setText(warning);
                            method.setShowAlert(Boolean.TRUE);
                        }
                        botInstanceContainer.getBotInstance(instanceId).execute(method);
                    } catch (TelegramApiException telegramApiException) {
                        LOGGER.error("Error to send a AnswerCallbackQuery to Telegram", telegramApiException);
                    }
                    break;
                }
                case "EditMessageReplyMarkupMailing": {
                    EditMessageReplyMarkup method = objectMapper.readValue(apiMethod.getData(), EditMessageReplyMarkup.class);
                    String chatId = method.getChatId();
                    boolean chatContains = chatList.contains(chatId);
                    if (chatContains) {
                        LOGGER.info("Already edited this chat id {}", chatId);
                    } else {
                        try {
                            chatList.add(chatId);
                            botInstanceContainer.getBotInstance(instanceId).execute(method);
                            validChatList.add(chatId);
                        } catch (TelegramApiException telegramApiException) {
                            LOGGER.error("Error to send a MailingMessage EditMessageReplyMarkup to Telegram", telegramApiException);
                        }
                    }
                    break;
                }
                case "MailingTextMessage": {
                    SendMessage method = objectMapper.readValue(apiMethod.getData(), SendMessage.class);
                    method.setReplyToMessageId(null);
                    String chatId = method.getChatId();
                    boolean chatContains = chatList.contains(chatId);
                    if (chatContains) {
                        LOGGER.info("Already sent to this chat id {}", chatId);
                    } else {
                        try {
                            chatList.add(chatId);
                            Message responseMessage = botInstanceContainer.getBotInstance(instanceId).execute(method);
                            process(responseMessage, kafkaKey);
                            validChatList.add(chatId);
                        } catch (TelegramApiException telegramApiException) {
                            LOGGER.error("Error to send a MailingMessage SendMessage to Telegram", telegramApiException);
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
                            validChatList.add(chatId);
                        } catch (TelegramApiException telegramApiException) {
                            LOGGER.error("Error to send a MailingMessage SendMessage to Telegram", telegramApiException);
//                            String exceptionMessage = telegramApiException.getMessage();
//                            String exceptionMessageToSend = "Exception Message => " + exceptionMessage;  try {
//                                botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, exceptionMessageToSend));
//                            } catch (TelegramApiException exception) {
//                                LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, telegramApiException);
//                            }
                        }
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
                            chatList.add(chatId);
                            Message responseMessage = botInstanceContainer.getBotInstance(instanceId).executePhoto(method);
                            process(responseMessage, kafkaKey);
                            validChatList.add(chatId);
                        } catch (TelegramApiException telegramApiException) {
                            LOGGER.error("Error to send a MailingMessage SendPhoto to Telegram", telegramApiException);
//                            String exceptionMessage = telegramApiException.getMessage();
//                            String exceptionMessageToSend = "Exception Message => " + exceptionMessage;
//                            try {
//                                botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, exceptionMessageToSend));
//                            } catch (TelegramApiException exception) {
//                                LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, telegramApiException);
//                            }
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
                            validChatList.add(chatId);
                        } catch (TelegramApiException telegramApiException) {
                            LOGGER.error("Error to send a MailingMessage SendMessage to Telegram", telegramApiException);
//                            String exceptionMessage = telegramApiException.getMessage();
//                            String exceptionMessageToSend = "Exception Message => " + exceptionMessage;
//                            try {
//                                botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, exceptionMessageToSend));
//                            } catch (TelegramApiException exception) {
//                                LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, telegramApiException);
//                            }
                        }
                    }
                    break;
                }
                case "MailingResetVideo": {
                    SendVideo method = objectMapper.readValue(apiMethod.getData(), SendVideo.class);
                    String chatId = method.getChatId();
                    int size = chatList.size();
                    int validSize = validChatList.size();
                    chatList.clear();
                    validChatList.clear();
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, "Was: " + size + "\nValid chatList size: " + validSize + "\nDone!"));
                    } catch (TelegramApiException exception) {
                        LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, exception);
                    }
                    break;
                }
                case "MailingResetDocument": {
                    SendDocument method = objectMapper.readValue(apiMethod.getData(), SendDocument.class);
                    String chatId = method.getChatId();
                    int size = chatList.size();
                    int validSize = validChatList.size();
                    chatList.clear();
                    validChatList.clear();
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, "Was: " + size + "\nValid chatList size: " + validSize + "\nDone!"));
                    } catch (TelegramApiException exception) {
                        LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, exception);
                    }
                    break;
                }
                case "MailingResetPhoto": {
                    SendPhoto method = objectMapper.readValue(apiMethod.getData(), SendPhoto.class);
                    String chatId = method.getChatId();
                    int size = chatList.size();
                    int validSize = validChatList.size();
                    chatList.clear();
                    validChatList.clear();
                    try {
                        botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, "Was: " + size + "\nValid chatList size: " + validSize + "\nDone!"));
                    } catch (TelegramApiException exception) {
                        LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, exception);
                    }
                    break;
                }
                case "MailingResetMessage": {
                    SendMessage method = objectMapper.readValue(apiMethod.getData(), SendMessage.class);
                    method.setReplyToMessageId(null);
                    InlineKeyboardMarkup cancelReplyInnerKeyboard = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> inlineKeyboardButtons = new ArrayList<>();
                    List<InlineKeyboardButton> inlineKeyboardButtonsFirstRow = new ArrayList<>();

                    InlineKeyboardButton cancelInlineButton = new InlineKeyboardButton();
                    cancelInlineButton.setText("❌Закрыть");
                    try {
                        cancelInlineButton.setCallbackData(objectMapper.writeValueAsString(new CallBackData("mailcnsl")));
                    } catch (JsonProcessingException exception) {
                        exception.printStackTrace();
                    }
                    inlineKeyboardButtonsFirstRow.add(cancelInlineButton);
                    inlineKeyboardButtons.add(inlineKeyboardButtonsFirstRow);
                    cancelReplyInnerKeyboard.setKeyboard(inlineKeyboardButtons);
                    method.setReplyMarkup(cancelReplyInnerKeyboard);
                    String chatId = method.getChatId();
                    int size = chatList.size();
                    int validSize = validChatList.size();
                    chatList.clear();
                    validChatList.clear();
                    try {
                        String message = "Сброшен счетчик по кол-ву отправленных сообщений: " + size + "\nИз успешно доставлено адресатам: " + validSize + "\n\n" + method.getText();
                        method.setText(message);
                        botInstanceContainer.getBotInstance(instanceId).execute(method);
                    } catch (TelegramApiException exception) {
                        LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, exception);
                    }
                    break;
                }
                case "MailingInfoMessage": {
                    SendMessage method = objectMapper.readValue(apiMethod.getData(), SendMessage.class);
                    String chatId = method.getChatId();
                    int size = chatList.size();
                    int validSize = validChatList.size();
                    int fullMailData = null == method.getReplyToMessageId() ? 1 : method.getReplyToMessageId();
                    int percentage = null == method.getReplyToMessageId() ? 0 : (size * 100 / fullMailData);
                    method.setReplyToMessageId(null);
                    StringBuilder stringBuilder = new StringBuilder();
                    if (percentage == 0) {
                        stringBuilder.append("⚠️Ожидаются подготовленные данные для старта рассылки на сервера <b>Telegram</b>");
                    } else if (percentage == 100) {
                        stringBuilder.append("✅Рассылка успешно обработана");
                    } else {
                        stringBuilder.append("⚠️Идёт отправка обработанной рассылки на сервера <b>Telegram</b>");
                    }
                    stringBuilder.append("\n").append("      ").append(percentage).append("% \uD83D\uDEA7\n");
                    stringBuilder.append("[");
                    for (int i = 0; i < percentage; i++) {
                        stringBuilder.append("|");
                    }
                    for (int i = 0; i < 100 - (percentage); i++) {
                        stringBuilder.append(" ");
                    }
                    stringBuilder.append("]");
                    InlineKeyboardMarkup cancelReplyInnerKeyboard = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> inlineKeyboardButtons = new ArrayList<>();
                    List<InlineKeyboardButton> inlineKeyboardButtonsFirstRow = new ArrayList<>();

                    InlineKeyboardButton cancelInlineButton = new InlineKeyboardButton();
                    cancelInlineButton.setText("❌Закрыть");
                    try {
                        cancelInlineButton.setCallbackData(objectMapper.writeValueAsString(new CallBackData("mailcnsl")));
                    } catch (JsonProcessingException exception) {
                        exception.printStackTrace();
                    }
                    inlineKeyboardButtonsFirstRow.add(cancelInlineButton);
                    inlineKeyboardButtons.add(inlineKeyboardButtonsFirstRow);
                    cancelReplyInnerKeyboard.setKeyboard(inlineKeyboardButtons);
                    method.setReplyMarkup(cancelReplyInnerKeyboard);
                    try {
                        String message = "Cчётчик по кол-ву отправленных сообщений: " + size + "\nИз успешно доставлено адресатам: " + validSize + "\n\n";
                        method.setText(message + stringBuilder.toString());
                        botInstanceContainer.getBotInstance(instanceId).execute(method);
                        //botInstanceContainer.getBotInstance(instanceId).execute(sendBlockActionToAdmin(chatId, message));
                    } catch (TelegramApiException exception) {
                        LOGGER.error("Error to send a message to chat id: {} Telegram", chatId, exception);
                    }
                    break;
                }
                default: {
                    SendMessage method = objectMapper.readValue(apiMethod.getData(), SendMessage.class);
                    method.setReplyToMessageId(null);
                    //boolean loading = kafkaKeyDTO.isLoading();
                    String chatId = method.getChatId();
                    Long currentTime = System.currentTimeMillis();
                    Long requestedUserId = Long.valueOf(chatId);
                    Long bannedTime = blockedList.getOrDefault(requestedUserId, currentTime);
                    SendChatAction sendChatAction = new SendChatAction();
                    sendChatAction.setAction(ActionType.TYPING);
                    sendChatAction.setChatId(chatId);
                    try {
                        if (bannedTime <= currentTime) {
                            botInstanceContainer.getBotInstance(instanceId).execute(sendChatAction);
                            Message responseMessage = botInstanceContainer.getBotInstance(instanceId).execute(method);
                            process(responseMessage, kafkaKey);
                            LOGGER.info("Successfully received response message from Telegram: {}", objectMapper.writeValueAsString(responseMessage));
                        } else {
                            blockedList.put(requestedUserId, bannedTime + ONE_MINUTE_IN_MILLIS / 20);
                            botInstanceContainer.getBotInstance(instanceId).execute(sendRestrictedMessage(chatId));
                        }

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
        sendMessage.setText("User =>" + userLink + " \nChat id =>" + chatId + "\nSent to " + chatList.size() + "  persons.\n" + "And this one sent an exception:\n" + cause);
        sendMessage.setParseMode(ParseMode.HTML);
        return sendMessage;
    }

    private SendMessage sendRestrictedMessage(String chatId) {
        Long currentTime = System.currentTimeMillis();
        Long requestedUserId = Long.valueOf(chatId);
        Long bannedTime = blockedList.getOrDefault(requestedUserId, currentTime);
        String warning = "⚠️Слишком частые запросы! \n\n⚠️Very often requests! \n\n⏳Попробуйте через " + TimeUnit.MILLISECONDS.toSeconds(bannedTime - currentTime) + " секунд!" +
                "\n\n⏳Try through " + TimeUnit.MILLISECONDS.toSeconds(bannedTime - currentTime) + " seconds!";

        InlineKeyboardMarkup cancelReplyInnerKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboardButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonsFirstRow = new ArrayList<>();

        InlineKeyboardButton cancelInlineButton = new InlineKeyboardButton();
        cancelInlineButton.setText("❌Закрыть | ❌Close");
        try {
            cancelInlineButton.setCallbackData(objectMapper.writeValueAsString(new CallBackData("timecnsl")));
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }
        inlineKeyboardButtonsFirstRow.add(cancelInlineButton);
        inlineKeyboardButtons.add(inlineKeyboardButtonsFirstRow);
        cancelReplyInnerKeyboard.setKeyboard(inlineKeyboardButtons);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(warning);
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.setReplyMarkup(cancelReplyInnerKeyboard);
        return sendMessage;
    }
}
