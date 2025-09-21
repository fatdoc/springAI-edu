<template>
  <div class="chat-container">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <h2>中药智能助手</h2>
      <p>专业的中药咨询服务，为您解答中药相关问题</p>
    </div>

    <!-- 消息列表 -->
    <div class="chat-messages" ref="messagesContainer">
      <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message', message.role]"
      >
        <div class="message-content">
          <div class="avatar">
            <img
                :src="message.role === 'user' ? userAvatar : botAvatar"
                :alt="message.role"
            />
          </div>
          <div class="text">
            <!-- 显示加载动画 -->
            <div v-if="message.role === 'bot' && message.loading" class="typing-indicator">
              <span></span><span></span><span></span>
            </div>

            <!-- 显示中药信息卡片 -->
            <div v-else-if="message.type === 'herb_info'">
              <p v-html="formatMessage(message.text)"></p>
              <div class="herb-card">
                <div class="herb-image-container">
                  <img
                      v-show="!message.imageError && message.data.image_url && !message.data.image_url.includes('default')"
                      :src="message.data.image_url"
                      alt="中药图片"
                      class="herb-image"
                      @error="handleImageError(message)"
                      @load="handleImageLoad(message)"
                  />
                  <div
                      v-show="message.imageError || !message.data.image_url || message.data.image_url.includes('default')"
                      class="image-placeholder"
                  >
                    <div class="placeholder-content">
                      <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#6c757d" stroke-width="1.5">
                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                        <circle cx="8.5" cy="8.5" r="1.5"/>
                        <polyline points="21,15 16,10 5,21"/>
                      </svg>
                      <span>{{ message.imageError ? '图片加载失败' : '暂无图片' }}</span>
                    </div>
                  </div>
                </div>
                <div class="herb-details">
                  <p><strong>名称：</strong>{{ message.data.name }}</p>
                  <p><strong>价格：</strong>{{ message.data.price }}</p>
                  <p>
                    <strong>购买链接：</strong>
                    <a :href="message.data.purchase_url" target="_blank" rel="noopener noreferrer">点击跳转</a>
                  </p>
                  <!-- 开发环境调试信息 -->
                  <p v-if="isDevelopment" style="font-size: 0.8rem; color: #666; margin-top: 10px;">
                    <strong>图片URL：</strong>{{ message.data.image_url }}
                    <br>
                    <strong>加载状态：</strong>{{ message.imageError ? '失败' : '正常' }}
                  </p>
                </div>
              </div>
            </div>

            <!-- 普通聊天消息 -->
            <div v-else>
              <p v-html="formatMessage(message.content)"></p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <input
          v-model="userInput"
          @keyup.enter="sendMessage"
          :disabled="isLoading"
          placeholder="请输入您的问题..."
          type="text"
      />
      <button @click="sendMessage" :disabled="isLoading || !userInput.trim()">
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import axios from 'axios'

// 响应式数据
const messages = ref([
  {
    role: 'bot',
    content: '您好！我是中药智能助手，您可以询问关于中药的问题，比如药材功效、用法等。',
    loading: false
  }
])

const userInput = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
const isDevelopment = ref(process.env.NODE_ENV === 'development')

const userAvatar = 'https://via.placeholder.com/40/4A90E2/FFFFFF?text=U'
const botAvatar = 'https://via.placeholder.com/40/50C878/FFFFFF?text=B'

// 改进的图片加载处理
const handleImageError = (message) => {
  console.error('图片加载失败:', message.data.image_url)
  message.imageError = true

  // 尝试使用代理服务
  if (message.data.image_url && !message.data.image_url.includes('proxy')) {
    const proxyUrl = `https://images.weserv.nl/?url=${encodeURIComponent(message.data.image_url)}`

    const testImg = new Image()
    testImg.onload = () => {
      message.data.image_url = proxyUrl
      message.imageError = false
    }
    testImg.onerror = () => {
      console.warn('代理图片也加载失败')
      message.imageError = true
    }
    testImg.src = proxyUrl
  }
}

const handleImageLoad = (message) => {
  console.log('图片加载成功:', message.data.image_url)
  message.imageError = false
}

