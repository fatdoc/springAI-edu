import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Config from '../views/Config.vue'
import Chat from '../views/Chat.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/config',
      name: 'Config',
      component: Config
    },
    {
      path: '/chat',
      name: 'Chat',
      component: Chat
    },
    {
      path: '/damai-ai',
      name: 'DaMaiAI',
      component: () => import('../views/DaMaiAi.vue')
    },
    {
      path: '/damai-rag',
      name: 'SmartRag',
      component: () => import('../views/SmartRag.vue')
    },
    {
      path: '/upload-kb',
      name: 'UploadKb',
      component: () => import('@/views/UploadKb.vue')
    },
    {
      path: '/private-rag',
      name: 'PrivateRag',
      component: () => import('@/views/PrivateRag.vue')
    },
    {
      path: '/functionCalling',
      name: 'FunctionCalling',
      component: () => import('@/views/Functioncalling.vue')
    },
    {
      path: '/forkVoice',
      name: 'ForkVoice',
      component: () => import('@/views/fork.vue')
    }
  ],
})

export default router
