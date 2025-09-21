<template>
  <div class="chat-page">
    <!-- 角色选择页面 -->
    <div v-if="!selectedConfig" class="role-selection">
      <div class="header-section">
        <h1 class="page-title">中医角色选择</h1>
        <div class="header-actions">
          <router-link to="/config" class="btn btn-primary">
            <i class="icon-plus"></i>新建角色
          </router-link>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>正在加载角色配置...</p>
      </div>

      <div v-else-if="error" class="error-state">
        <div class="error-icon">⚠️</div>
        <p>{{ error }}</p>
        <button @click="loadConfigs" class="btn btn-secondary">重试</button>
      </div>

      <div v-else-if="!savedConfigs.length" class="empty-state">
        <div class="empty-icon">🍃</div>
        <p>暂无角色配置</p>
        <router-link to="/config" class="btn btn-primary">去配置角色</router-link>
      </div>

      <div v-else class="role-list">
        <div
            v-for="config in savedConfigs"
            :key="config.id"
            class="role-card"
            @click="selectRole(config)"
        >
          <div class="role-header">
            <div class="role-avatar">
              <i class="icon-user-md"></i>
            </div>
            <div class="role-info">
              <h3>{{ config.roleName }}</h3>
              <p class="role-description">{{ config.description }}</p>
            </div>
          </div>
          <div class="role-footer">
            <span class="role-model">{{ config.model }}</span>
            <button @click.stop="deleteRole(config.id)" class="btn-delete">
              <i class="icon-trash"></i>删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 聊天页面 -->
    <div v-else class="chat-area">
      <div class="chat-header">
        <button @click="backToRoles" class="btn btn-icon">
          <i class="icon-arrow-left"></i>
        </button>
        <div class="chat-title">
          <h2>{{ selectedConfig.roleName }}</h2>
          <p class="chat-subtitle">{{ selectedConfig.description }}</p>
        </div>
        <button @click="clearChat" class="btn btn-secondary">
          <i class="icon-trash"></i>清空
        </button>
      </div>

      <div class="messages-container" ref="messagesContainer">
        <div v-if="!messages.length" class="welcome">
          <div class="welcome-icon">🌿</div>
          <h3>欢迎咨询</h3>
          <p>与 {{ selectedConfig.roleName }} 开始对话</p>
        </div>

        <div v-else class="messages">
          <div
              v-for="message in messages"
              :key="message.id"
              class="message"
              :class="message.role"
          >
            <div class="message-avatar">
              <i v-if="message.role === 'user'" class="icon-user"></i>
              <i v-else class="icon-herbal"></i>
            </div>
            <div class="message-content-wrapper">
              <div class="message-content">
                {{ message.content }}
                <button
                    v-if="message.role === 'assistant' && message.content"
                    @click="playTTS(message.content)"
                    class="play-voice-btn"
                    title="播放语音"
                >
                  🔊
                </button>
              </div>
              <div class="message-time">{{ formatTime(message.id) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-container">
          <textarea
              v-model="inputMessage"
              @keydown.enter.exact.prevent="sendMessage"
              @keydown.enter.shift.exact.prevent="inputMessage += '\n'"
              placeholder="输入消息... (Shift+Enter换行)"
              class="message-input"
              rows="3"
          ></textarea>
          <button
              @click="sendMessage"
              class="btn btn-primary send-btn"
              :disabled="!inputMessage.trim() || sending"
          >
            <i :class="sending ? 'icon-loading' : 'icon-send'"></i>
            {{ sending ? '发送中...' : '发送' }}
          </button>
        </div>
        <div class="input-hint">
          <span>按 Enter 发送，Shift + Enter 换行</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { userConfigAPI } from '../api/api.js'

const selectedConfig = ref(null)
const savedConfigs = ref([])
const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const messagesContainer = ref(null)
const loading = ref(false)
const error = ref('')

// TTS相关状态
const playingMessageId = ref(null)
const audioInstances = new Map()

// 加载配置
const loadConfigs = async () => {
  loading.value = true
  error.value = ''
  try {
    const configs = await userConfigAPI.getConfigList('default')
    savedConfigs.value = Array.isArray(configs) ? configs : []
    if (!Array.isArray(configs)) error.value = '配置数据格式错误'
  } catch (err) {
    error.value = '加载配置失败'
  } finally {
    loading.value = false
  }
}

// TTS语音合成功能 - 直接调用后端
const playTTS = async (text, messageId = Date.now().toString()) => {
  try {
    // 如果正在播放其他音频，先停止
    if (playingMessageId.value && playingMessageId.value !== messageId) {
      stopCurrentAudio()
    }

    // 如果正在播放同一条消息的音频，则停止播放
    if (playingMessageId.value === messageId) {
      stopCurrentAudio()
      return
    }

    playingMessageId.value = messageId
    console.debug('[TTS] 开始请求语音合成:', text.substring(0, 50) + '...')

    // 直接调用后端TTS接口
    const response = await fetch('http://localhost:8989/tts/synthesize', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ text })
    })

    console.debug('[TTS] 返回状态:', response.status)

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`TTS服务错误: ${response.status}`)
    }

    // 获取音频数据
    const audioBlob = await response.blob()
    if (audioBlob.size === 0) {
      throw new Error('返回的音频数据为空')
    }

    // 创建音频实例
    const audioUrl = URL.createObjectURL(audioBlob)
    const audio = new Audio(audioUrl)

    // 存储音频实例
    audioInstances.set(messageId, { audio, url: audioUrl })

    // 设置音频事件监听
    audio.onerror = (err) => {
      console.error('[TTS] 音频播放出错:', err)
      cleanupAudio(messageId)
      playingMessageId.value = null
    }

    audio.onended = () => {
      console.debug('[TTS] 音频播放完成')
      cleanupAudio(messageId)
      playingMessageId.value = null
    }

    // 开始播放
    await audio.play()
    console.debug('[TTS] 音频开始播放')

  } catch (error) {
    console.error('[TTS] 播放失败:', error)
    playingMessageId.value = null

    // 显示友好的错误信息
    let errorMessage = '语音播放失败'
    if (error.message.includes('TTS服务错误')) {
      errorMessage = 'TTS服务暂时不可用'
    } else if (error.message.includes('网络')) {
      errorMessage = '网络连接异常'
    }

    alert(errorMessage)
  }
}

