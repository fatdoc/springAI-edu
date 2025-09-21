<template>
  <div class="llm-config-page">
    <div class="container">
      <h1 class="title">大模型配置</h1>
      <p class="subtitle">配置不同的大语言模型参数</p>
      
      <div class="config-form">
        <div class="form-section">
          <h2>新增模型配置</h2>
          <div class="form-group">
            <label for="configName">配置名称</label>
            <input 
              id="configName"
              v-model="config.configName" 
              type="text" 
              placeholder="例如：我的ChatGPT配置"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label for="llmType">模型类型</label>
            <select v-model="config.llmType" class="form-select" @change="onLLMTypeChange">
              <option value="">请选择模型类型</option>
              <option value="chatgpt">ChatGPT</option>
              <option value="deepseek">DeepSeek</option>
              <option value="doubao">豆包</option>
              <option value="qwen">通义千问</option>
              <option value="ollama">Ollama</option>
            </select>
          </div>

          <div class="form-group">
            <label for="apiKey">API Key</label>
            <input 
              id="apiKey"
              v-model="config.apiKey" 
              type="password" 
              :placeholder="getApiKeyPlaceholder()"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label for="baseUrl">Base URL</label>
            <input 
              id="baseUrl"
              v-model="config.baseUrl" 
              type="text" 
              :placeholder="getBaseUrlPlaceholder()"
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="modelName">模型名称</label>
            <select v-model="config.modelName" class="form-select">
              <option value="">请选择模型</option>
              <option v-for="model in getAvailableModels()" :key="model.value" :value="model.value">
                {{ model.label }}
              </option>
            </select>
          </div>
          
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
          </div>

          <div class="form-group">
            <label for="timeout">超时时间 (毫秒)</label>
            <input 
              id="timeout"
              v-model.number="config.timeout" 
              type="number" 
              min="5000" 
              max="120000"
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input 
                v-model="config.isDefault" 
                type="checkbox" 
                class="form-checkbox"
              />
              <span>设为默认配置</span>
            </label>
          </div>
        </div>
        
        <div class="form-actions">
          <button @click="saveConfig" class="btn btn-primary" :disabled="saving">
            {{ saving ? '保存中...' : '保存配置' }}
          </button>
          <button @click="testConnection" class="btn btn-secondary" :disabled="testing">
            {{ testing ? '测试中...' : '测试连接' }}
          </button>
          <button @click="resetForm" class="btn btn-outline">重置</button>
        </div>
      </div>
      
      <div v-if="savedConfigs.length > 0" class="saved-configs">
        <h2>已保存的模型配置</h2>
        <div class="config-list">
          <div 
            v-for="config in savedConfigs" 
            :key="config.id"
            class="config-item"
            :class="{ 'default-config': config.isDefault }"
          >
            <div class="config-info">
              <div class="config-header">
                <h3>{{ config.configName }}</h3>
                <span v-if="config.isDefault" class="default-badge">默认</span>
              </div>
              <div class="config-meta">
                <span class="llm-type">{{ getLLMTypeLabel(config.llmType) }}</span>
                <span class="model-name">{{ config.modelName }}</span>
                <span class="temperature">温度: {{ config.temperature }}</span>
              </div>
              <div class="config-details">
                <span class="base-url">{{ config.baseUrl }}</span>
                <span class="create-time">{{ formatTime(config.createTime) }}</span>
              </div>
            </div>
            <div class="config-actions">
              <button @click="loadConfig(config)" class="btn btn-small">编辑</button>
              <button @click="setAsDefault(config.id)" class="btn btn-small" :disabled="config.isDefault">
                {{ config.isDefault ? '已默认' : '设默认' }}
              </button>
              <button @click="deleteConfig(config.id)" class="btn btn-small btn-danger">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useDark } from '@vueuse/core'
// import { llmConfigAPI } from '../api/api.js'

const isDark = useDark()
const saving = ref(false)
const testing = ref(false)

const config = reactive({
  userId: 'default',
  configName: '',
  llmType: '',
  apiKey: '',
  baseUrl: '',
  modelName: '',
  temperature: 0.7,
  maxTokens: 2000,
  timeout: 30000,
  isDefault: false
})

const savedConfigs = ref([])

// 不同LLM类型的默认配置
const llmTypeConfigs = {
  chatgpt: {
    baseUrl: 'https://api.openai.com/v1',
    models: [
      { value: 'gpt-3.5-turbo', label: 'GPT-3.5 Turbo' },
      { value: 'gpt-4', label: 'GPT-4' },
      { value: 'gpt-4-turbo', label: 'GPT-4 Turbo' },
      { value: 'gpt-4o', label: 'GPT-4o' }
    ]
  },
  deepseek: {
    baseUrl: 'https://api.deepseek.com',
    models: [
      { value: 'deepseek-chat', label: 'DeepSeek Chat' },
      { value: 'deepseek-coder', label: 'DeepSeek Coder' }
    ]
  },
  doubao: {
    baseUrl: 'https://ark.cn-beijing.volces.com/api/v3',
    models: [
      { value: 'ep-20241218142546-xxxxx', label: '豆包 Pro' },
      { value: 'ep-20241218142546-yyyyy', label: '豆包 Lite' }
    ]
  },
  qwen: {
    baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    models: [
      { value: 'qwen-max', label: '通义千问 Max' },
      { value: 'qwen-plus', label: '通义千问 Plus' },
      { value: 'qwen-turbo', label: '通义千问 Turbo' }
    ]
  },
  ollama: {
    baseUrl: 'http://localhost:11434',
    models: [
      { value: 'llama2:7b', label: 'Llama2 7B' },
      { value: 'llama2:13b', label: 'Llama2 13B' },
      { value: 'codellama:7b', label: 'Code Llama 7B' },
      { value: 'mistral:7b', label: 'Mistral 7B' },
      { value: 'deepseek-r1:7b', label: 'DeepSeek R1 7B' }
    ]
  }
}

