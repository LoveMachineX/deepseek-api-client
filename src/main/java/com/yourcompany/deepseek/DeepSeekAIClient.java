package com.yourcompany.deepseek;

import java.io.*;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DeepSeekAIClient {

    // 添加连接提供器用于测试
    static Function<String, HttpURLConnection> connectionProvider = url -> {
        try {
            return (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    // 允许测试时替换连接
    public static void setConnectionProvider(Function<String, HttpURLConnection> provider) {
        connectionProvider = provider;
    }

    // DeepSeek API 配置
    private static final String API_KEY = "sk-2cc5cc7c0b3347d1ac3cb3aceac2f066"; // api是在deepseek上新注册的，目前没有花费，仅做测试
    private static final String API_ENDPOINT = "https://api.deepseek.com/v1/chat/completions";

    // 建议的API调用额度
    // private static final String API_URL = "YOUR_CURRENT_API_ENDPOINT";
    // private static final int MAX_FREE_CALLS = 100; // 根据实际调整
    // public String getResponse(String query) throws OverQuotaException {
    // if(usedCount >= MAX_FREE_CALLS) {
    // throw new OverQuotaException("已达到试用限额");
    // }
    // }

    public static void main(String[] args) {
        String userQuery = "你好，我想咨询一下你们的产品";
        String aiResponse = getAIResponse(userQuery);
        System.out.println("AI客服回复: " + aiResponse);
    }

    /**
     * 获取AI客服的回复
     * 
     * @param userMessage 用户输入的消息
     * @return AI生成的回复
     */
    public static String getAIResponse(String userMessage) {
        try {
            // 创建请求URL
            URL url = new URL(API_ENDPOINT);

            // 打开连接
            HttpURLConnection connection = connectionProvider.apply(API_ENDPOINT);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setDoOutput(true);

            // 构建请求体
            String requestBody = buildRequestBody(userMessage);

            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            } finally {
                connection.disconnect();
            }

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return parseResponse(response.toString());
                }
            } else {
                System.err.println("API请求失败,响应码: " + responseCode);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    System.err.println("错误响应: " + errorResponse.toString());
                }
                return "抱歉,AI客服暂时无法提供服务,请稍后再试。";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "处理您的请求时出现错误: " + e.getMessage();
        }
    }

    /**
     * 构建API请求体
     * 
     * @param message 用户消息
     * @return JSON格式的请求体
     */
    private static String buildRequestBody(String message) {
        // 这里可以根据DeepSeek API的实际要求调整请求体结构
        // 以下是一个通用的ChatCompletion请求示例
        return String.format("{\n" +
                "  \"model\": \"deepseek-chat\",\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"system\",\n" +
                "      \"content\": \"你是一个专业的客服助手，负责回答客户关于公司产品和服务的咨询。请保持友好、专业的语气，提供准确的信息。\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \"%s\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"temperature\": 0.7,\n" +
                "  \"max_tokens\": 1000\n" +
                "}", message);
    }

    /**
     * 解析API响应
     * 
     * @param jsonResponse API返回的JSON响应
     * @return 提取的AI回复内容
     */
    private static String parseResponse(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonResponse);
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}