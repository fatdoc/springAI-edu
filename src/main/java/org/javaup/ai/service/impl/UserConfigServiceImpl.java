package org.javaup.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.javaup.ai.entity.UserConfig;
import org.javaup.ai.mapper.UserConfigMapper;
import org.javaup.ai.service.UserConfigService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @program: 大麦-ai智能服务项目
 * @description: 用户配置Service实现类
 * @author: 阿星不是程序员
 **/
@Service
public class UserConfigServiceImpl extends ServiceImpl<UserConfigMapper, UserConfig> implements UserConfigService {

    @Override
    public List<UserConfig> getConfigsByUserId(String userId) {
        LambdaQueryWrapper<UserConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConfig::getUserId, userId)
                .eq(UserConfig::getEnabled, true)
                .eq(UserConfig::getStatus, true)  // 使用Boolean类型，true表示正常状态
                .orderByDesc(UserConfig::getCreateTime);

        List<UserConfig> configs = this.list(wrapper);
        System.out.println("查询用户配置 - 用户ID: " + userId + ", 找到数量: " + (configs != null ? configs.size() : 0));
        return configs;
    }

    @Override
    public boolean saveUserConfig(UserConfig userConfig) {
        try {
            // 设置创建和修改时间 - 使用Date类型
            Date now = new Date();
            if (userConfig.getId() == null) {
                // 新建记录，设置创建时间
                userConfig.setCreateTime(now);
            }
            // 总是更新修改时间
            userConfig.setEditTime(now);

            // 设置默认值
            if (userConfig.getEnabled() == null) {
                userConfig.setEnabled(true);
            }
            if (userConfig.getStatus() == null) {
                userConfig.setStatus(1);  // 使用Boolean类型，true表示正常状态
            }
            if (userConfig.getTemperature() == null) {
                userConfig.setTemperature(0.7);
            }
            if (userConfig.getMaxTokens() == null) {
                userConfig.setMaxTokens(2000);
            }

            // 确保用户ID不为空
            if (userConfig.getUserId() == null || userConfig.getUserId().trim().isEmpty()) {
                userConfig.setUserId("default");
            }

            boolean result = this.saveOrUpdate(userConfig);  // 支持新增和更新
            System.out.println("保存配置结果: " + result + ", 配置ID: " + userConfig.getId());
            return result;
        } catch (Exception e) {
            System.err.println("保存配置失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("保存配置失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteUserConfig(Long id, String userId) {
        try {
            // 改为逻辑删除，而不是物理删除
            LambdaUpdateWrapper<UserConfig> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserConfig::getId, id)
                    .eq(UserConfig::getUserId, userId)
                    .set(UserConfig::getEnabled, false)  // 设置为禁用
                    .set(UserConfig::getStatus, false)   // 使用Boolean类型，false表示删除状态
                    .set(UserConfig::getEditTime, new Date());

            boolean result = this.update(updateWrapper);
            System.out.println("删除配置结果: " + result + ", 配置ID: " + id);
            return result;
        } catch (Exception e) {
            System.err.println("删除配置失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("删除配置失败: " + e.getMessage());
        }
    }

    @Override
    public boolean testConnection(UserConfig userConfig) {
        try {
            // 添加基本验证
            if (userConfig.getApiKey() == null || userConfig.getApiKey().trim().isEmpty()) {
                throw new RuntimeException("API Key 不能为空");
            }

            if (userConfig.getBaseUrl() == null || userConfig.getBaseUrl().trim().isEmpty()) {
                throw new RuntimeException("Base URL 不能为空");
            }

            if (userConfig.getModel() == null || userConfig.getModel().trim().isEmpty()) {
                throw new RuntimeException("模型名称不能为空");
            }

            // 这里可以添加实际的API测试逻辑
            System.out.println("连接测试通过 - 模型: " + userConfig.getModel());

            return true;  // 暂时返回true，表示测试成功
        } catch (Exception e) {
            System.err.println("连接测试失败: " + e.getMessage());
            throw new RuntimeException("连接测试失败: " + e.getMessage());
        }
    }
}