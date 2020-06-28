package com.botmasterzzz.social.service.impl;

import com.botmasterzzz.bot.bot.DefaultBotOptions;
import com.botmasterzzz.bot.generic.BotSession;
import com.botmasterzzz.bot.updatesreceivers.DefaultBotSession;
import com.botmasterzzz.social.config.telegram.Telegram;
import com.botmasterzzz.social.dao.TelegramInstanceDAO;
import com.botmasterzzz.social.service.TelegramInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelegramInstanceServiceImpl implements TelegramInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramInstanceServiceImpl.class);

    private TelegramInstanceDAO telegramInstanceDAO;
    private ApplicationContext applicationContext;

    @Autowired
    public TelegramInstanceServiceImpl(TelegramInstanceDAO telegramInstanceDAO, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.telegramInstanceDAO = telegramInstanceDAO;
    }

    @Override
    public void botStart() {
        Telegram telegramInstance = applicationContext.getBean(Telegram.class);

        BotSession botSession = new DefaultBotSession();
        String token = "527844587:AAGkYqISxP0gcNTAN3telufS5mr174C5E8Y";
        String botName = "Super TestBot";
        botSession.setToken(token);

        telegramInstance.setToken(token);
        telegramInstance.setUserName(botName);
        telegramInstance.setOptions(new DefaultBotOptions());
        telegramInstance.setSession(botSession);
        telegramInstance.start();

        LOGGER.info("Telegram bot has been started: {}", botName);
    }
}
