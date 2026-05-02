package com.evbgsl.otp.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JsonUtil {

    private static final Gson GSON = new GsonBuilder().create();

    private JsonUtil() {
    }

    public static <T> T fromJson(InputStream inputStream, Class<T> clazz) {
        return GSON.fromJson(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8),
                clazz
        );
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }
}