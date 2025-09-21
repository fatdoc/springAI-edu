<template>
  <div class="role-config-page">
    <div class="container">
      <h1 class="title">角色配置</h1>
      <p class="subtitle">配置AI角色和系统提示词</p>
      
      <div class="config-form">
        <div class="form-section">
          <h2>新增角色配置</h2>
          <div class="form-group">
            <label for="roleName">角色名称</label>
            <input 
              id="roleName"
              v-model="config.roleName" 
              type="text" 
              placeholder="例如：编程助手、客服助手、写作助手"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label for="roleDescription">角色描述</label>
            <textarea 
              id="roleDescription"
              v-model="config.roleDescription" 
              placeholder="简要描述这个角色的功能和特点..."
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>

          <div class="form-group">
            <label for="llmConfigId">选择大模型配置</label>
            <select v-model="config.llmConfigId" class="form-select" @change="onLLMConfigChange">
              <option value="">请选择大模型配置</option>
              <option v-for="llmConfig in llmConfigs" :key="llmConfig.id" :value="llmConfig.id">
                {{ llmConfig.configName }} ({{ getLLMTypeLabel(llmConfig.llmType) }} - {{ llmConfig.modelName }})
              </option>
            </select>
            <div v-if="!llmConfigs.length" class="no-llm-configs">
              <p>还没有配置大模型，请先到 <router-link to="/llm-config">大模型配置</router-link> 页面添加模型配置</p>
            </div>
          </div>

          <div class="form-group">
            <label for="systemPrompt">系统提示词</label>
            <textarea 
              id="systemPrompt"
              v-model="config.systemPrompt" 
              placeholder="请输入角色的系统提示词，定义角色的行为和风格..."
              class="form-textarea"
              rows="8"
            ></textarea>
            <div class="prompt-templates">
              <h4>常用模板：</h4>
              <div class="template-buttons">
                <button 
                  v-for="template in promptTemplates" 
                  :key="template.name"
                  @click="applyTemplate(template)"
                  class="template-btn"
                >
                  {{ template.name }}
                </button>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input 
                v-model="config.isDefault" 
                type="checkbox" 
                class="form-checkbox"
              />
              <span>设为默认角色</span>
            </label>
          </div>

          <!-- 高级参数配置 -->
          <div class="advanced-section">
            <h3>高级参数配置 (可选)</h3>
            <div class="advanced-toggle">
              <label class="checkbox-label">
                <input 
                  v-model="showAdvanced" 
                  type="checkbox" 
                  class="form-checkbox"
                />
                <span>自定义温度参数和Token限制</span>
              </label>
            </div>

            <div v-if="showAdvanced" class="advanced-params">
              <div class="form-group">
                <label for="temperature">温度 (Temperature)</label>
                <input 
                  id="temperature"
                  v-model.number="config.temperature" 
                  type="range" 
                  min="0" 
                  max="2" 
                  step="0.1"
                  class="form-range"
                />
                <span class="range-value">{{ config.temperature }}</span>
                <small class="help-text">当前LLM默认值: {{ selectedLLMConfig?.temperature || 'N/A' }}</small>
              </div>
              
              <div class="form-group">
                <label for="maxTokens">最大 Token 数</label>
                <input 
                  id="maxTokens"
                  v-model.number="config.maxTokens" 
                  type="number" 
                  min="1" 
                  max="4000"
                  class="form-input"
                />
                <small class="help-text">当前LLM默认值: {{ selectedLLMConfig?.maxTokens || 'N/A' }}</small>
              </div>
            </div>
          </div>
        </div>
        
        <div class="form-actions">
          <button @click="saveConfig" class="btn btn-primary" :disabled="saving">
            {{ saving ? '保存中...' : '保存配置' }}
          </button>
          <button @click="testRole" class="btn btn-secondary" :disabled="testing">
            {{ testing ? '测试中...' : '测试角色' }}
          </button>
          <button @click="resetForm" class="btn btn-outline">重置</button>
        </div>
      </div>
      
      <div v-if="savedConfigs.length > 0" class="saved-configs">
        <h2>已保存的角色配置</h2>
        <div class="config-list">
          <div 
            v-for="roleConfig in savedConfigs" 
            :key="roleConfig.id"
            class="config-item"
            :class="{ 'default-config': roleConfig.isDefault }"
          >
            <div class="config-info">
              <div class="config-header">
                <h3>{{ roleConfig.roleName }}</h3>
                <span v-if="roleConfig.isDefault" class="default-badge">默认</span>
              </div>
              <p class="role-description">{{ roleConfig.roleDescription }}</p>
              <div class="config-meta">
                <span class="llm-info">
                  {{ getLLMConfigName(roleConfig.llmConfigId) }}
                </span>
                <span v-if="roleConfig.temperature !== null" class="temperature">
                  温度: {{ roleConfig.temperature }}
                </span>
                <span v-if="roleConfig.maxTokens !== null" class="max-tokens">
                  Max Tokens: {{ roleConfig.maxTokens }}
                </span>
              </div>
              <div class="config-details">
                <span class="create-time">{{ formatTime(roleConfig.createTime) }}</span>
              </div>
            </div>
            <div class="config-actions">
              <button @click="loadConfig(roleConfig)" class="btn btn-small">编辑</button>
              <button @click="setAsDefault(roleConfig.id)" class="btn btn-small" :disabled="roleConfig.isDefault">
                {{ roleConfig.isDefault ? '已默认' : '设默认' }}
              </button>
              <button @click="deleteConfig(roleConfig.id)" class="btn btn-small btn-danger">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useDark } from '@vueuse/core'
