package com.botmasterzzz.social.config.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BotInstanceContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotInstanceContainer.class);

    private Map<Long, Telegram> telegramInstanceMap;

    public static BotInstanceContainer getInstanse() {
        return Holder.INST;
    }

    public void addTelegramBotInstance(Long id, Telegram telegram) {
        if (telegramInstanceMap.containsKey(id)) {
            LOGGER.info("Bot instance {} already added", id);
        }
        telegramInstanceMap.put(id, telegram);
    }

    public boolean isBotAdded(Long id) {
        return telegramInstanceMap.containsKey(id);
    }

    public Telegram getBotInstance(Long id) {
        return telegramInstanceMap.get(id);
    }

    private BotInstanceContainer() {
        telegramInstanceMap = new ConcurrentHashMap<>();
    }

    private static class Holder {
        static final BotInstanceContainer INST = new BotInstanceContainer();
    }

    public Map<Long, Telegram> getTelegramInstanceMap() {
        return telegramInstanceMap;
    }
}