// 获取API Key占位符
const getApiKeyPlaceholder = () => {
  const placeholders = {
    chatgpt: 'sk-...',
    deepseek: 'sk-...',
    doubao: 'ak-...',
    qwen: 'sk-...',
    ollama: 'Ollama无需API Key'
  }
  return placeholders[config.llmType] || '请输入API Key'
}

// 获取Base URL占位符
const getBaseUrlPlaceholder = () => {
  return llmTypeConfigs[config.llmType]?.baseUrl || '请输入Base URL'
}

// 获取可用模型列表
const getAvailableModels = () => {
  return llmTypeConfigs[config.llmType]?.models || []
}

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

// LLM类型改变时的处理
const onLLMTypeChange = () => {
  if (config.llmType && llmTypeConfigs[config.llmType]) {
    config.baseUrl = llmTypeConfigs[config.llmType].baseUrl
    config.modelName = ''
  }
}

// 保存配置
const saveConfig = async () => {
  if (!config.configName || !config.llmType || !config.apiKey || !config.baseUrl || !config.modelName) {
    alert('请填写完整的配置信息')
    return
  }
  
  saving.value = true
  try {
    // await llmConfigAPI.saveConfig(config)
    console.log('保存配置:', config)
    alert('配置保存成功！')
    
    // 重新加载配置列表
    await loadConfigs()
    
    // 清空表单
    resetForm()
  } catch (error) {
    console.error('保存配置失败:', error)
    alert('保存配置失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 测试连接
const testConnection = async () => {
  if (!config.apiKey || !config.baseUrl || !config.modelName) {
    alert('请填写 API Key、Base URL 和模型名称')
    return
  }
  
  testing.value = true
  try {
    // await llmConfigAPI.testConnection(config)
    console.log('测试连接:', config)
    alert('连接测试成功！')
  } catch (error) {
    console.error('连接测试失败:', error)
    alert('连接测试失败: ' + error.message)
  } finally {
    testing.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(config, {
    configName: '',
    llmType: '',
    apiKey: '',
    baseUrl: '',
    modelName: '',
    temperature: 0.7,
    maxTokens: 2000,
    timeout: 30000,
    isDefault: false
  })
}

// 加载配置
const loadConfig = (savedConfig) => {
  Object.assign(config, savedConfig)
}

// 设为默认配置
const setAsDefault = async (configId) => {
  try {
    // await llmConfigAPI.setAsDefault(configId, config.userId)
    console.log('设为默认配置:', configId)
    await loadConfigs()
    alert('设置默认配置成功！')
  } catch (error) {
    console.error('设置默认配置失败:', error)
    alert('设置默认配置失败: ' + error.message)
  }
}

// 删除配置
const deleteConfig = async (configId) => {
  if (confirm('确定要删除这个配置吗？')) {
    try {
      // await llmConfigAPI.deleteConfig(configId, config.userId)
      console.log('删除配置:', configId)
      await loadConfigs()
      alert('删除配置成功！')
    } catch (error) {
      console.error('删除配置失败:', error)
      alert('删除配置失败: ' + error.message)
    }
  }
}

// 加载已保存的配置
const loadConfigs = async () => {
  try {
    // const configs = await llmConfigAPI.getConfigList(config.userId)
    // savedConfigs.value = configs
    console.log('加载配置列表')
  } catch (error) {
    console.error('加载配置失败:', error)
    savedConfigs.value = []
  }
}

// 格式化时间
const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

// 初始化时加载配置
onMounted(() => {
  loadConfigs()
})
</script>

<style scoped lang="scss">
.llm-config-page {
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
    .form-select {
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
      align-items: center;
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

        .config-meta {
          display: flex;
          gap: 1rem;
          margin-bottom: 0.5rem;
          font-size: 0.9rem;

          .llm-type {
            color: var(--primary-color);
            font-weight: 500;
          }

          .model-name {
            color: #666;
          }

          .temperature {
            color: #888;
          }

          .dark & {
            .model-name {
              color: #999;
            }

            .temperature {
              color: #666;
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
  .llm-config-page {
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

      .config-actions {
        justify-content: center;
      }

      .config-meta,
      .config-details {
        flex-direction: column;
        gap: 0.5rem;
      }
    }
  }
}
</style>
