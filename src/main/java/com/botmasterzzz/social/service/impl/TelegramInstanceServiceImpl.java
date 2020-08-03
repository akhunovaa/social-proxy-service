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

    @Value("${telegram.chelny.kazan.bot.token}")
    private String taxiToken;

    @Value("${telegram.getparts.bot.token}")
    private String getPartsToken;

    @PostConstruct
    private void postConstruct() {
        BotSession botSession = new DefaultBotSession();
        String botName = "Zyxel";
        Long instanceId = 31L;
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

        BotSession taxiBotSession = new DefaultBotSession();
        taxiBotSession.setToken(taxiToken);
        Telegram taxiInstance = applicationContext.getBean(Telegram.class);
        taxiInstance.setToken(taxiToken);
        taxiInstance.setUserName("Такси Челны Казань");
        taxiInstance.setOptions(new DefaultBotOptions());
        taxiInstance.setSession(taxiBotSession);
        taxiInstance.setInstanceId(33L);
        taxiInstance.start();

        LOGGER.info("Telegram taxi bot after service restart has been started. {}", token);
        botInstanceContainer.addTelegramBotInstance(33L, taxiInstance);
        LOGGER.info("Telegram taxi bot after service restart has been added. {}", token);

        BotSession getpartsBotSession = new DefaultBotSession();
        taxiBotSession.setToken(getPartsToken);
        Telegram getpartsInstance = applicationContext.getBean(Telegram.class);
        getpartsInstance.setToken(getPartsToken);
        getpartsInstance.setUserName("GetParts24.ru");
        getpartsInstance.setOptions(new DefaultBotOptions());
        getpartsInstance.setSession(getpartsBotSession);
        getpartsInstance.setInstanceId(1L);
        getpartsInstance.start();

        LOGGER.info("Telegram GetParts24.ru bot after service restart has been started. {}", token);
        botInstanceContainer.addTelegramBotInstance(1L, getpartsInstance);
        LOGGER.info("Telegram GetParts24.ru bot after service restart has been added. {}", token);
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
