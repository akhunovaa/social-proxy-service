package com.botmasterzzz.social.service;

public interface EmailService {

    void sendMail(String recipient, String subject, String body);

    /**
     * This method will send a pre-configured message
     */
    void sendPreConfiguredMail(String message);

    void sendMailWithAttachment(String to, String subject, String body, String fileToAttach);

    void sendMailWithInlineResources(String to, String subject, String fileToAttach);

}
