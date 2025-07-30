package com.yourcompany.deepseek;

import static org.junit.Assert.*;
import org.junit.Test;

public class DeepSeekAIClientTest {
    
    @Test
    public void testApiResponseParsing() {
        String mockJsonResponse = "{\"choices\":[{\"message\":{\"content\":\"这是测试回复\"}}]}";
        String expected = "这是测试回复";
        
        String actual = DeepSeekAIClient.parseResponse(mockJsonResponse);
        assertEquals(expected, actual);
    }

    @Test
    public void testRequestBuilder() {
        String userInput = "你好";
        String expectedSubstring = "\"content\":\"你好\"";
        
        String requestBody = DeepSeekAIClient.buildRequestBody(userInput);
        assertTrue(requestBody.contains(expectedSubstring));
    }
}