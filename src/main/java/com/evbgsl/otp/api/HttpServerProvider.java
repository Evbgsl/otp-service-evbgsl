package com.evbgsl.otp.api;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerProvider {

    private HttpServerProvider() {
    }

    public static HttpServer create(int port) throws IOException {
        return HttpServer.create(new InetSocketAddress(port), 0);
    }
}