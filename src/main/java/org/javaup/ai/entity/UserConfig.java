package org.javaup.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import org.javaup.ai.entity.base.BaseTableData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @program: 大麦-ai智能服务项目
 * @description: 用户配置实体
 * @author: 阿星不是程序员
 **/
@Data
@EqualsAndHashCode(callSuper = true)  // 继承基类的equals和hashCode
@TableName("d_user_config")
public class UserConfig extends BaseTableData {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色描述
     */
    @TableField("description")
    private String description;

    /**
     * API密钥
     */
    @TableField("api_key")
    private String apiKey;

    /**
     * API基础URL
     */
    @TableField("base_url")
    private String baseUrl;

    /**
     * 模型名称
     */
    @TableField("model")
    private String model;

    /**
     * 温度参数
     */
    @TableField("temperature")
    private Double temperature;

    /**
     * 最大token数
     */
    @TableField("max_tokens")
    private Integer maxTokens;

    /**
     * 系统提示词
     */
    @TableField("system_prompt")
    private String systemPrompt;

    /**
     * 是否启用：false-否，true-是
     */
    @TableField("enabled")
    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    // 注意：不要重新定义 status 字段，因为 BaseTableData 中已经有了
    // status 字段会从基类继承，类型为 Boolean
}