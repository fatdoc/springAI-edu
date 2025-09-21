package org.javaup.ai;

import org.javaup.ai.entity.UserConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @program: 大麦-ai智能服务项目
 * @description: 用户配置测试类
 * @author: 阿星不是程序员
 **/
@SpringBootTest
public class UserConfigTest {
    
    @Test
    public void testUserConfigCreation() {
        UserConfig config = new UserConfig();
        config.setUserId("test-user");
        config.setRoleName("测试角色");
        config.setDescription("这是一个测试角色");
        config.setApiKey("test-api-key");
        config.setBaseUrl("https://api.openai.com/v1");
        config.setModel("gpt-3.5-turbo");
        config.setTemperature(0.7);
        config.setMaxTokens(2000);
        config.setSystemPrompt("你是一个有用的AI助手");
        config.setEnabled(true);
        
        assertNotNull(config);
        assertEquals("test-user", config.getUserId());
        assertEquals("测试角色", config.getRoleName());
        assertEquals("gpt-3.5-turbo", config.getModel());
        assertEquals(0.7, config.getTemperature());
        assertEquals(2000, config.getMaxTokens());
        assertTrue(config.getEnabled());
    }
} 