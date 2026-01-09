<template>
  <div class="home" :class="{ 'dark': isDark }">
    <div class="container">
      <h1 class="title">具体 AI 应用</h1>
      <div class="cards-grid">
        <router-link 
          v-for="app in aiApps" 
          :key="app.id"
          :to="app.route"
          class="card"
        >
          <div class="card-content">
            <component :is="app.icon" class="icon" />
            <h2>{{ app.title }}</h2>
            <p>{{ app.description }}</p>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useDark } from '@vueuse/core'
import { 
  ChatBubbleLeftRightIcon,
  HeartIcon,
  UserGroupIcon,
  DocumentTextIcon
} from '@heroicons/vue/24/outline'
import DamaiAssistantIcon from '../components/icons/DamaiAssistantIcon.vue'
import DamaiAssistantRobotIcon from '../components/icons/DamaiAssistantRobotIcon.vue'
import DamaiAnalysisIcon from '../components/icons/DamaiAnalysisIcon.vue'

const isDark = useDark()

const aiApps = ref([
  {
    id: 1,
    title: '大麦贴心助手',
    description: '帮你解决大麦业务相关的问题',
    route: '/damai-ai',
    icon: DamaiAssistantRobotIcon
  },
  {
    id: 2,
    title: '大麦规则助手',
    description: '帮你解决大麦规则相关的问题',
    route: '/damai-rag',
    icon: DamaiAssistantIcon
  },
  {
    id: 3,
    title: '大麦运维助手',
    description: '日志查询、链路追踪、系统监控分析',
    route: '/damai-analysis',
    icon: DamaiAnalysisIcon
  }
])
</script>

<style scoped lang="scss">
.home {
  min-height: 100vh;
  padding: 2rem;
  background: var(--bg-color);
  transition: background-color 0.3s;

  .container {
    max-width: 1600px;
    margin: 0 auto;
    padding: 0 2rem;
  }

  .title {
    text-align: center;
    font-size: 2.5rem;
    margin-bottom: 3rem;
    background: linear-gradient(45deg, rgba(255, 55, 29, 0.85), #ff8f29);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: titleAppear 1s ease-out;
    position: relative;
    font-weight: 600;
    
    &::after {
      content: '';
      position: absolute;
      bottom: -10px;
      left: 50%;
      transform: translateX(-50%);
      width: 60px;
      height: 3px;
      background: linear-gradient(90deg, transparent, rgba(255, 55, 29, 0.5), transparent);
      animation: lineAppear 1s ease-out 0.5s forwards;
      opacity: 0;
    }
  }

  .cards-grid {
    display: grid;
    grid-template-columns: repeat(1, 1fr);
    gap: 2.5rem;
    justify-items: center;
    padding: 1rem;
    max-width: 400px;
    margin: 0 auto;
    perspective: 1000px;

    @media (min-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
      max-width: 700px;
      gap: 2rem;
    }

    @media (min-width: 1200px) {
      grid-template-columns: repeat(3, 1fr);
      max-width: 1100px;
      gap: 3rem;
    }
  }

  .card {
    position: relative;
    width: 100%;
    max-width: 320px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 24px;
    padding: 2.5rem;
    text-decoration: none;
    color: inherit;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    border: 1px solid rgba(255, 255, 255, 0.2);
    overflow: hidden;
    animation: cardAppear 0.6s ease-out forwards;
    opacity: 0;
    transform: translateY(20px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    transform-style: preserve-3d;

    @for $i from 1 through 3 {
      &:nth-child(#{$i}) {
        animation-delay: #{$i * 0.15}s;
      }
    }

    .dark & {
      background: rgba(255, 255, 255, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    }

    &:hover {
      transform: translateY(-10px) rotateX(8deg) rotateY(8deg);
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
      
      .dark & {
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
      }

      .icon {
        transform: scale(1.15) translateZ(20px) rotate(5deg);
      }

      h2 {
        transform: translateZ(15px) scale(1.05);
        color: rgba(255, 55, 29, 0.9);
        letter-spacing: 0.5px;
      }

      p {
        transform: translateZ(10px) scale(1.02);
        color: #333;
        
        .dark & {
          color: #bbb;
        }
      }
    }

    .card-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      transform-style: preserve-3d;
      transition: transform 0.4s ease;
    }

    .icon {
      width: 56px;
      height: 56px;
      margin-bottom: 1.5rem;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      transform-style: preserve-3d;

      &.heart-icon {
        animation: pulse 2s ease-in-out infinite;
      }
    }

    h2 {
      font-size: 1.5rem;
      margin-bottom: 1rem;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      font-weight: 600;
      transform-style: preserve-3d;
      letter-spacing: 0;
    }

    p {
      color: #666;
      font-size: 1rem;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      line-height: 1.6;
      font-weight: 400;
      transform-style: preserve-3d;

      .dark & {
        color: #999;
      }
    }
  }

  &.dark {
    background: #1a1a1a;
    
    .card {
      background: rgba(255, 255, 255, 0.05);
      
      p {
        color: #999;
      }
    }
  }
}

@keyframes titleAppear {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes lineAppear {
  from {
    opacity: 0;
    width: 0;
  }
  to {
    opacity: 1;
    width: 60px;
  }
}

@keyframes cardAppear {
  from {
    opacity: 0;
    transform: translateY(30px) rotateX(-10deg);
  }
  to {
    opacity: 1;
    transform: translateY(0) rotateX(0);
  }
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

@media (max-width: 768px) {
  .home {
    padding: 1rem;
    
    .container {
      padding: 0 1rem;
    }
    
    .title {
      font-size: 2rem;
    }

    .card {
      max-width: 100%;
    }
  }
}
</style> 