package com.botmasterzzz.social.config.telegram;

import com.botmasterzzz.bot.TelegramLongPollingBot;
import com.botmasterzzz.bot.api.impl.methods.ActionType;
import com.botmasterzzz.bot.api.impl.methods.send.SendChatAction;
import com.botmasterzzz.bot.api.impl.objects.Update;
import com.botmasterzzz.bot.bot.DefaultBotOptions;
import com.botmasterzzz.bot.exceptions.TelegramApiException;
import com.botmasterzzz.bot.generic.BotSession;
import com.botmasterzzz.social.dto.KafkaKeyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component
public class Telegram extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(Telegram.class);

    private Long instanceId;
    private String userName;
    private String token;
    private BotSession session;
    private DefaultBotOptions options;
    private boolean registered;

    @Value(value = "${telegram.incoming.messages.topic.name}")
    private String topicName;

    private final KafkaTemplate<KafkaKeyDTO, Update> kafkaTemplate;

    @Autowired
    public Telegram(KafkaTemplate<KafkaKeyDTO, Update> kafkaTemplate) {
        super(new DefaultBotOptions());
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void onUpdateReceived(final Update update) {
        LOGGER.info("Update received for an instance: {} update: {}", this.instanceId, update.toString());
        LOGGER.info("<= sending {}", update.toString());
        KafkaKeyDTO kafkaKeyDTO = new KafkaKeyDTO();
        kafkaKeyDTO.setInstanceKey(this.instanceId);
        kafkaKeyDTO.setUpdateId(update.getUpdateId());
        kafkaTemplate.send(topicName, kafkaKeyDTO, update);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public BotSession getSession() {
        return session;
    }

    public void setSession(BotSession session) {
        this.session = session;
    }

    public void setOptions(DefaultBotOptions options) {
        this.options = options;
    }

    @Override
    public String getBotUsername() {
        return this.userName;
    }

    @Override
    public String getBotToken() {
        return this.token;
    }

    public void start() {
        if (!session.isRunning() && registered) {
            session.start();
        } else if (!session.isRunning()) {
            registerBot();
            this.registered = Boolean.TRUE;
        }
    }

    public void stop() {
        if (session.isRunning()) {
            session.stop();
        }
    }

    public boolean botStatus() {
        return session.isRunning();
    }

    private void registerBot() {
        session.setCallback(this);
        session.setOptions(options);
        session.start();
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }
}
