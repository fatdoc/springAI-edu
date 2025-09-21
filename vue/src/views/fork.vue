<template>
  <div class="upload-container">
    <h2>声音复刻训练</h2>

    <input type="file" accept=".mp3,.wav,.flac" @change="handleFileChange" />
    <button :disabled="!selectedFile || loading" @click="uploadAudio">
      {{ loading ? "上传中..." : "上传训练音频" }}
    </button>

    <div v-if="responseData" class="response">
      <h3>响应内容</h3>
      <pre>{{ JSON.stringify(responseData, null, 2) }}</pre>
    </div>

    <div v-if="errorMsg" class="error">
      <h3>错误信息</h3>
      <p>{{ errorMsg }}</p>
    </div>

    <!-- 添加状态查询功能 -->
    <div class="status-section">
      <h3>训练状态查询</h3>
      <button :disabled="loading" @click="getTrainingStatus">
        {{ loading ? "查询中..." : "查询训练状态" }}
      </button>

      <div v-if="statusData" class="response">
        <h4>状态信息</h4>
        <pre>{{ JSON.stringify(statusData, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

// 固定配置项（你需要替换）
const APP_ID = '5401484939'
const ACCESS_TOKEN = 'HfZ61-mR05QDMg50V1foczoubgz9mFyY'
const SPEAKER_ID = 'S_j9LdZJcA1'
// 使用代理，开发环境使用相对路径
const HOST = ''

const selectedFile = ref(null)
const responseData = ref(null)
const statusData = ref(null)
const errorMsg = ref(null)
const loading = ref(false)

// 获取文件扩展名作为音频格式（与Python代码一致）
const getAudioFormat = (filename) => {
  const extension = filename.split('.').pop().toLowerCase()
  return extension
}

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    const allowedFormats = ['mp3', 'wav', 'flac']
    const format = getAudioFormat(file.name)

    if (allowedFormats.includes(format)) {
      selectedFile.value = file
      errorMsg.value = null
    } else {
      errorMsg.value = '请上传 MP3、WAV 或 FLAC 格式的音频文件'
      selectedFile.value = null
    }
  }
}

const uploadAudio = async () => {
  if (!selectedFile.value) return

  loading.value = true
  responseData.value = null
  errorMsg.value = null

  try {
    const reader = new FileReader()
    reader.onload = async () => {
      try {
        // 获取base64编码的音频数据（去除data:audio/xxx;base64,前缀）
        const base64Audio = reader.result.split(',')[1]
        const audioFormat = getAudioFormat(selectedFile.value.name)

        // 构建请求体（与Python代码完全一致）
        const payload = {
          appid: APP_ID,
          speaker_id: SPEAKER_ID,
          audios: [
            {
              audio_bytes: base64Audio,
              audio_format: audioFormat
            }
          ],
          source: 2,
          language: 0,
          model_type: 1
        }

        // 构建请求头（与Python代码完全一致）
        const headers = {
          'Content-Type': 'application/json',
          'Authorization': `Bearer;${ACCESS_TOKEN}`,
          'Resource-Id': 'volc.megatts.voiceclone'
        }

        console.log('发送请求:', {
          url: `${HOST}/api/v1/mega_tts/audio/upload`,
          headers,
          payload: { ...payload, audios: [{ ...payload.audios[0], audio_bytes: 'base64_data...' }] }
        })

        const response = await axios.post(
            `${HOST}/api/v1/mega_tts/audio/upload`,
            payload,
            { headers }
        )

        console.log('status code =', response.status)
        console.log('headers =', response.headers)
        console.log('response:', response.data)

        responseData.value = response.data
      } catch (error) {
        console.error('上传错误:', error)
        if (error.response) {
          errorMsg.value = `请求错误 (${error.response.status}): ${JSON.stringify(error.response.data)}`
        } else {
          errorMsg.value = `网络错误: ${error.message}`
        }
      } finally {
        loading.value = false
      }
    }

    reader.onerror = () => {
      errorMsg.value = '文件读取失败'
      loading.value = false
    }

    reader.readAsDataURL(selectedFile.value)
  } catch (error) {
    console.error('处理文件错误:', error)
    errorMsg.value = `处理文件错误: ${error.message}`
    loading.value = false
  }
}

// 查询训练状态（与Python代码一致）
const getTrainingStatus = async () => {
  loading.value = true
  statusData.value = null
  errorMsg.value = null

  try {
    const headers = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer;${ACCESS_TOKEN}`,
      'Resource-Id': 'volc.megatts.voiceclone'
    }

    const body = {
      appid: APP_ID,
      speaker_id: SPEAKER_ID
    }

    console.log('查询状态:', { url: `${HOST}/api/v1/mega_tts/status`, headers, body })

    const response = await axios.post(
        `${HOST}/api/v1/mega_tts/status`,
        body,
        { headers }
    )

    console.log('状态查询响应:', response.data)
    statusData.value = response.data
  } catch (error) {
    console.error('状态查询错误:', error)
    if (error.response) {
      errorMsg.value = `状态查询错误 (${error.response.status}): ${JSON.stringify(error.response.data)}`
    } else {
      errorMsg.value = `网络错误: ${error.message}`
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.upload-container {
  padding: 20px;
  max-width: 600px;
  margin: auto;
}

h2, h3, h4 {
  color: #333;
}

input[type="file"] {
  margin-bottom: 10px;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 100%;
}

button {
  background-color: #007bff;
  color: white;
  padding: 10px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
  margin-bottom: 10px;
}

button:hover:not(:disabled) {
  background-color: #0056b3;
}

button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.response, .error {
  margin-top: 20px;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.response {
  background: #f8f9fa;
  border-color: #28a745;
}

.error {
  color: #721c24;
  background: #f8d7da;
  border-color: #f5c6cb;
}

.status-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ddd;
}

pre {
  background: #f4f4f4;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  line-height: 1.4;
}
</style>