package com.example.behavior;

import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface HttpTransport {
    String postJson(String url, Map<String, String> headers, String body) throws IOException;
}
