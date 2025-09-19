import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
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
    component: () => import('../views/UploadKb.vue')
  },
  {
    path: '/private-rag',
    name: 'PrivateRag',
    component: () => import('../views/PrivateRag.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 