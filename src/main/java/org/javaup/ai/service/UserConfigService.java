package org.javaup.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.javaup.ai.entity.UserConfig;

import java.util.List;

/**
 * @program: 大麦-ai智能服务项目
 * @description: 用户配置Service接口
 * @author: 阿星不是程序员
 **/
public interface UserConfigService extends IService<UserConfig> {
    
    /**
     * 根据用户ID获取配置列表
     * @param userId 用户ID
     * @return 配置列表
     */
    List<UserConfig> getConfigsByUserId(String userId);
    
    /**
     * 保存用户配置
     * @param userConfig 用户配置
     * @return 是否保存成功
     */
    boolean saveUserConfig(UserConfig userConfig);
    
    /**
     * 删除用户配置
     * @param id 配置ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteUserConfig(Long id, String userId);
    
    /**
     * 测试配置连接
     * @param userConfig 用户配置
     * @return 测试结果
     */
    boolean testConnection(UserConfig userConfig);
} 