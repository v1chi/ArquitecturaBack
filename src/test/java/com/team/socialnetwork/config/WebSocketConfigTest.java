package com.team.socialnetwork.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class WebSocketConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testWebSocketConfigBeanExists() {
        WebSocketConfig webSocketConfig = applicationContext.getBean(WebSocketConfig.class);
        assertNotNull(webSocketConfig);
    }

    @Test
    void testAuthChannelInterceptorBeanExists() {
        AuthChannelInterceptor authChannelInterceptor = applicationContext.getBean(AuthChannelInterceptor.class);
        assertNotNull(authChannelInterceptor);
    }
}