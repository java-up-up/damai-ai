<template>
  <div class="observability-container">
    <h1 class="page-title">AI 可观测性监控</h1>
    
    <!-- 今日概览 -->
    <div class="stats-overview">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <div class="stat-value">{{ todayStats.totalCalls || 0 }}</div>
          <div class="stat-label">今日调用次数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🎯</div>
        <div class="stat-content">
          <div class="stat-value">{{ todayStats.totalTokens || 0 }}</div>
          <div class="stat-label">今日Token消耗</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-content">
          <div class="stat-value">¥{{ formatCost(todayStats.totalCost) }}</div>
          <div class="stat-label">今日预估费用</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⬆️</div>
        <div class="stat-content">
          <div class="stat-value">{{ todayStats.totalPromptTokens || 0 }}</div>
          <div class="stat-label">输入Token</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⬇️</div>
        <div class="stat-content">
          <div class="stat-value">{{ todayStats.totalCompletionTokens || 0 }}</div>
          <div class="stat-label">输出Token</div>
        </div>
      </div>
    </div>

    <!-- 按类型统计 -->
    <div class="section">
      <h2 class="section-title">按场景统计（今日）</h2>
      <div class="type-stats">
        <div v-for="stat in typeStats" :key="stat.requestType" class="type-card">
          <div class="type-name">{{ getTypeName(stat.requestType) }}</div>
          <div class="type-detail">
            <span>调用: {{ stat.calls }}次</span>
            <span>Token: {{ stat.totalTokens }}</span>
            <span>费用: ¥{{ formatCost(stat.totalCost) }}</span>
          </div>
        </div>
        <div v-if="typeStats.length === 0" class="empty-tip">暂无数据</div>
      </div>
    </div>

    <!-- 最近调用记录 -->
    <div class="section">
      <h2 class="section-title">
        最近调用记录
        <span class="title-tip">点击行查看会话统计</span>
        <button class="refresh-btn" @click="loadData">🔄 刷新</button>
      </h2>
      <div class="traces-table">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>会话ID</th>
              <th>场景</th>
              <th>模型</th>
              <th>输入Token</th>
              <th>输出Token</th>
              <th>延迟</th>
              <th>费用</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="trace in traces" :key="trace.id" @click="showConversationStats(trace.conversationId)" class="clickable-row">
              <td>{{ formatTime(trace.createTime) }}</td>
              <td class="conversation-id">{{ truncateId(trace.conversationId) }}</td>
              <td>
                <span class="type-badge" :class="getTypeClass(trace.requestType)">
                  {{ getTypeName(trace.requestType) }}
                </span>
              </td>
              <td>{{ trace.modelName }}</td>
              <td>{{ trace.promptTokens || 0 }}</td>
              <td>{{ trace.completionTokens || 0 }}</td>
              <td>{{ trace.latencyMs }}ms</td>
              <td>¥{{ formatCost(trace.estimatedCost) }}</td>
              <td>
                <span :class="trace.success ? 'status-success' : 'status-fail'">
                  {{ trace.success ? '成功' : '失败' }}
                </span>
              </td>
            </tr>
            <tr v-if="traces.length === 0">
              <td colspan="9" class="empty-row">暂无调用记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 会话统计弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>会话统计详情</h3>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="conversation-id-full">
            会话ID: {{ currentConversationId }}
          </div>
          <div v-if="conversationStats" class="stats-grid">
            <div class="stats-item">
              <div class="stats-label">调用次数</div>
              <div class="stats-value">{{ conversationStats.totalCalls }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">输入Token</div>
              <div class="stats-value">{{ conversationStats.totalPromptTokens }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">输出Token</div>
              <div class="stats-value">{{ conversationStats.totalCompletionTokens }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">总Token</div>
              <div class="stats-value">{{ conversationStats.totalTokens }}</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">总延迟</div>
              <div class="stats-value">{{ conversationStats.totalLatencyMs }}ms</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">平均延迟</div>
              <div class="stats-value">{{ conversationStats.avgLatencyMs }}ms</div>
            </div>
            <div class="stats-item">
              <div class="stats-label">成功率</div>
              <div class="stats-value">{{ conversationStats.successRate?.toFixed(1) }}%</div>
            </div>
            <div class="stats-item highlight">
              <div class="stats-label">预估总费用</div>
              <div class="stats-value">¥{{ formatCost(conversationStats.totalCost) }}</div>
            </div>
          </div>
          <div v-else class="loading">加载中...</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { observabilityAPI } from '../api/api'

const todayStats = ref({})
const typeStats = ref([])
const traces = ref([])
const showModal = ref(false)
const currentConversationId = ref('')
const conversationStats = ref(null)

const loadData = async () => {
  try {
    const [stats, types, traceList] = await Promise.all([
      observabilityAPI.getTodayStats(),
      observabilityAPI.getStatsByType(),
      observabilityAPI.getRecentTraces(50)
    ])
    todayStats.value = stats || {}
    typeStats.value = types || []
    traces.value = traceList || []
  } catch (error) {
    console.error('Load data error:', error)
  }
}

const showConversationStats = async (conversationId) => {
  if (!conversationId) return
  currentConversationId.value = conversationId
  conversationStats.value = null
  showModal.value = true
  
  try {
    const stats = await observabilityAPI.getConversationStats(conversationId)
    conversationStats.value = stats
  } catch (error) {
    console.error('Load conversation stats error:', error)
  }
}

const closeModal = () => {
  showModal.value = false
  conversationStats.value = null
}

const truncateId = (id) => {
  if (!id) return '-'
  return id.length > 12 ? id.substring(0, 12) + '...' : id
}

const formatCost = (cost) => {
  if (!cost) return '0.000000'
  return Number(cost).toFixed(6)
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getTypeName = (type) => {
  const typeMap = {
    '贴心助手': '贴心助手',
    '运维助手': '运维助手',
    '规则助手': '规则助手',
    'ASSISTANT': '贴心助手',
    'ANALYSIS': '运维助手',
    'MARKDOWN': '规则助手',
    'RAG': '规则助手',
    'CHAT': '普通对话'
  }
  return typeMap[type] || type
}

const getTypeClass = (type) => {
  const classMap = {
    '贴心助手': 'type-assistant',
    '运维助手': 'type-analysis',
    '规则助手': 'type-markdown',
    'ASSISTANT': 'type-assistant',
    'ANALYSIS': 'type-analysis',
    'MARKDOWN': 'type-markdown'
  }
  return classMap[type] || 'type-default'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.observability-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 24px;
  text-align: center;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #eee;
}

.stat-icon {
  font-size: 32px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.refresh-btn {
  background: #1890ff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.refresh-btn:hover {
  background: #40a9ff;
}

.type-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.type-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #1890ff;
}

.type-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.type-detail {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #666;
}

.traces-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

tr:hover {
  background: #f8f9fa;
}

.type-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-assistant {
  background: #e3f2fd;
  color: #1976d2;
}

.type-analysis {
  background: #fff3e0;
  color: #f57c00;
}

.type-markdown {
  background: #e8f5e9;
  color: #388e3c;
}

.type-default {
  background: #f5f5f5;
  color: #666;
}

.status-success {
  color: #4caf50;
  font-weight: 500;
}

.status-fail {
  color: #f44336;
  font-weight: 500;
}

.empty-row, .empty-tip {
  text-align: center;
  color: #999;
  padding: 24px;
}

.title-tip {
  font-size: 12px;
  color: #999;
  font-weight: normal;
  margin-left: 12px;
}

.clickable-row {
  cursor: pointer;
  transition: background 0.2s;
}

.clickable-row:hover {
  background: #e3f2fd !important;
}

.conversation-id {
  font-family: monospace;
  color: #1890ff;
  font-size: 12px;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.conversation-id-full {
  font-family: monospace;
  font-size: 12px;
  color: #666;
  background: #f8f9fa;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 16px;
  word-break: break-all;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stats-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}

.stats-item.highlight {
  background: #1890ff;
  color: white;
}

.stats-item.highlight .stats-label {
  color: rgba(255, 255, 255, 0.9);
}

.stats-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.stats-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.stats-item.highlight .stats-value {
  color: white;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .type-stats {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
