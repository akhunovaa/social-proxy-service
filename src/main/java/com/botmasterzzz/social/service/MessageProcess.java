package com.botmasterzzz.social.service;

import com.botmasterzzz.bot.api.impl.objects.Message;
import com.botmasterzzz.bot.api.impl.objects.OutgoingMessage;

public interface MessageProcess {

    void process(OutgoingMessage apiMethod, Long kafkaKey);

    void process(Message message, Long kafkaKey);

}
