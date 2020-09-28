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

    @Value("${telegram.rune.bot.token}")
    private String runeBotToken;

    @Value("${telegram.style.lady.bot.token}")
    private String styleLadyBotToken;

    @Value("${telegram.search.bot.token}")
    private String searchBotToken;

    @Value("${telegram.chelny.kazan.bot.token}")
    private String taxiToken;

    @Value("${telegram.getparts.bot.token}")
    private String getPartsToken;

    @Value("${telegram.antiparkon.bot.token}")
    private String antiparkonToken;

    @Value("${telegram.portfolio.bot.token}")
    private String portfolioToken;

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

        BotSession runeBotSession = new DefaultBotSession();
        runeBotSession.setToken(runeBotToken);
        Telegram runeBotInstance = applicationContext.getBean(Telegram.class);
        runeBotInstance.setToken(runeBotToken);
        runeBotInstance.setUserName("Руна Дня / Совет Рун");
        runeBotInstance.setOptions(new DefaultBotOptions());
        runeBotInstance.setSession(runeBotSession);
        runeBotInstance.setInstanceId(38L);
        runeBotInstance.start();

        LOGGER.info("Telegram Rune of Day bot after service restart has been started. {}", runeBotToken);
        botInstanceContainer.addTelegramBotInstance(38L, runeBotInstance);
        LOGGER.info("Telegram Rune of Day bot after service restart has been added. {}", runeBotToken);

        BotSession styleLadyBotSession = new DefaultBotSession();
        styleLadyBotSession.setToken(styleLadyBotToken);
        Telegram styleLadyBotInstance = applicationContext.getBean(Telegram.class);
        styleLadyBotInstance.setToken(styleLadyBotToken);
        styleLadyBotInstance.setUserName("Стиль Леди");
        styleLadyBotInstance.setOptions(new DefaultBotOptions());
        styleLadyBotInstance.setSession(styleLadyBotSession);
        styleLadyBotInstance.setInstanceId(41L);
        styleLadyBotInstance.start();

        LOGGER.info("Telegram Style Lady bot after service restart has been started. {}", styleLadyBotToken);
        botInstanceContainer.addTelegramBotInstance(41L, styleLadyBotInstance);
        LOGGER.info("Telegram Style Lady bot after service restart has been added. {}", styleLadyBotToken);

        BotSession searchBotSession = new DefaultBotSession();
        searchBotSession.setToken(searchBotToken);
        Telegram searchBotInstance = applicationContext.getBean(Telegram.class);
        searchBotInstance.setToken(searchBotToken);
        searchBotInstance.setUserName("Search Telegram BOT");
        searchBotInstance.setOptions(new DefaultBotOptions());
        searchBotInstance.setSession(searchBotSession);
        searchBotInstance.setInstanceId(42L);
        searchBotInstance.start();

        LOGGER.info("Telegram Search bot has been started. {}", searchBotToken);
        botInstanceContainer.addTelegramBotInstance(42L, searchBotInstance);
        LOGGER.info("Telegram Search bot has been added. {}", searchBotToken);

        BotSession taxiBotSession = new DefaultBotSession();
        taxiBotSession.setToken(taxiToken);
        Telegram taxiInstance = applicationContext.getBean(Telegram.class);
        taxiInstance.setToken(taxiToken);
        taxiInstance.setUserName("Такси Челны Казань");
        taxiInstance.setOptions(new DefaultBotOptions());
        taxiInstance.setSession(taxiBotSession);
        taxiInstance.setInstanceId(33L);
        taxiInstance.start();

        LOGGER.info("Telegram taxi bot after service restart has been started. {}", taxiToken);
        botInstanceContainer.addTelegramBotInstance(33L, taxiInstance);
        LOGGER.info("Telegram taxi bot after service restart has been added. {}", taxiToken);

        BotSession getpartsBotSession = new DefaultBotSession();
        getpartsBotSession.setToken(getPartsToken);
        Telegram getpartsInstance = applicationContext.getBean(Telegram.class);
        getpartsInstance.setToken(getPartsToken);
        getpartsInstance.setUserName("GetParts24.ru");
        getpartsInstance.setOptions(new DefaultBotOptions());
        getpartsInstance.setSession(getpartsBotSession);
        getpartsInstance.setInstanceId(1L);
        getpartsInstance.start();

        LOGGER.info("Telegram GetParts24.ru bot after service restart has been started. {}", getPartsToken);
        botInstanceContainer.addTelegramBotInstance(1L, getpartsInstance);
        LOGGER.info("Telegram GetParts24.ru bot after service restart has been added. {}", getPartsToken);

        BotSession antiparkonBotSession = new DefaultBotSession();
        antiparkonBotSession.setToken(antiparkonToken);
        Telegram antiparkonInstance = applicationContext.getBean(Telegram.class);
        antiparkonInstance.setToken(antiparkonToken);
        antiparkonInstance.setUserName("АнтиПаркон");
        antiparkonInstance.setOptions(new DefaultBotOptions());
        antiparkonInstance.setSession(antiparkonBotSession);
        antiparkonInstance.setInstanceId(35L);//35L prod
        antiparkonInstance.start();

        LOGGER.info("Telegram AntiParkon bot after service restart has been started. {}", antiparkonToken);
        botInstanceContainer.addTelegramBotInstance(35L, antiparkonInstance);
        LOGGER.info("Telegram AntiParkon bot after service restart has been added. {}", antiparkonToken);//

        BotSession portfolioBotSession = new DefaultBotSession();
        portfolioBotSession.setToken(portfolioToken);
        Telegram portfolioInstance = applicationContext.getBean(Telegram.class);
        portfolioInstance.setToken(portfolioToken);
        portfolioInstance.setUserName("Портфолио");
        portfolioInstance.setOptions(new DefaultBotOptions());
        portfolioInstance.setSession(portfolioBotSession);
        portfolioInstance.setInstanceId(37L);
        portfolioInstance.start();

        LOGGER.info("Telegram Portfolio bot after service restart has been started. {}", portfolioToken);
        botInstanceContainer.addTelegramBotInstance(37L, portfolioInstance);
        LOGGER.info("Telegram Portfolio bot after service restart has been added. {}", portfolioToken);
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
