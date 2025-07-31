package com.yourcompany.deepseek;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.skyscreamer.jsonassert.JSONAssert;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

public class DeepSeekAIClientTest {
    private static final String MOCK_RESPONSE = "{\"choices\":[{\"message\":{\"content\":\"这是测试回复\"}}]}";

    @After
    public void resetConnectionProvider() {
        DeepSeekAIClient.setConnectionProvider(url -> {
            try {
                return (HttpURLConnection) URI.create(url).toURL().openConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void parseResponse_ValidJson_ReturnsContent() throws IOException {
        assertEquals("这是测试回复", DeepSeekAIClient.parseResponse(MOCK_RESPONSE));
    }

    @Test
    public void parseResponse_InvalidJson_ThrowsException() {
        assertThrows(IOException.class, () -> {
            DeepSeekAIClient.parseResponse("{invalid}");
        });
    }

    @Test
    public void buildRequestBody_NormalInput_ContainsContent() throws Exception {
        String requestBody = DeepSeekAIClient.buildRequestBody("你好");
        JSONAssert.assertEquals(
                "{\"messages\":[{\"role\":\"user\",\"content\":\"你好\"}]}",
                requestBody, false);
    }

    @Test
    public void getAIResponse_MockedServer_ReturnsResponse() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setBody(MOCK_RESPONSE));
            server.start();

            DeepSeekAIClient.setConnectionProvider(url -> {
                try {
                    return (HttpURLConnection) URI.create(server.url("/").toString()).toURL().openConnection();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            assertEquals("这是测试回复", DeepSeekAIClient.getAIResponse("test"));
        }
    }
}