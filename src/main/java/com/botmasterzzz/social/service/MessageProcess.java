package com.botmasterzzz.social.service;

import com.botmasterzzz.bot.api.impl.objects.Message;
import com.botmasterzzz.bot.api.impl.objects.OutgoingMessage;
import com.botmasterzzz.bot.api.impl.objects.ProfileInfoResponse;

public interface MessageProcess {

    void process(OutgoingMessage apiMethod, Long kafkaKey);

    void process(Message message, Long kafkaKey);

    void process(ProfileInfoResponse profileInfoResponse, Long kafkaKey);

}
