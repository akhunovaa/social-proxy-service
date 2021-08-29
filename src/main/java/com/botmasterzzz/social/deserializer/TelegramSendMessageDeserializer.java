package com.botmasterzzz.social.deserializer;

import com.botmasterzzz.bot.api.impl.methods.send.SendMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

public class TelegramSendMessageDeserializer implements Deserializer<SendMessage> {

    @Override
    public SendMessage deserialize(String arg0, byte[] arg1) {
        ObjectMapper mapper = new ObjectMapper();
        SendMessage data = null;
        try {
            data = mapper.readValue(arg1, SendMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
