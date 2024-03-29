package com.botmasterzzz.social.config.telegram;

import com.botmasterzzz.bot.api.impl.objects.Message;
import com.botmasterzzz.bot.api.impl.objects.ProfileInfoResponse;
import com.botmasterzzz.bot.api.impl.objects.Update;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTelegramProducerConfig {

    @Value("${kafka.server}")
    private String kafkaServer;

    @Value(value = "${telegram.incoming.messages.topic.name}")
    private String topicName;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        //props.put(ProducerConfig.ACKS_CONFIG, "0");
        //props.put(ProducerConfig.RETRIES_CONFIG, 1);
        return props;
    }

    @Bean
    public ProducerFactory<Long, Update> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ProducerFactory<Long, Message> responseMessageProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ProducerFactory<Long, ProfileInfoResponse> responseProfilePhotosMessageProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<Long, Update> kafkaTemplate() {
        KafkaTemplate<Long, Update> template = new KafkaTemplate<>(producerFactory());
        template.setMessageConverter(new StringJsonMessageConverter());
        return template;
    }

    @Bean
    public KafkaTemplate<Long, Message> kafkaMessageTemplate() {
        KafkaTemplate<Long, Message> template = new KafkaTemplate<>(responseMessageProducerFactory());
        template.setMessageConverter(new StringJsonMessageConverter());
        return template;
    }

    @Bean
    public KafkaTemplate<Long, ProfileInfoResponse> kafkaProfileInfoTemplate() {
        KafkaTemplate<Long, ProfileInfoResponse> template = new KafkaTemplate<>(responseProfilePhotosMessageProducerFactory());
        template.setMessageConverter(new StringJsonMessageConverter());
        return template;
    }
}