// import { roleConfigAPI, llmConfigAPI } from '../api/api.js'

const isDark = useDark()
const saving = ref(false)
const testing = ref(false)
const showAdvanced = ref(false)

const config = reactive({
  userId: 'default',
  roleName: '',
  roleDescription: '',
  llmConfigId: '',
  systemPrompt: '',
  temperature: null,
  maxTokens: null,
  isDefault: false
})

const savedConfigs = ref([])
const llmConfigs = ref([])

// 提示词模板
const promptTemplates = [
  {
    name: '编程助手',
    prompt: `你是一个专业的编程助手，具有以下特点：

1. 精通多种编程语言，包括但不限于 Python、JavaScript、Java、Go、C++ 等
2. 能够提供清晰的代码示例和解释
3. 遵循最佳实践和编程规范
4. 能够帮助调试代码和解决技术问题
5. 提供简洁、准确的回答

请用中文回答用户的问题，提供实用的编程建议和代码示例。`
  },
  {
    name: '客服助手',
    prompt: `你是一个友善、专业的客服助手，具有以下特点：

1. 耐心、礼貌地回答用户问题
2. 提供准确的产品和服务信息
3. 能够处理投诉和建议
4. 主动提供解决方案
5. 保持积极的服务态度

请用温和、专业的语气回答用户的问题，确保用户得到满意的帮助。`
  },
  {
    name: '写作助手',
    prompt: `你是一个专业的写作助手，具有以下特点：

1. 精通各种文体写作，包括文章、邮件、报告等
2. 能够提供写作建议和技巧
3. 帮助润色和修改文本
4. 根据不同场景调整写作风格
5. 确保内容逻辑清晰、语言流畅

请根据用户需求提供专业的写作帮助和建议。`
  },
  {
    name: '学习导师',
    prompt: `你是一个耐心的学习导师，具有以下特点：

1. 能够根据学生水平调整教学方式
2. 提供清晰的概念解释和实例
3. 鼓励学生思考和实践
4. 能够解答各种学科问题
5. 培养学生的学习兴趣

请用启发式的方式引导学生学习，提供适合的学习建议和方法。`
  }
]

// 获取LLM类型标签
const getLLMTypeLabel = (type) => {
  const labels = {
    chatgpt: 'ChatGPT',
    deepseek: 'DeepSeek',
    doubao: '豆包',
    qwen: '通义千问',
    ollama: 'Ollama'
  }
  return labels[type] || type
}

// 获取LLM配置名称
const getLLMConfigName = (llmConfigId) => {
  const llmConfig = llmConfigs.value.find(config => config.id === llmConfigId)
  return llmConfig ? `${llmConfig.configName} (${getLLMTypeLabel(llmConfig.llmType)})` : '未知配置'
}

