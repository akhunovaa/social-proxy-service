package com.botmasterzzz.social.config.telegram;

import com.botmasterzzz.bot.api.impl.objects.OutgoingMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
public class KafkaTelegramConsumerConfig {

    @Value("${kafka.server}")
    private String kafkaServer;

    @Value("${telegram.group.id}")
    private String groupId;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, OutgoingMessage> singleFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, OutgoingMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(messageConsumerFactory());
        factory.setBatchListener(false);
        factory.setConcurrency(5);
        factory.setRetryTemplate(kafkaRetry());
        factory.setRecoveryCallback(retryContext -> Optional.empty());
        factory.setMessageConverter(new StringJsonMessageConverter());
        return factory;
    }

    @Bean
    public ConsumerFactory<Long, OutgoingMessage> messageConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }


    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return props;
    }


    private RetryTemplate kafkaRetry() {
        RetryTemplate retryTemplate = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(100 * 10000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        RetryPolicy retryPolicy = new NeverRetryPolicy();
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }


}