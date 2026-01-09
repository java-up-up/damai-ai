<template>
  <svg xmlns="http://www.w3.org/2000/svg" :width="size" :height="size" viewBox="0 0 64 64" fill="none">
    <!-- 监控屏幕 -->
    <rect x="8" y="8" width="48" height="36" rx="4" fill="#E8F5E9" stroke="#4CAF50" stroke-width="2"/>
    
    <!-- 屏幕内容 - 波形图 -->
    <path d="M14 30 L20 24 L26 32 L32 18 L38 28 L44 22 L50 26" 
          stroke="#4CAF50" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
    
    <!-- 数据点 -->
    <circle cx="20" cy="24" r="2" fill="#4CAF50"/>
    <circle cx="32" cy="18" r="2" fill="#FF5722"/>
    <circle cx="44" cy="22" r="2" fill="#4CAF50"/>
    
    <!-- 状态指示灯 -->
    <circle cx="16" cy="14" r="2" fill="#4CAF50"/>
    <circle cx="22" cy="14" r="2" fill="#FFC107"/>
    <circle cx="28" cy="14" r="2" fill="#F44336"/>
    
    <!-- 底座 -->
    <rect x="24" y="44" width="16" height="4" fill="#78909C"/>
    <rect x="20" y="48" width="24" height="4" rx="2" fill="#546E7A"/>
    
    <!-- 放大镜 - 代表分析 -->
    <circle cx="50" cy="50" r="8" fill="#FFF3E0" stroke="#FF9800" stroke-width="2"/>
    <line x1="55" y1="55" x2="62" y2="62" stroke="#FF9800" stroke-width="3" stroke-linecap="round"/>
    
    <!-- 放大镜内的图表 -->
    <path d="M46 52 L48 48 L50 51 L52 46 L54 50" stroke="#FF9800" stroke-width="1.5" fill="none" stroke-linecap="round"/>
  </svg>
</template>

<script setup>
defineProps({
  size: {
    type: Number,
    default: 64
  }
})
</script>
