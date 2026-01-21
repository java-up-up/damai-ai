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
    path: '/damai-analysis',
    name: 'DaMaiAnalysis',
    component: () => import('../views/DaMaiAnalysis.vue')
  },
  {
    path: '/ai-observability',
    name: 'AiObservability',
    component: () => import('../views/AiObservability.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 