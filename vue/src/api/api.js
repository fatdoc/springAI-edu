const BASE_URL = 'http://localhost:8989'
const TIMEOUT = 30000 // 30秒超时

// 统一的错误处理
class APIError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
    this.name = 'APIError'
  }
}

// 统一的请求处理函数
async function fetchWithTimeout(url, options = {}) {
  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), TIMEOUT)

  try {
    const response = await fetch(url, {
      ...options,
      signal: controller.signal
    })

    if (!response.ok) {
      throw new APIError(`HTTP error! status: ${response.status}`, response.status)
    }

    return response
  } finally {
    clearTimeout(timeoutId)
  }
}

// 构建URL的辅助函数
function buildUrl(path, params = {}) {
  const url = new URL(`${BASE_URL}${path}`)
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      url.searchParams.append(key, value)
    }
  })
  return url
}

export const chatAPI = {
  // 发送聊天消息
  async simpleChat(data, chatId) {
    try {
      const url = buildUrl('/simple/chat', { chatId })
      const response = await fetchWithTimeout(url, {
        method: 'POST',
        body: data instanceof FormData ? data : new URLSearchParams({ prompt: data })
      })
      return response.body.getReader()
    } catch (error) {
      console.error('Simple Chat Error:', error)
      throw error
    }
  },

  // 获取聊天的历史会话id列表
  async chatTypeHistoryList(type = 1) {
    try {
      const url = buildUrl('/chat/type/history/list', { type })
      const response = await fetchWithTimeout(url)
      const chats = await response.json()

      return chats.map(chat => ({
        id:chat.chatId,
        title: chat.title === '' || chat.title === null  || chat.title === undefined ? `新的对话` : chat.title
      }))
    } catch (error) {
      console.error('History Chat ID List Error:', error)
      return []
    }
  },

  // 获取具体对话下的历史消息
  async chatHistoryMessageList(chatId, type = 1) {
    try {
      const url = buildUrl('/chat/history/message/list', { chatId, type })
      const response = await fetchWithTimeout(url)
      const messages = await response.json()

      return messages.map(msg => ({
        ...msg,
        timestamp: new Date()
      }))
    } catch (error) {
      console.error('History Chat History List Error:', error)
      return []
    }
  },

  // 发送助手消息
  async sendAssistantMessage(prompt, chatId) {
    try {
      const url = buildUrl('/program/chat', { prompt, chatId })
      const response = await fetchWithTimeout(url)
      return response.body.getReader()
    } catch (error) {
      console.error('Assistant Message Error:', error)
      throw error
    }
  },

  // 发送rag消息
  async sendRagMessage(prompt, chatId) {
    try {
      const url = buildUrl('/program/rag', { prompt, chatId })
      const response = await fetchWithTimeout(url)
      return response.body.getReader()
    } catch (error) {
      console.error('RAG Message Error:', error)
      throw error
    }
  },

  // 删除对话
  async deleteChat(chatId, type = 1) {
    try {
      const url = buildUrl('/chat/delete', { chatId, type })
      await fetchWithTimeout(url)
      return true
    } catch (error) {
      console.error('Delete Chat Error:', error)
      throw error
    }
  }
}

export const userConfigAPI = {
  // 获取用户配置列表
  async getConfigList(userId = 'default') {
    try {
      const url = buildUrl('/user-config/list', { userId });
      const response = await fetchWithTimeout(url);
      const result = await response.json();

      // 修改成功状态检查，假设 code: 0 表示成功
      if (result.code === 0) {
        return result.data || [];
      } else {
        throw new Error(result.message || '获取配置列表失败');
      }
    } catch (error) {
      console.error('Get Config List Error:', error);
      throw error; // 抛出错误供上层处理
    }
  },

  // 保存用户配置
  async saveConfig(config) {
    try {
      const response = await fetchWithTimeout(`${BASE_URL}/user-config/save`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config)
      })
      const result = await response.json()
      
      if (result.success) {
        return result.data
      } else {
        throw new Error(result.message || '保存配置失败')
      }
    } catch (error) {
      console.error('Save Config Error:', error)
      throw error
    }
  },

  // 删除用户配置
  async deleteConfig(id, userId = 'default') {
    try {
      const url = buildUrl('/user-config/delete', { id, userId })
      const response = await fetchWithTimeout(url, { method: 'DELETE' })
      const result = await response.json()
      
      if (result.success) {
        return result.data
      } else {
        throw new Error(result.message || '删除配置失败')
      }
    } catch (error) {
      console.error('Delete Config Error:', error)
      throw error
    }
  },

  // 测试配置连接
  async testConnection(config) {
    try {
      const response = await fetchWithTimeout(`${BASE_URL}/user-config/test`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config)
      })
      const result = await response.json()
      
      if (result.success) {
        return result.data
      } else {
        throw new Error(result.message || '连接测试失败')
      }
    } catch (error) {
      console.error('Test Connection Error:', error)
      throw error
    }
  },

  // 基于用户配置的聊天
  async userChat(prompt, chatId, configId) {
    try {
      const url = buildUrl('/user-chat/chat', { prompt, chatId, configId })
      const response = await fetchWithTimeout(url)
      return response.body.getReader()
    } catch (error) {
      console.error('User Chat Error:', error)
      throw error
    }
  }
}

export const ragQueryAPI = {
  // 流式查询版本 - 使用统一的BASE_URL和工具函数
  async ask(question, kbId, chatId = null, topK ) {
    try {
      const response = await fetchWithTimeout(`${BASE_URL}/rag-query/ask`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          question,
          kbId,
          chatId,
          topK
        })
      })
      return response.body.getReader()
    } catch (error) {
      console.error('RAG Query Error:', error)
      throw error
    }
  }
}

export const fileRagAPI = {
  // 上传知识库
  async uploadKnowledgeBase({ file, name, remark }) {
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('name', name)
      formData.append('remark', remark)
      const response = await fetchWithTimeout(`${BASE_URL}/file-rag/upload`, {
        method: 'POST',
        body: formData
      })
      return response.json()
    } catch (error) {
      console.error('Upload Knowledge Base Error:', error)
      throw error
    }
  },

  // 获取知识库列表
  async listKnowledgeBases() {
    try {
      const response = await fetchWithTimeout(`${BASE_URL}/file-rag/list`)
      return response.json()
    } catch (error) {
      console.error('List Knowledge Bases Error:', error)
      throw error
    }
  }
}