// 当前选中的LLM配置
const selectedLLMConfig = computed(() => {
  return llmConfigs.value.find(config => config.id === config.llmConfigId)
})

// LLM配置改变时的处理
const onLLMConfigChange = () => {
  if (selectedLLMConfig.value) {
    config.temperature = selectedLLMConfig.value.temperature
    config.maxTokens = selectedLLMConfig.value.maxTokens
  }
}

// 应用模板
const applyTemplate = (template) => {
  config.systemPrompt = template.prompt
}

// 保存配置
const saveConfig = async () => {
  if (!config.roleName || !config.llmConfigId || !config.systemPrompt) {
    alert('请填写完整的配置信息')
    return
  }
  
  saving.value = true
  try {
    // await roleConfigAPI.saveConfig(config)
    console.log('保存角色配置:', config)
    alert('角色配置保存成功！')
    
    // 重新加载配置列表
    await loadConfigs()
    
    // 清空表单
    resetForm()
  } catch (error) {
    console.error('保存角色配置失败:', error)
    alert('保存角色配置失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 测试角色
const testRole = async () => {
  if (!config.roleName || !config.systemPrompt) {
    alert('请填写角色名称和系统提示词')
    return
  }
  
  testing.value = true
  try {
    // await roleConfigAPI.testRole(config)
    console.log('测试角色:', config)
    alert('角色测试成功！')
  } catch (error) {
    console.error('角色测试失败:', error)
    alert('角色测试失败: ' + error.message)
  } finally {
    testing.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(config, {
    roleName: '',
    roleDescription: '',
    llmConfigId: '',
    systemPrompt: '',
    temperature: null,
    maxTokens: null,
    isDefault: false
  })
  showAdvanced.value = false
}

// 加载配置
const loadConfig = (savedConfig) => {
  Object.assign(config, savedConfig)
  if (savedConfig.temperature !== null || savedConfig.maxTokens !== null) {
    showAdvanced.value = true
  }
}

// 设为默认配置
const setAsDefault = async (configId) => {
  try {
    // await roleConfigAPI.setAsDefault(configId, config.userId)
    console.log('设为默认角色:', configId)
    await loadConfigs()
    alert('设置默认角色成功！')
  } catch (error) {
    console.error('设置默认角色失败:', error)
    alert('设置默认角色失败: ' + error.message)
  }
}

// 删除配置
const deleteConfig = async (configId) => {
  if (confirm('确定要删除这个角色配置吗？')) {
    try {
      // await roleConfigAPI.deleteConfig(configId, config.userId)
      console.log('删除角色配置:', configId)
      await loadConfigs()
      alert('删除角色配置成功！')
    } catch (error) {
      console.error('删除角色配置失败:', error)
      alert('删除角色配置失败: ' + error.message)
    }
  }
}

// 加载已保存的配置
const loadConfigs = async () => {
  try {
    // const configs = await roleConfigAPI.getConfigList(config.userId)
    // savedConfigs.value = configs
    console.log('加载角色配置列表')
  } catch (error) {
    console.error('加载角色配置失败:', error)
    savedConfigs.value = []
  }
}

// 加载LLM配置列表
const loadLLMConfigs = async () => {
  try {
    // const configs = await llmConfigAPI.getConfigList(config.userId)
    // llmConfigs.value = configs
    console.log('加载LLM配置列表')
  } catch (error) {
    console.error('加载LLM配置失败:', error)
    llmConfigs.value = []
  }
}

// 格式化时间
const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

// 初始化时加载配置
onMounted(() => {
  loadConfigs()
  loadLLMConfigs()
})
</script>

<style scoped lang="scss">
.role-config-page {
  min-height: 100vh;
  padding: 2rem;
  background: var(--bg-color);
  transition: background-color 0.3s;

  .container {
    max-width: 900px;
    margin: 0 auto;
  }

  .title {
    text-align: center;
    font-size: 2.5rem;
    margin-bottom: 0.5rem;
    background: linear-gradient(45deg, rgba(255, 55, 29, 0.85), #ff8f29);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    font-weight: 600;
  }

  .subtitle {
    text-align: center;
    color: #666;
    margin-bottom: 3rem;
    font-size: 1.1rem;
  }

  .config-form {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 24px;
    padding: 2rem;
    margin-bottom: 2rem;
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);

    .dark & {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
  }

  .form-section {
    margin-bottom: 2rem;

    h2 {
      font-size: 1.5rem;
      margin-bottom: 1.5rem;
      color: var(--text-color);
      border-bottom: 2px solid var(--primary-color);
      padding-bottom: 0.5rem;
    }
  }

  .form-group {
    margin-bottom: 1.5rem;

    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      color: var(--text-color);
    }

    .form-input,
    .form-select,
    .form-textarea {
      width: 100%;
      padding: 0.75rem 1rem;
      border: 2px solid #e1e5e9;
      border-radius: 12px;
      font-size: 1rem;
      transition: all 0.3s ease;
      background: var(--bg-color);
      color: var(--text-color);

      &:focus {
        outline: none;
        border-color: var(--primary-color);
        box-shadow: 0 0 0 3px rgba(255, 59, 29, 0.1);
      }

      .dark & {
        border-color: #444;
        background: #2a2a2a;
      }
    }

    .form-textarea {
      resize: vertical;
      min-height: 100px;
    }

    .form-range {
      width: 100%;
      height: 6px;
      border-radius: 3px;
      background: #e1e5e9;
      outline: none;
      -webkit-appearance: none;

      &::-webkit-slider-thumb {
        -webkit-appearance: none;
        appearance: none;
        width: 20px;
        height: 20px;
        border-radius: 50%;
        background: var(--primary-color);
        cursor: pointer;
      }

      &::-moz-range-thumb {
        width: 20px;
        height: 20px;
        border-radius: 50%;
        background: var(--primary-color);
        cursor: pointer;
        border: none;
      }
    }

    .range-value {
      display: inline-block;
      margin-left: 1rem;
      font-weight: 500;
      color: var(--primary-color);
    }

    .help-text {
      display: block;
      margin-top: 0.5rem;
      font-size: 0.8rem;
      color: #666;

      .dark & {
        color: #999;
      }
    }

    .checkbox-label {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;

      .form-checkbox {
        width: 18px;
        height: 18px;
        accent-color: var(--primary-color);
      }
    }

    .no-llm-configs {
      margin-top: 0.5rem;
      padding: 1rem;
      background: #fff3cd;
      border: 1px solid #ffeaa7;
      border-radius: 8px;
      color: #856404;

      .dark & {
        background: rgba(255, 243, 205, 0.1);
        border-color: rgba(255, 234, 167, 0.3);
        color: #ffc107;
      }

      a {
        color: var(--primary-color);
        text-decoration: none;

        &:hover {
          text-decoration: underline;
        }
      }
    }
  }

  .prompt-templates {
    margin-top: 1rem;
    padding: 1rem;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 12px;
    border: 1px solid #e1e5e9;

    .dark & {
      background: rgba(255, 255, 255, 0.05);
      border-color: #444;
    }

    h4 {
      margin: 0 0 1rem 0;
      color: var(--text-color);
      font-size: 1rem;
    }

    .template-buttons {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;

      .template-btn {
        padding: 0.5rem 1rem;
        border: 1px solid var(--primary-color);
        border-radius: 20px;
        background: transparent;
        color: var(--primary-color);
        font-size: 0.9rem;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          background: var(--primary-color);
          color: white;
        }
      }
    }
  }

  .advanced-section {
    margin-top: 2rem;
    padding: 1.5rem;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 12px;
    border: 1px solid #e1e5e9;

    .dark & {
      background: rgba(255, 255, 255, 0.05);
      border-color: #444;
    }

    h3 {
      margin: 0 0 1rem 0;
      color: var(--text-color);
      font-size: 1.2rem;
    }

    .advanced-toggle {
      margin-bottom: 1rem;
    }

    .advanced-params {
      margin-top: 1rem;
    }
  }

  .form-actions {
    display: flex;
    gap: 1rem;
    justify-content: center;
    margin-top: 2rem;

    .btn {
      padding: 0.75rem 2rem;
      border: none;
      border-radius: 12px;
      font-size: 1rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.3s ease;
      min-width: 120px;

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      &.btn-primary {
        background: var(--primary-color);
        color: white;

        &:hover:not(:disabled) {
          background: #e02e1c;
          transform: translateY(-2px);
        }
      }

      &.btn-secondary {
        background: #f8f9fa;
        color: var(--text-color);
        border: 2px solid #e1e5e9;

        &:hover:not(:disabled) {
          background: #e9ecef;
          transform: translateY(-2px);
        }

        .dark & {
          background: #2a2a2a;
          border-color: #444;
          color: #fff;

          &:hover:not(:disabled) {
            background: #3a3a3a;
          }
        }
      }

      &.btn-outline {
        background: transparent;
        color: var(--primary-color);
        border: 2px solid var(--primary-color);

        &:hover:not(:disabled) {
          background: var(--primary-color);
          color: white;
          transform: translateY(-2px);
        }
      }
    }
  }

  .saved-configs {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 24px;
    padding: 2rem;
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);

    .dark & {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.1);
    }

    h2 {
      font-size: 1.5rem;
      margin-bottom: 1.5rem;
      color: var(--text-color);
      border-bottom: 2px solid var(--primary-color);
      padding-bottom: 0.5rem;
    }

    .config-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .config-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 1.5rem;
      border: 1px solid #e1e5e9;
      border-radius: 12px;
      background: var(--bg-color);
      transition: all 0.3s ease;

      &.default-config {
        border-color: var(--primary-color);
        background: rgba(255, 59, 29, 0.05);
      }

      &:hover {
        border-color: var(--primary-color);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .dark & {
        border-color: #444;
        background: #2a2a2a;

        &:hover {
          border-color: var(--primary-color);
        }

        &.default-config {
          background: rgba(255, 59, 29, 0.1);
        }
      }

      .config-info {
        flex: 1;
        margin-right: 1rem;

        .config-header {
          display: flex;
          align-items: center;
          gap: 1rem;
          margin-bottom: 0.5rem;

          h3 {
            margin: 0;
            color: var(--text-color);
            font-size: 1.2rem;
          }

          .default-badge {
            background: var(--primary-color);
            color: white;
            padding: 0.25rem 0.75rem;
            border-radius: 12px;
            font-size: 0.8rem;
            font-weight: 500;
          }
        }

        .role-description {
          margin: 0 0 0.5rem 0;
          color: #666;
          font-size: 0.9rem;
          line-height: 1.4;

          .dark & {
            color: #999;
          }
        }

        .config-meta {
          display: flex;
          gap: 1rem;
          margin-bottom: 0.5rem;
          font-size: 0.9rem;

          .llm-info {
            color: var(--primary-color);
            font-weight: 500;
          }

          .temperature,
          .max-tokens {
            color: #666;

            .dark & {
              color: #999;
            }
          }
        }

        .config-details {
          display: flex;
          gap: 1rem;
          font-size: 0.8rem;
          color: #888;

          .dark & {
            color: #666;
          }
        }
      }

      .config-actions {
        display: flex;
        gap: 0.5rem;

        .btn {
          padding: 0.5rem 1rem;
          border: none;
          border-radius: 8px;
          font-size: 0.9rem;
          cursor: pointer;
          transition: all 0.3s ease;

          &.btn-small {
            background: #f8f9fa;
            color: var(--text-color);
            border: 1px solid #e1e5e9;

            &:hover {
              background: #e9ecef;
            }

            &:disabled {
              opacity: 0.5;
              cursor: not-allowed;
            }

            &.btn-danger {
              background: #dc3545;
              color: white;
              border-color: #dc3545;

              &:hover {
                background: #c82333;
              }
            }

            .dark & {
              background: #2a2a2a;
              border-color: #444;
              color: #fff;

              &:hover {
                background: #3a3a3a;
              }
            }
          }
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .role-config-page {
    padding: 1rem;

    .title {
      font-size: 2rem;
    }

    .form-actions {
      flex-direction: column;
      align-items: center;

      .btn {
        width: 100%;
        max-width: 200px;
      }
    }

    .config-item {
      flex-direction: column;
      align-items: stretch;
      gap: 1rem;

      .config-info {
        margin-right: 0;
      }

      .config-actions {
        justify-content: center;
      }

      .config-meta,
      .config-details {
        flex-direction: column;
        gap: 0.5rem;
      }
    }

    .template-buttons {
      justify-content: center;
    }
  }
}
</style>
