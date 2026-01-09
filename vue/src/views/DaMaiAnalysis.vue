<template>
  <div class="analysis-service" :class="{ 'dark': isDark }">
    <div class="chat-container">
      <div class="sidebar">
        <div class="history-header">
          <h2>聊天记录</h2>
          <button class="new-chat" @click="startNewChat">
            <PlusIcon class="icon" :size="24" />
            新的聊天
          </button>
        </div>
        <div class="history-list">
          <div 
            v-for="chat in chatHistory" 
            :key="`${chat.id}-${chat.title}`"
            class="history-item"
            :class="{ 'active': currentChatId === chat.id }"
            @click="loadChat(chat.id)"
          >
            <ChatBubbleIcon class="icon" :size="24" />
            <span class="title">{{ chat.title || '新的对话' }}</span>
            <button 
              class="delete-btn" 
              @click.stop="deleteChat(chat.id)"
              title="删除对话"
            >
              <TrashIcon class="icon" :size="20" />
            </button>
          </div>
        </div>
      </div>
      
      <div class="chat-main">
        <div class="service-header">
          <div class="service-info">
            <DamaiAnalysisIcon class="avatar" :size="48" />
            <div class="info">
              <h3>麦小维</h3>
              <p>大麦系统运维分析助手</p>
            </div>
          </div>
        </div>

        <div class="messages" ref="messagesRef">
          <Chat
            v-for="(message, index) in currentMessages"
            :key="index"
            :message="message"
            :is-stream="isStreaming && index === currentMessages.length - 1"
          />
        </div>
        
        <div class="input-area">
          <textarea
            v-model="userInput"
            @keydown.enter.prevent="sendMessage()"
            placeholder="请描述您需要查询的日志、分析的问题，或需要的监控指标..."
            rows="1"
            ref="inputRef"
          ></textarea>
          <button 
            class="send-button" 
            @click="sendMessage()"
            :disabled="isStreaming || !userInput.trim()"
          >
            <SendIcon class="icon" :size="40" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {nextTick, onMounted, ref, triggerRef} from 'vue'
import {useDark} from '@vueuse/core'
import {marked} from 'marked'
import DOMPurify from 'dompurify'
import Chat from '../components/Chat.vue'
import {chatAPI} from '../api/api'
import DamaiAnalysisIcon from '../components/icons/DamaiAnalysisIcon.vue'
import ChatBubbleIcon from '../components/icons/ChatBubbleIcon.vue'
import PlusIcon from '../components/icons/PlusIcon.vue'
import SendIcon from '../components/icons/SendIcon.vue'
import TrashIcon from '../components/icons/TrashIcon.vue'

const isDark = useDark()
const messagesRef = ref(null)
const inputRef = ref(null)
const userInput = ref('')
const isStreaming = ref(false)
const currentChatId = ref(null)
const currentMessages = ref([])
const chatHistory = ref([])
const titleUpdateTimer = ref(null)

// ChatType.ANALYSIS 的 code 值为 4
const CHAT_TYPE = 4

// 配置 marked
marked.setOptions({
  breaks: true,
  gfm: true,
  sanitize: false
})

