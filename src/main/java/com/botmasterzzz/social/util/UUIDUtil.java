package com.botmasterzzz.social.util;

import com.fasterxml.uuid.Generators;

import java.util.UUID;

public class UUIDUtil {

    public static String generateTimeBasedUUID() {
        UUID uuid = Generators.timeBasedGenerator().generate();
        return uuid.toString();
    }

    public static String generateRandomUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

}
