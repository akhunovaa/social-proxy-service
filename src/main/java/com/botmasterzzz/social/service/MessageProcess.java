package com.botmasterzzz.social.service;

import com.botmasterzzz.bot.api.impl.objects.OutgoingMessage;

public interface MessageProcess {

    void process(OutgoingMessage apiMethod, Long kafkaKey);

}