// 自动调整输入框高度
const adjustTextareaHeight = () => {
  const textarea = inputRef.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = textarea.scrollHeight + 'px'
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 发送消息
const sendMessage = async (content) => {
  if (isStreaming.value || (!content && !userInput.value.trim())) return
  
  const messageContent = content || userInput.value.trim()
  
  // 添加用户消息
  const userMessage = {
    role: 'user',
    content: messageContent,
    timestamp: new Date()
  }
  currentMessages.value.push(userMessage)
  
  if (!content) {
    userInput.value = ''
    adjustTextareaHeight()
  }
  await scrollToBottom()
  
  // 添加助手消息占位
  const assistantMessage = {
    role: 'assistant',
    content: '',
    timestamp: new Date(),
    isMarkdown: true
  }
  currentMessages.value.push(assistantMessage)
  isStreaming.value = true
  
  let totalContent = ''
  
  try {
    // 调用 MCP 分析接口
    const reader = await chatAPI.sendAnalysisMessage(messageContent, currentChatId.value)
    const decoder = new TextDecoder('utf-8')
    
    while (true) {
      try {
        const { value, done } = await reader.read()
        if (done) break
        
        totalContent += decoder.decode(value)
        
        await nextTick(() => {
          const updatedMessage = {
            ...assistantMessage,
            content: totalContent,
            isMarkdown: true
          }
          const lastIndex = currentMessages.value.length - 1
          currentMessages.value.splice(lastIndex, 1, updatedMessage)
        })
        await scrollToBottom()
      } catch (readError) {
        console.error('读取流错误:', readError)
        break
      }
    }

    checkAndUpdateChatTitles()

  } catch (error) {
    console.error('发送消息失败:', error)
    assistantMessage.content = '抱歉，发生了错误，请稍后重试。'
  } finally {
    isStreaming.value = false
    await scrollToBottom()
  }
}

// 加载特定对话
const loadChat = async (chatId) => {
  currentChatId.value = chatId
  try {
    const messages = await chatAPI.chatHistoryMessageList(chatId, CHAT_TYPE)
    currentMessages.value = messages.map(msg => ({
      ...msg,
      isMarkdown: msg.role === 'assistant'
    }))
  } catch (error) {
    console.error('加载对话消息失败:', error)
    currentMessages.value = []
  }
}

// 加载聊天历史
const loadChatHistory = async () => {
  try {
    const history = await chatAPI.chatTypeHistoryList(CHAT_TYPE)
    chatHistory.value = history || []
    if (history && history.length > 0) {
      await loadChat(history[0].id)
    } else {
      await startNewChat()
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
    chatHistory.value = []
    await startNewChat()
  }
}

// 开始新对话
const startNewChat = async () => {
  const newChatId = Date.now().toString()
  currentChatId.value = newChatId
  currentMessages.value = []
  
  const newChat = {
    id: newChatId,
    title: '新的对话'
  }
  chatHistory.value = [newChat, ...chatHistory.value]
}

// 删除对话
const deleteChat = async (chatId) => {
  if (!confirm('确定要删除这个对话吗？')) {
    return
  }
  
  try {
    await chatAPI.deleteChat(chatId, CHAT_TYPE)
    chatHistory.value = chatHistory.value.filter(chat => chat.id !== chatId)
    
    if (currentChatId.value === chatId) {
      await startNewChat()
    }
  } catch (error) {
    console.error('删除对话失败:', error)
    alert('删除对话失败，请稍后重试')
  }
}

// 检查并更新聊天标题
const checkAndUpdateChatTitles = async () => {
  try {
    const hasNewChatTitle = chatHistory.value.some(chat => 
      chat.title === '新的对话' || chat.title === '新的聊天'
    )
    
    if (!hasNewChatTitle) {
      return
    }
    
    const chatListData = await chatAPI.chatTypeHistoryList(CHAT_TYPE)
    
    if (!chatListData || (Array.isArray(chatListData) && chatListData.length === 0)) {
      return
    }
    
    if (Array.isArray(chatListData)) {
      let hasUpdated = false
      
      for (let i = 0; i < chatHistory.value.length; i++) {
        const chat = chatHistory.value[i]
        const matchedChat = chatListData.find(apiChat => apiChat.id === chat.id)
        
        if (matchedChat && matchedChat.title && matchedChat.title.trim()) {
          chatHistory.value[i] = { 
            ...chat, 
            title: matchedChat.title 
          }
          hasUpdated = true
        }
      }
      
      if (hasUpdated) {
        triggerRef(chatHistory)
        await nextTick()
      }
    }
  } catch (error) {
    console.error('检查并更新聊天标题失败:', error)
  }
}

onMounted(() => {
  loadChatHistory()
  adjustTextareaHeight()
  checkAndUpdateChatTitles()
})
</script>

<style scoped lang="scss">
.analysis-service {
  position: fixed;
  inset: 64px 0 0 0;
  display: flex;
  background: var(--bg-color);
  overflow: hidden;

  .chat-container {
    flex: 1;
    display: flex;
    max-width: 1800px;
    width: 100%;
    margin: 0 auto;
    padding: 1.5rem 2rem;
    gap: 1.5rem;
    height: 100%;
    overflow: hidden;
  }

  .sidebar {
    width: 300px;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 1rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
    transition: transform 0.3s ease;
    
    .history-header {
      flex-shrink: 0;
      padding: 1rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      
      h2 {
        font-size: 1.25rem;
        font-weight: 600;
      }
      
      .new-chat {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.5rem 1rem;
        border-radius: 0.5rem;
        background: #4CAF50;
        color: white;
        border: none;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          background: #388E3C;
          transform: translateY(-1px);
        }
        
        &:active {
          transform: translateY(0);
        }
      }
    }
    
    .history-list {
      flex: 1;
      overflow-y: auto;
      padding: 0.5rem;
      scrollbar-width: thin;
      scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
      
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: transparent;
      }
      
      &::-webkit-scrollbar-thumb {
        background-color: rgba(0, 0, 0, 0.2);
        border-radius: 3px;
      }
      
      .history-item {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.75rem;
        border-radius: 0.5rem;
        cursor: pointer;
        transition: all 0.3s ease;
        margin-bottom: 0.25rem;
        
        &:hover {
          background: rgba(76, 175, 80, 0.1);
          transform: translateX(2px);
          
          .delete-btn {
            opacity: 1;
          }
        }
        
        &.active {
          background: rgba(76, 175, 80, 0.15);
          font-weight: 500;
        }
        
        .title {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          font-size: 0.95rem;
        }

        .delete-btn {
          opacity: 0;
          background: none;
          border: none;
          padding: 0.25rem;
          cursor: pointer;
          color: #666;
          transition: all 0.3s ease;
          border-radius: 0.25rem;
          
          &:hover {
            color: #ff4d4f;
            background: rgba(255, 77, 79, 0.1);
          }
        }
      }
    }
  }

  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 1rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
    overflow: hidden;
    transition: all 0.3s ease;

    .service-header {
      flex-shrink: 0;
      padding: 1rem 2rem;
      border-bottom: 1px solid rgba(0, 0, 0, 0.05);
      background: rgba(255, 255, 255, 0.98);
      transition: all 0.3s ease;

      .service-info {
        display: flex;
        align-items: center;
        gap: 1rem;

        .info {
          h3 {
            font-size: 1.25rem;
            margin-bottom: 0.25rem;
            font-weight: 600;
            color: #4CAF50;
          }

          p {
            font-size: 0.875rem;
            color: #666;
          }
        }
      }
    }
    
    .messages {
      flex: 1;
      overflow-y: auto;
      padding: 2rem;
      scrollbar-width: thin;
      scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
      
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: transparent;
      }
      
      &::-webkit-scrollbar-thumb {
        background-color: rgba(0, 0, 0, 0.2);
        border-radius: 3px;
      }
    }
    
    .input-area {
      flex-shrink: 0;
      padding: 1.5rem 2rem;
      background: rgba(255, 255, 255, 0.98);
      border-top: 1px solid rgba(0, 0, 0, 0.05);
      display: flex;
      gap: 1rem;
      align-items: flex-end;
      transition: all 0.3s ease;
      
      textarea {
        flex: 1;
        resize: none;
        border: 1px solid rgba(76, 175, 80, 0.3);
        background: white;
        border-radius: 0.75rem;
        padding: 1rem;
        color: inherit;
        font-family: inherit;
        font-size: 1rem;
        line-height: 1.5;
        max-height: 150px;
        transition: all 0.3s ease;
        
        &:focus {
          outline: none;
          border-color: #4CAF50;
          box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
        }
      }
      
      .send-button {
        background: #E8F5E9;
        color: #4CAF50;
        border: 1px solid #C8E6C9;
        border-radius: 0.75rem;
        width: 3.5rem;
        height: 3.5rem;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s ease;
        padding: 0;
        
        &:hover:not(:disabled) {
          background: #C8E6C9;
          transform: scale(1.05);
        }
        
        &:active:not(:disabled) {
          transform: scale(0.95);
        }
        
        &:disabled {
          background: #f5f5f5;
          border-color: #e0e0e0;
          cursor: not-allowed;
          opacity: 0.6;
        }
      }
    }
  }
}