// 停止当前播放的音频
const stopCurrentAudio = () => {
  if (playingMessageId.value) {
    const instance = audioInstances.get(playingMessageId.value)
    if (instance) {
      instance.audio.pause()
      instance.audio.currentTime = 0
      cleanupAudio(playingMessageId.value)
    }
    playingMessageId.value = null
  }
}

// 清理音频资源
const cleanupAudio = (messageId) => {
  const instance = audioInstances.get(messageId)
  if (instance) {
    URL.revokeObjectURL(instance.url)
    audioInstances.delete(messageId)
  }
}

// 清理所有音频资源
const cleanupAllAudio = () => {
  audioInstances.forEach((instance) => {
    instance.audio.pause()
    URL.revokeObjectURL(instance.url)
  })
  audioInstances.clear()
  playingMessageId.value = null
}

onMounted(loadConfigs)

// 组件卸载时清理资源
onUnmounted(() => {
  cleanupAllAudio()
})

// 选择角色
const selectRole = (config) => {
  selectedConfig.value = config
  messages.value = []
  cleanupAllAudio() // 切换角色时清理音频
}

// 返回角色选择
const backToRoles = () => {
  selectedConfig.value = null
  messages.value = []
  cleanupAllAudio() // 返回时清理音频
}

// 清空对话
const clearChat = () => {
  if (confirm('确定清空对话？')) {
    messages.value = []
    cleanupAllAudio() // 清空对话时清理音频
  }
}

