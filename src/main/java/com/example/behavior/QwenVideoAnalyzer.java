package com.example.behavior;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QwenVideoAnalyzer {

    private static final String DEFAULT_ENDPOINT = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String DEFAULT_MODEL = "qwen3-vl-plus";

    private final HttpTransport transport;
    private final String endpoint;
    private final String model;
    private final String apiKey;

    public QwenVideoAnalyzer(HttpTransport transport, String apiKey) {
        this(transport, DEFAULT_ENDPOINT, DEFAULT_MODEL, apiKey);
    }

    public QwenVideoAnalyzer(HttpTransport transport, String endpoint, String model, String apiKey) {
        this.transport = transport;
        this.endpoint = endpoint;
        this.model = model;
        this.apiKey = apiKey;
    }

    public AiVideoAnalysisResult analyzeVideoUrl(String videoUrl) throws IOException {
        if (videoUrl == null || videoUrl.isBlank()) {
            throw new IllegalArgumentException("videoUrl is required");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("DashScope API key is required");
        }

        String payload = buildPayload(videoUrl);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);

        String response = transport.postJson(endpoint, headers, payload);
        String assistantContent = extractAssistantContent(response);
        return parseResult(assistantContent);
    }

    String buildPayload(String videoUrl) {
        String escapedVideoUrl = escapeJson(videoUrl);
        String systemPrompt = escapeJson("你是幼儿行为分析助手。请根据视频内容给出行为标签和结构化报告。严格只返回 JSON。JSON格式为：{\"tags\":[\"标签1\",\"标签2\"],\"report\":\"报告内容\"}");
        String userPrompt = escapeJson("请分析该短视频，输出行为标签与报告。仅返回 JSON，不要输出额外解释。");

        return "{" +
                "\"model\":\"" + escapeJson(model) + "\"," +
                "\"messages\":[" +
                "{\"role\":\"system\",\"content\":[{\"type\":\"text\",\"text\":\"" + systemPrompt + "\"}]}" +
                ",{\"role\":\"user\",\"content\":[" +
                "{\"type\":\"video_url\",\"video_url\":{\"url\":\"" + escapedVideoUrl + "\"}}," +
                "{\"type\":\"text\",\"text\":\"" + userPrompt + "\"}" +
                "]}" +
                "]," +
                "\"temperature\":0.2" +
                "}";
    }

    AiVideoAnalysisResult parseResult(String assistantContent) {
        List<String> tags = parseTags(assistantContent);
        String report = parseReport(assistantContent);
        if (tags.isEmpty()) {
            tags = List.of("未识别到标签");
        }
        if (report == null || report.isBlank()) {
            report = "模型返回中未找到 report 字段，请查看 rawModelContent。";
        }
        return new AiVideoAnalysisResult(tags, report, assistantContent);
    }

    String extractAssistantContent(String responseJson) {
        Pattern p = Pattern.compile("\\\"content\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"");
        Matcher m = p.matcher(responseJson);
        String last = null;
        while (m.find()) {
            last = m.group(1);
        }
        if (last == null) {
            return responseJson;
        }
        return unescapeJson(last);
    }

    private List<String> parseTags(String text) {
        Pattern p = Pattern.compile("\\\"tags\\\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Matcher m = p.matcher(text);
        if (!m.find()) {
            return new ArrayList<>();
        }
        String body = m.group(1);
        Pattern item = Pattern.compile("\\\"(.*?)\\\"");
        Matcher itemMatcher = item.matcher(body);
        List<String> tags = new ArrayList<>();
        while (itemMatcher.find()) {
            tags.add(unescapeJson(itemMatcher.group(1)));
        }
        return tags;
    }

    private String parseReport(String text) {
        Pattern p = Pattern.compile("\\\"report\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"");
        Matcher m = p.matcher(text);
        if (!m.find()) {
            return null;
        }
        return unescapeJson(m.group(1));
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private String unescapeJson(String text) {
        return text
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