.dark {
  .sidebar {
    background: rgba(40, 40, 40, 0.95);
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
    
    .history-header {
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    }
    
    .history-list {
      scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
      
      &::-webkit-scrollbar-thumb {
        background-color: rgba(255, 255, 255, 0.2);
      }
      
      .history-item {
        &:hover {
          background: rgba(76, 175, 80, 0.15);
        }
        
        &.active {
          background: rgba(76, 175, 80, 0.2);
        }
      }
    }
  }
  
  .chat-main {
    background: rgba(40, 40, 40, 0.95);
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
    
    .service-header {
      background: rgba(30, 30, 30, 0.98);
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);

      .info p {
        color: #999;
      }
    }

    .messages {
      scrollbar-color: rgba(255, 255, 255, 0.2) transparent;
      
      &::-webkit-scrollbar-thumb {
        background-color: rgba(255, 255, 255, 0.2);
      }
    }

    .input-area {
      background: rgba(30, 30, 30, 0.98);
      border-top: 1px solid rgba(255, 255, 255, 0.05);
      
      textarea {
        background: rgba(50, 50, 50, 0.95);
        border-color: rgba(76, 175, 80, 0.3);
        color: white;
        
        &:focus {
          border-color: #4CAF50;
          box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
        }
      }
      
      .send-button {
        background: rgba(76, 175, 80, 0.2);
        border-color: rgba(76, 175, 80, 0.3);
      }
    }
  }
}

@media (max-width: 768px) {
  .analysis-service {
    .chat-container {
      padding: 0;
    }
    
    .sidebar {
      position: fixed;
      left: -300px;
      top: 64px;
      bottom: 0;
      z-index: 100;
      
      &.show {
        transform: translateX(300px);
      }
    }
    
    .chat-main {
      border-radius: 0;
      
      .service-header {
        padding: 1rem;
      }
      
      .messages {
        padding: 1rem;
      }
      
      .input-area {
        padding: 1rem;
      }
    }
  }
}
</style>
