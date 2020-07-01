package com.botmasterzzz.social.service.impl;

import com.botmasterzzz.bot.bot.DefaultBotOptions;
import com.botmasterzzz.bot.generic.BotSession;
import com.botmasterzzz.bot.updatesreceivers.DefaultBotSession;
import com.botmasterzzz.social.config.telegram.BotInstanceContainer;
import com.botmasterzzz.social.config.telegram.Telegram;
import com.botmasterzzz.social.dao.TelegramInstanceDAO;
import com.botmasterzzz.social.service.TelegramInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TelegramInstanceServiceImpl implements TelegramInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramInstanceServiceImpl.class);

    private static BotInstanceContainer botInstanceContainer = BotInstanceContainer.getInstanse();

    private TelegramInstanceDAO telegramInstanceDAO;
    private ApplicationContext applicationContext;

    @Value("${telegram.bot.token}")
    private String token;

    @PostConstruct
    private void postConstruct() {
        BotSession botSession = new DefaultBotSession();
        String botName = "Zyxel";
        Long instanceId = 1L;
        botSession.setToken(token);
        Telegram telegramInstance = applicationContext.getBean(Telegram.class);
        telegramInstance.setToken(token);
        telegramInstance.setUserName(botName);
        telegramInstance.setOptions(new DefaultBotOptions());
        telegramInstance.setSession(botSession);
        telegramInstance.setInstanceId(instanceId);
        telegramInstance.start();
        LOGGER.info("Telegram bot after service restart has been started. {}", token);
        botInstanceContainer.addTelegramBotInstance(instanceId, telegramInstance);
        LOGGER.info("Telegram bot after service restart has been added. {}", token);
    }

    @Autowired
    public TelegramInstanceServiceImpl(TelegramInstanceDAO telegramInstanceDAO, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.telegramInstanceDAO = telegramInstanceDAO;
    }

    @Override
    public void botStart() {
        LOGGER.info("Telegram bot has been started: {}", "Zyxel");
    }
}
