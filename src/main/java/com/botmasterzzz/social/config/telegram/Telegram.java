package com.botmasterzzz.social.config.telegram;

import com.botmasterzzz.bot.TelegramLongPollingBot;
import com.botmasterzzz.bot.api.impl.objects.Update;
import com.botmasterzzz.bot.bot.DefaultBotOptions;
import com.botmasterzzz.bot.generic.BotSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Telegram extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(Telegram.class);

    private String userName;
    private String token;
    private BotSession session;
    private boolean registered;

    @Value(value = "${telegram.incoming.messages.topic.name}")
    private String topicName;

    private KafkaTemplate<Long, Update> kafkaTemplate;

    @Autowired
    public Telegram(KafkaTemplate<Long, Update> kafkaTemplate) {
        super(new DefaultBotOptions());
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public synchronized void onUpdateReceived(final Update update) {
        LOGGER.info("Update received: " + update.toString());
        LOGGER.info("<= sending {}", update.toString());
        kafkaTemplate.send(topicName, update);

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
        session.start();
    }


}