// 发送消息方法
const sendMessage = async () => {
  if (!userInput.value.trim() || isLoading.value) return

  const message = userInput.value.trim()

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message,
    loading: false
  })

  // 清空输入框
  userInput.value = ''
  isLoading.value = true

  // 添加机器人加载消息
  messages.value.push({
    role: 'bot',
    content: '',
    loading: true
  })

  try {
    // 使用 URLSearchParams 发送表单数据
    const params = new URLSearchParams()
    params.append('message', message)

    const response = await axios.post('http://localhost:8989/api/chat/message', params, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      timeout: 30000
    })

    // 更新机器人回复
    const lastIndex = messages.value.length - 1
    const res = response.data

    if (res.type === 'herb_info') {
      messages.value[lastIndex].type = 'herb_info'
      messages.value[lastIndex].text = res.text
      messages.value[lastIndex].data = res.data
      messages.value[lastIndex].imageError = false
    } else {
      messages.value[lastIndex].type = 'chat'
      messages.value[lastIndex].content = res.text || res.content
    }
    messages.value[lastIndex].loading = false

  } catch (error) {
    console.error('发送消息失败:', error)

    const lastIndex = messages.value.length - 1
    messages.value[lastIndex].content = '抱歉，服务暂时不可用，请稍后重试。'
    messages.value[lastIndex].loading = false
  } finally {
    isLoading.value = false
  }
}

// 格式化消息内容
const formatMessage = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  scrollToBottom()
})

watch(messages, () => {
  scrollToBottom()
}, { deep: true })
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 900px;
  margin: 0 auto;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: white;
}

.chat-header {
  background: linear-gradient(135deg, #4A90E2, #5e72e4);
  color: white;
  padding: 20px;
  text-align: center;
}

.chat-header h2 {
  margin: 0 0 8px 0;
  font-size: 1.8rem;
  font-weight: 500;
}

.chat-header p {
  margin: 0;
  opacity: 0.9;
  font-size: 0.9rem;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f8f9fa;
}

.message {
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-content {
  display: flex;
  align-items: flex-start;
  max-width: 90%;
}

.message.user .message-content {
  margin-left: auto;
  flex-direction: row-reverse;
}

.avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.text {
  margin: 0 15px;
  max-width: 80%;
}

.message.user .text {
  text-align: right;
}

.text p {
  background-color: white;
  border-radius: 18px;
  padding: 15px 20px;
  margin: 0;
  display: inline-block;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  line-height: 1.5;
  word-wrap: break-word;
  text-align: left;
}

.message.bot .text p {
  background-color: #e3f2fd;
  border-top-left-radius: 4px;
}

.message.user .text p {
  background-color: #4A90E2;
  color: white;
  border-top-right-radius: 4px;
}

.typing-indicator {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background-color: #e3f2fd;
  border-radius: 18px;
  border-top-left-radius: 4px;
  min-height: 24px;
}

.typing-indicator span {
  height: 10px;
  width: 10px;
  background-color: #90a4ae;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-5px);
  }
}

.chat-input {
  display: flex;
  padding: 20px;
  background-color: white;
  border-top: 1px solid #e0e0e0;
}

.chat-input input {
  flex: 1;
  padding: 14px 20px;
  border: 1px solid #e0e0e0;
  border-radius: 30px;
  outline: none;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.chat-input input:focus {
  border-color: #4A90E2;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

.chat-input button {
  margin-left: 15px;
  padding: 14px 30px;
  background: linear-gradient(135deg, #4A90E2, #5e72e4);
  color: white;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.chat-input button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

.chat-input button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.herb-card {
  margin-top: 10px;
  background: #ffffff;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  padding: 15px;
  display: flex;
  align-items: flex-start;
  gap: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.herb-image-container {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.herb-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  color: #6c757d;
}

.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.placeholder-content span {
  margin-top: 8px;
  font-size: 0.75rem;
  line-height: 1.2;
}

.herb-details {
  flex: 1;
}

.herb-details p {
  margin: 4px 0;
  font-size: 0.95rem;
}

.herb-details a {
  color: #4A90E2;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .chat-container {
    height: 100vh;
    border-radius: 0;
  }

  .chat-header {
    padding: 15px;
  }

  .chat-header h2 {
    font-size: 1.5rem;
  }

  .chat-messages {
    padding: 15px;
  }

  .message-content {
    max-width: 95%;
  }

  .text {
    max-width: 80%;
    margin: 0 10px;
  }

  .text p {
    padding: 12px 16px;
    font-size: 0.9rem;
  }

  .chat-input {
    padding: 15px;
  }

  .chat-input input {
    padding: 12px 16px;
  }

  .chat-input button {
    padding: 12px 24px;
  }

  .herb-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .herb-image-container {
    width: 120px;
    height: 120px;
  }
}
</style>