<template>
  <div class="config-page">
    <div class="container">
      <h1 class="title">AI 配置</h1>
      <p class="subtitle">配置大模型参数和角色提示词</p>
      
      <div class="config-form">
        <div class="form-section">
          <h2>模型配置</h2>
          <div class="form-group">
            <label for="apiKey">API Key</label>
            <input 
              id="apiKey"
              v-model="config.apiKey" 
              type="password" 
              placeholder="请输入您的 API Key"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label for="baseUrl">Base URL</label>
            <input 
              id="baseUrl"
              v-model="config.baseUrl" 
              type="text" 
              placeholder="https://api.openai.com/v1"
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="model">模型名称</label>
            <select v-model="config.model" class="form-select">
              <option value="gpt-3.5-turbo">GPT-3.5 Turbo</option>
              <option value="gpt-4">GPT-4</option>
              <option value="gpt-4-turbo">GPT-4 Turbo</option>
              <option value="claude-3-sonnet">Claude-3 Sonnet</option>
              <option value="claude-3-opus">Claude-3 Opus</option>
              <option value="claude-3-opus">deepseek-chat</option>
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
        </div>
        
        <div class="form-section">
          <h2>角色配置</h2>
          <div class="form-group">
            <label for="roleName">角色名称</label>
            <input 
              id="roleName"
              v-model="config.roleName" 
              type="text" 
              placeholder="例如：编程助手、客服助手"
              class="form-input"
            />
          </div>
          
          <div class="form-group">
            <label for="systemPrompt">系统提示词</label>
            <textarea 
              id="systemPrompt"
              v-model="config.systemPrompt" 
              placeholder="请输入角色的系统提示词，定义角色的行为和风格..."
              class="form-textarea"
              rows="6"
            ></textarea>
          </div>
          
          <div class="form-group">
            <label for="description">角色描述</label>
            <textarea 
              id="description"
              v-model="config.description" 
              placeholder="简要描述这个角色的功能和特点..."
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>
        </div>
        
        <div class="form-actions">
          <button @click="saveConfig" class="btn btn-primary" :disabled="saving">
            {{ saving ? '保存中...' : '保存配置' }}
          </button>
          <button @click="testConnection" class="btn btn-secondary" :disabled="testing">
            {{ testing ? '测试中...' : '测试连接' }}
          </button>
        </div>
      </div>
      
      <div v-if="savedConfigs.length > 0" class="saved-configs">
        <h2>已保存的配置</h2>
        <div class="config-list">
          <div 
            v-for="config in savedConfigs" 
            :key="config.id"
            class="config-item"
          >
            <div class="config-info">
              <h3>{{ config.roleName }}</h3>
              <p>{{ config.description }}</p>
              <div class="config-meta">
                <span>模型: {{ config.model }}</span>
                <span>温度: {{ config.temperature }}</span>
              </div>
            </div>
            <div class="config-actions">
              <button @click="loadConfig(config)" class="btn btn-small">加载</button>
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
import { userConfigAPI } from '../api/api.js'

const isDark = useDark()
const saving = ref(false)
const testing = ref(false)

const config = reactive({
  userId: 'default', // 默认用户ID
  apiKey: '',
  baseUrl: 'https://api.openai.com/v1',
  model: 'gpt-3.5-turbo',
  temperature: 0.7,
  maxTokens: 2000,
  roleName: '',
  systemPrompt: '',
  description: ''
})

const savedConfigs = ref([])

// 保存配置到后端
const saveConfig = async () => {
  if (!config.apiKey || !config.roleName || !config.systemPrompt) {
    alert('请填写完整的配置信息')
    return
  }
  
  saving.value = true
  try {
    await userConfigAPI.saveConfig(config)
    
    alert('配置保存成功！')
    
    // 重新加载配置列表
    await loadConfigs()
    
    // 清空表单
    Object.assign(config, {
      apiKey: '',
      roleName: '',
      systemPrompt: '',
      description: ''
    })
  } catch (error) {
    console.error('保存配置失败:', error)
    alert('保存配置失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 测试连接
const testConnection = async () => {
  if (!config.apiKey || !config.baseUrl) {
    alert('请填写 API Key 和 Base URL')
    return
  }
  
  testing.value = true
  try {
    await userConfigAPI.testConnection(config)
    alert('连接测试成功！')
  } catch (error) {
    console.error('连接测试失败:', error)
    alert('连接测试失败: ' + error.message)
  } finally {
    testing.value = false
  }
}

// 加载配置
const loadConfig = (savedConfig) => {
  Object.assign(config, savedConfig)
}

// 删除配置
const deleteConfig = async (configId) => {
  if (confirm('确定要删除这个配置吗？')) {
    try {
      await userConfigAPI.deleteConfig(configId, config.userId)
      await loadConfigs()
    } catch (error) {
      console.error('删除配置失败:', error)
      alert('删除配置失败: ' + error.message)
    }
  }
}

// 加载已保存的配置
const loadConfigs = async () => {
  try {
    const configs = await userConfigAPI.getConfigList(config.userId)
    savedConfigs.value = configs
  } catch (error) {
    console.error('加载配置失败:', error)
    savedConfigs.value = []
  }
}

// 初始化时加载配置
onMounted(() => {
  loadConfigs()
})
</script>

<style scoped lang="scss">
.config-page {
  min-height: 100vh;
  padding: 2rem;
  background: var(--bg-color);
  transition: background-color 0.3s;

  .container {
    max-width: 800px;
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
      padding: 1rem;
      border: 1px solid #e1e5e9;
      border-radius: 12px;
      background: var(--bg-color);
      transition: all 0.3s ease;

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
      }

      .config-info {
        flex: 1;

        h3 {
          margin: 0 0 0.5rem 0;
          color: var(--text-color);
          font-size: 1.1rem;
        }

        p {
          margin: 0 0 0.5rem 0;
          color: #666;
          font-size: 0.9rem;

          .dark & {
            color: #999;
          }
        }

        .config-meta {
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
  .config-page {
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
    }
  }
}
</style> 