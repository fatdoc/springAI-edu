package org.javaup.ai.cotroller;

import org.javaup.ai.common.ApiResponse;
import org.javaup.ai.entity.UserConfig;
import org.javaup.ai.service.UserConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: 大麦-ai智能服务项目
 * @description: 用户配置控制器
 * @author: 阿星不是程序员
 **/
@RestController
@RequestMapping("/user-config")
public class UserConfigController {
    
    @Autowired
    private UserConfigService userConfigService;
    
    /**
     * 获取用户配置列表
     * @param userId 用户ID
     * @return 配置列表
     */
    @GetMapping("/list")
    public ApiResponse<List<UserConfig>> getConfigList(@RequestParam("userId") String userId) {
        try {
            List<UserConfig> configs = userConfigService.getConfigsByUserId(userId);
            return ApiResponse.ok(configs);
        } catch (Exception e) {
            return ApiResponse.error("获取配置列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存用户配置
     * @param userConfig 用户配置
     * @return 保存结果
     */
    @PostMapping("/save")
    public ApiResponse<Boolean> saveConfig(@RequestBody UserConfig userConfig) {
        try {
            boolean result = userConfigService.saveUserConfig(userConfig);
            if (result) {
                return ApiResponse.ok(true);
            } else {
                return ApiResponse.error("配置保存失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("配置保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户配置
     * @param id 配置ID
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ApiResponse<Boolean> deleteConfig(@RequestParam("id") Long id, 
                                           @RequestParam("userId") String userId) {
        try {
            boolean result = userConfigService.deleteUserConfig(id, userId);
            if (result) {
                return ApiResponse.ok(true);
            } else {
                return ApiResponse.error("配置删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("配置删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试配置连接
     * @param userConfig 用户配置
     * @return 测试结果
     */
    @PostMapping("/test")
    public ApiResponse<Boolean> testConnection(@RequestBody UserConfig userConfig) {
        try {
            boolean result = userConfigService.testConnection(userConfig);
            if (result) {
                return ApiResponse.ok(true);
            } else {
                return ApiResponse.error("连接测试失败");
            }
        } catch (Exception e) {
            return ApiResponse.error("连接测试失败: " + e.getMessage());
        }
    }
} 