// 删除角色
const deleteRole = async (configId) => {
  if (confirm('确定删除角色？')) {
    try {
      await userConfigAPI.deleteConfig(configId, 'default')
      await loadConfigs()
      if (selectedConfig.value?.id === configId) backToRoles()
    } catch (err) {
      alert('删除角色失败')
    }
  }
}

// 发送消息
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || sending.value) return

  messages.value.push({
    id: Date.now().toString(),
    role: 'user',
    content: message
  })
  inputMessage.value = ''
  sending.value = true

  const aiMessage = { id: (Date.now() + 1).toString(), role: 'assistant', content: '' }
  messages.value.push(aiMessage)

  try {
    const chatId = Date.now().toString()
    const reader = await userConfigAPI.userChat(message, chatId, selectedConfig.value.id)
    const decoder = new TextDecoder('utf-8')

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      aiMessage.content += chunk
      messages.value = [...messages.value]
      await nextTick()
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }

    await nextTick()
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  } catch (err) {
    aiMessage.content = '发送消息失败: ' + err.message
    messages.value = [...messages.value]
  } finally {
    sending.value = false
    await nextTick()
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 时间格式化
const formatTime = (timestamp) => {
  const date = new Date(Number(timestamp))
  return date.toLocaleTimeString()
}
</script>
<style scoped>/* 基础色调改为中医传统色系 - 米色背景、棕色主调 */
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 900px;
  margin: 0 auto;
  font-family: "SimSun", "STSong", serif;
  background-color: #f5f1e6;
  background-image: url("https://img95.699pic.com/photo/50122/3990.jpg_wh860.jpg");
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  background-repeat: no-repeat;
  background-blend-mode: overlay;
}

