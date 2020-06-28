package com.botmasterzzz.social.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailDTO extends AbstractDto {

    private String recipient;
    private String from;
    private String subject;
    private String message;

    public MailDTO() {
    }

    public MailDTO(String message) {
        this.message = message;
    }

    public class Builder {

        private Builder() {

        }

        public MailDTO.Builder setId(Long id) {
            MailDTO.super.id = id;
            return this;
        }

        public MailDTO.Builder setRecepient(String recepient) {
            MailDTO.this.recipient = recepient;
            return this;
        }

        public MailDTO.Builder setFrom(String from) {
            MailDTO.this.from = from;
            return this;
        }

        public MailDTO.Builder setSubject(String subject) {
            MailDTO.this.subject = subject;
            return this;
        }

        public MailDTO.Builder setMessage(String message) {
            MailDTO.this.message = message;
            return this;
        }

        public MailDTO build() {
            return MailDTO.this;
        }

    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MailDTO{" +
                "recipient='" + recipient + '\'' +
                ", from='" + from + '\'' +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
