package com.yourcompany.deepseek;

import java.io.*;
import java.util.function.Function;
import java.net.URI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeepSeekAIClient {
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekAIClient.class);

    // 添加连接提供器用于测试
    static Function<String, HttpURLConnection> connectionProvider = url -> {
        try {
            return (HttpURLConnection) URI.create(url).toURL().openConnection();
        } catch (IOException e) {
            throw new ConnectionFailedException("Failed to connect to: " + url, e);
        }
    };

    // 允许测试时替换连接
    public static void setConnectionProvider(Function<String, HttpURLConnection> provider) {
        connectionProvider = provider;
    }

    // DeepSeek API 配置
    private static final String API_KEY = "sk-2cc5cc7c0b3347d1ac3cb3aceac2f066"; // api是在deepseek上新注册的，已充值10元，仅做测试
    private static final String API_ENDPOINT = "https://api.deepseek.com/v1/chat/completions";

    public static void main(String[] args) {
        String userQuery = "你好，我想咨询一下你们的产品";
        String aiResponse = getAIResponse(userQuery);

        // 直接使用System.out测试
        System.out.println("直接输出测试: " + aiResponse);

        // 使用Logger测试
        logger.info("Logger输出测试: {}", aiResponse);
    }

    /**
     * 获取AI客服的回复
     * 
     * @param userMessage 用户输入的消息
     * @return AI生成的回复
     */
    public static String getAIResponse(String userMessage) {
        try {
            // 打开连接
            HttpURLConnection connection = connectionProvider.apply(API_ENDPOINT);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
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
                    String aiResponse = parseResponse(response.toString());

                    if (logger.isInfoEnabled()) {
                        logger.info("AI客服回复: {}", aiResponse);
                    }
                    return aiResponse; // 返回AI回复
                }
            } else {
                logger.error("API请求失败,响应码: {}", responseCode);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    logger.error("错误响应: {}", errorResponse);
                }
                return "抱歉,AI客服暂时无法提供服务,请稍后再试。";
            }

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("处理请求时出现错误", e);
            }
            return "处理您的请求时出现错误: " + e.getMessage();
        }
    }

    /**
     * 构建API请求体
     * 
     * @param message 用户消息
     * @return JSON格式的请求体
     */
    static String buildRequestBody(String message) {
        // 这里可以根据DeepSeek API的实际要求调整请求体结构
        // 以下是一个通用的ChatCompletion请求示例
        return String.format("""
                {
                  "model": "deepseek-chat",
                  "messages": [
                    {
                      "role": "system",
                      "content": "你是一个专业的客服助手，负责回答客户关于公司产品和服务的咨询。请保持友好、专业的语气，提供准确的信息。"
                    },
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ],
                  "temperature": 0.7,
                  "max_tokens": 1000
                }
                """, message);
    }

    /**
     * 解析API响应
     * 
     * @param jsonResponse API返回的JSON响应
     * @return 提取的AI回复内容
     */
    static String parseResponse(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonResponse);
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}