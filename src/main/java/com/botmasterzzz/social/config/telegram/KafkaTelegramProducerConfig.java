package com.botmasterzzz.social.config.telegram;

import com.botmasterzzz.bot.api.impl.objects.Message;
import com.botmasterzzz.bot.api.impl.objects.Update;
import com.botmasterzzz.social.dto.KafkaKeyDTO;
import com.botmasterzzz.social.serializer.KafkaKeySerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
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
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaKeySerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return props;
    }

    @Bean
    public Map<String, Object> messageProducerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return props;
    }

    @Bean
    public ProducerFactory<KafkaKeyDTO, Update> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public ProducerFactory<String, Message> producerMessageFactory() {
        return new DefaultKafkaProducerFactory<>(messageProducerConfigs());
    }

    @Bean
    public KafkaTemplate<KafkaKeyDTO, Update> kafkaTemplate() {
        KafkaTemplate<KafkaKeyDTO, Update> template = new KafkaTemplate<>(producerFactory());
        template.setMessageConverter(new StringJsonMessageConverter());
        return template;
    }

    @Bean
    public KafkaTemplate<String, Message> kafkaMessageTemplate() {
        KafkaTemplate<String, Message> template = new KafkaTemplate<>(producerMessageFactory());
        template.setMessageConverter(new StringJsonMessageConverter());
        return template;
    }
}