/* 页面标题 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: rgba(255, 248, 225, 0.9);
  border-bottom: 2px solid #d7ccc8;
}

.page-title {
  margin: 0;
  color: #5d4037;
  font-size: 1.8rem;
  font-weight: normal;
}

/* 按钮样式 */
.btn {
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: "SimSun", serif;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-primary {
  background: #8b5a2b;
  color: #fff8e1;
  box-shadow: 0 2px 6px rgba(139, 90, 43, 0.3);
}

.btn-primary:hover {
  background: #6d4c41;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(139, 90, 43, 0.4);
}

.btn-secondary {
  background: #a1887f;
  color: #fff8e1;
}

.btn-secondary:hover {
  background: #8d6e63;
}

.btn-icon {
  padding: 8px 12px;
  background: #8b5a2b;
  color: #fff8e1;
}

.btn-icon:hover {
  background: #6d4c41;
}

/* 角色卡片 */
.role-list {
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.role-card {
  background: rgba(255, 253, 245, 0.85);
  border: 1px solid #d7ccc8;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.role-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
  border-color: #8b5a2b;
}

.role-header {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.role-avatar {
  width: 50px;
  height: 50px;
  background: #d7ccc8;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: #5d4037;
}

.role-info h3 {
  margin: 0 0 8px 0;
  color: #5d4037;
  font-size: 1.2rem;
}

.role-description {
  margin: 0;
  color: #8d6e63;
  font-size: 0.9rem;
  line-height: 1.4;
}

.role-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed #d7ccc8;
  padding-top: 15px;
}

.role-model {
  background: #8b5a2b;
  color: #fff8e1;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.8rem;
}

.btn-delete {
  background: none;
  border: none;
  color: #e57373;
  cursor: pointer;
  padding: 5px 10px;
  font-size: 0.9rem;
}

.btn-delete:hover {
  color: #f44336;
  background: rgba(244, 67, 54, 0.1);
  border-radius: 4px;
}

/* 聊天区域 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: rgba(255, 248, 225, 0.95);
  border-bottom: 2px solid #d7ccc8;
}

.chat-title h2 {
  margin: 0 0 4px 0;
  color: #5d4037;
  font-size: 1.4rem;
  font-weight: normal;
}

.chat-subtitle {
  margin: 0;
  color: #8d6e63;
  font-size: 0.9rem;
}

/* 消息容器 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: rgba(255, 253, 245, 0.7);
  margin: 15px;
  border-radius: 12px;
  box-shadow: inset 0 0 10px rgba(139, 90, 43, 0.05);
}

/* 欢迎区域 */
.welcome {
  text-align: center;
  padding: 40px 20px;
  color: #8d6e63;
}

.welcome-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

.welcome h3 {
  margin: 0 0 10px 0;
  color: #5d4037;
}

/* 消息气泡 */
.messages {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message.user {
  align-self: flex-end;
}

.message.assistant {
  align-self: flex-start;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 1.2rem;
}

.message.user .message-avatar {
  background: #d7ccc8;
  color: #5d4037;
}

.message.assistant .message-avatar {
  background: #c8e6c9;
  color: #2e7d32;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
}

.message-content {
  padding: 15px;
  border-radius: 18px;
  line-height: 1.5;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.message.user .message-content {
  background: #d7ccc8;
  color: #3e2723;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  background: #e8f5e9;
  color: #2e7d32;
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 0.7rem;
  color: #8d6e63;
  margin-top: 5px;
  padding: 0 10px;
}

/* 输入区域 */
.input-area {
  padding: 15px 20px;
  background: rgba(255, 248, 225, 0.9);
  border-top: 2px solid #d7ccc8;
}

.input-container {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.message-input {
  flex: 1;
  padding: 12px 15px;
  border: 1px solid #d7ccc8;
  border-radius: 10px;
  resize: none;
  background-color: #fff8e1;
  color: #5d4037;
  font-family: "SimSun", serif;
  font-size: 1rem;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message-input:focus {
  outline: none;
  border-color: #8b5a2b;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1), 0 0 0 2px rgba(139, 90, 43, 0.2);
}

.send-btn {
  align-self: flex-end;
  height: fit-content;
  padding: 12px 20px;
}

.input-hint {
  text-align: right;
  font-size: 0.8rem;
  color: #8d6e63;
}

/* 状态页面 */
.loading-state, .error-state, .empty-state {
  text-align: center;
  padding: 50px 20px;
  color: #8d6e63;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #d7ccc8;
  border-top: 4px solid #8b5a2b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon, .empty-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

/* 图标字体 */
.icon-plus::before { content: "+"; }
.icon-trash::before { content: "🗑"; }
.icon-arrow-left::before { content: "←"; }
.icon-send::before { content: "➤"; }
.icon-loading::before { content: "⏳"; }
.icon-user::before { content: "👤"; }
.icon-user-md::before { content: "👨‍⚕️"; }
.icon-herbal::before { content: "🌿"; }

/* 响应式设计 */
@media (max-width: 768px) {
  .role-list {
    grid-template-columns: 1fr;
    padding: 15px;
  }

  .message {
    max-width: 90%;
  }

  .header-section {
    padding: 15px;
  }

  .chat-header {
    padding: 12px 15px;
  }

  .messages-container {
    padding: 15px;
    margin: 10px;
  }
}

.play-voice-btn {
  margin-left: 8px;
  cursor: pointer;
  border: none;
  background: none;
  font-size: 16px;
}

/* 在你的Vue组件的<style>标签中添加以下样式 */

.play-voice-btn {
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
  margin-left: 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
  opacity: 0.7;
}

.play-voice-btn:hover {
  opacity: 1;
  background: rgba(0, 0, 0, 0.1);
}

.play-voice-btn.playing {
  opacity: 1;
  background: rgba(76, 175, 80, 0.2);
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

/* 消息内容容器样式调整 */
.message-content {
  position: relative;
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

/* 确保按钮在消息右侧对齐 */
.message-content-wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.message-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  word-wrap: break-word;
  white-space: pre-wrap;
}

</style>