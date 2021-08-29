package com.botmasterzzz.social.controller;

import com.botmasterzzz.social.dto.MailDTO;
import com.botmasterzzz.social.model.Response;
import com.botmasterzzz.social.service.TelegramInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@SuppressWarnings("deprecation")
public class TelegramController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramController.class);

    private final TelegramInstanceService telegramInstanceService;

    @Autowired
    public TelegramController(TelegramInstanceService telegramInstanceService) {
        this.telegramInstanceService = telegramInstanceService;
    }

    @RequestMapping(value = "/start", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Response botStart() {
        LOGGER.info("Incoming request to bo start...");
        telegramInstanceService.botStart();
        return getResponseDto(new MailDTO());
    }

}
