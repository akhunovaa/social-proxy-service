package com.botmasterzzz.social.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${app.email.login}")
    private String login;

    @Value("${app.email.password}")
    private String password;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.yandex.com");
        mailSender.setPort(465);

        mailSender.setUsername(login);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.mime.charset", "utf-8");
        props.setProperty("mail.smtp.allow8bitmime", "true");
        props.setProperty("mail.smtps.allow8bitmime", "true");
        return mailSender;
    }

    @Bean
    public SimpleMailMessage emailTemplate() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("admin@yourapi.ru");
        message.setFrom("noreply@yourapi.ru");
        message.setText("Test message from an email-service");
        return message;
    }
}