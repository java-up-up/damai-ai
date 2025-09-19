<template>
  <div class="private-rag" :class="{ 'dark': isDark }">
    <div class="chat-container">
      <div class="sidebar">
        <div class="history-header">
          <h2>对话历史</h2>
          <button class="new-chat" @click="initiateNewConversation">
            <PlusIcon class="icon" :size="24" />
            开始新对话
          </button>
        </div>
        <div class="history-list">
          <div
              v-for="chat in conversationHistory"
              :key="chat.id"
              class="history-item"
              :class="{ 'active': activeConversationId === chat.id }"
              @click="switchConversation(chat.id)"
          >
            <ChatBubbleIcon class="icon" :size="24" />
            <span class="title">{{ chat.title || '新对话' }}</span>
            <button
                class="delete-btn"
                @click.stop="removeConversation(chat.id)"
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
            <LaptopIcon class="avatar" :size="48" />
            <div class="info">
              <h3>私人知识库</h3>
              <p>基于您的知识库智能问答</p>
            </div>
          </div>

          <!-- 知识库选择器 -->
          <div class="kb-selector">
            <label for="kb-select">知识库：</label>
            <select id="kb-select" v-model="selectedKbId" @change="onKbChange">
              <option value="">选择知识库</option>
              <option v-for="kb in knowledgeBases" :key="kb.id" :value="kb.id">
                {{ kb.name }}（{{ kb.remark }}）
              </option>
            </select>
          </div>
        </div>

        <div class="messages" ref="messagesContainer">
          <Chat
              v-for="(message, index) in activeMessages"
              :key="index"
              :message="message"
              :is-stream="isProcessing && index === activeMessages.length - 1"
          />
        </div>

        <div class="input-area">
          <textarea
              v-model="userQuery"
              @keydown.enter.prevent="submitMessage()"
              placeholder="请选择知识库后输入您的问题..."
              rows="1"
              ref="queryInput"
              :disabled="!selectedKbId"
          ></textarea>
          <button
              class="send-button"
              @click="submitMessage()"
              :disabled="isProcessing || !userQuery.trim() || !selectedKbId"
          >
            <SendIcon class="icon" :size="40" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, triggerRef, watch } from 'vue'
import { useDark } from '@vueuse/core'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import Chat from '../components/Chat.vue'
import { chatAPI, fileRagAPI , ragQueryAPI  } from '../api/api'
import LaptopIcon from '../components/icons/LaptopIcon.vue'
import ChatBubbleIcon from '../components/icons/ChatBubbleIcon.vue'
import PlusIcon from '../components/icons/PlusIcon.vue'
import SendIcon from '../components/icons/SendIcon.vue'
import TrashIcon from '../components/icons/TrashIcon.vue'

const isDark = useDark()
const messagesContainer = ref(null)
const queryInput = ref(null)
const userQuery = ref('')
const isProcessing = ref(false)
const activeConversationId = ref(null)
const activeMessages = ref([])
const conversationHistory = ref([])

// 知识库相关状态
const knowledgeBases = ref([])
const selectedKbId = ref('')

// 配置 marked
marked.setOptions({
  breaks: true,  // 支持换行
  gfm: true,     // 支持 GitHub Flavored Markdown
  sanitize: false // 允许 HTML
})

// 加载知识库列表
const loadKnowledgeBases = async () => {
  try {
    const res = await fileRagAPI.listKnowledgeBases()
    knowledgeBases.value = res.data || []
  } catch (error) {
    console.error('加载知识库列表失败:', error)
    knowledgeBases.value = []
  }
}

// 知识库变更处理
const onKbChange = () => {
  // 清空当前对话，开始新对话
  if (selectedKbId.value) {
    initiateNewConversation()
  }
}

// 自动调整输入框高度
const adjustTextareaHeight = () => {
  const textarea = queryInput.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = textarea.scrollHeight + 'px'
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 发送消息 - 使用RAG查询接口
const submitMessage = async (content) => {
  if (isProcessing.value || (!content && !userQuery.value.trim()) || !selectedKbId.value) return

  // 使用传入的 content 或用户输入框的内容
  const messageContent = content || userQuery.value.trim()

  // 添加用户消息
  const userMessage = {
    role: 'user',
    content: messageContent,
    timestamp: new Date()
  }
  activeMessages.value.push(userMessage)

  // 清空输入
  if (!content) {
    userQuery.value = ''
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
  activeMessages.value.push(assistantMessage)
  isProcessing.value = true

  let totalContent = ''

  try {
    // 获取当前知识库的 topK
    const currentKb = knowledgeBases.value.find(kb => kb.id == selectedKbId.value)
    const topK = currentKb ? currentKb.topK : 5

    // 关键：如果 activeConversationId.value 为空，不传 chatId，让后端新建
    const reader = await ragQueryAPI.ask(
        messageContent,
        selectedKbId.value,
        activeConversationId.value, // 这里一定有值
        topK
    )

    const decoder = new TextDecoder('utf-8')

    while (true) {
      try {
        const { value, done } = await reader.read()
        if (done) break

        // 累积新内容
        totalContent += decoder.decode(value)

        await nextTick(() => {
          // 更新消息
          const updatedMessage = {
            ...assistantMessage,
            content: totalContent,
            isMarkdown: true
          }
          const lastIndex = activeMessages.value.length - 1
          activeMessages.value.splice(lastIndex, 1, updatedMessage)
        })
        await scrollToBottom()
      } catch (readError) {
        console.error('读取流错误:', readError)
        break
      }
    }

    // 发送完消息后，刷新历史，拿到新 chatId
    await refreshHistoryAndSetActive()

  } catch (error) {
    // ... 错误处理保持不变
  } finally {
    isProcessing.value = false
    await scrollToBottom()
  }
}

// 加载特定对话
const switchConversation = async (chatId) => {
  activeConversationId.value = chatId
  try {
    // 使用chatType为4表示私人知识库对话
    const messages = await chatAPI.chatHistoryMessageList(chatId, 4)
    activeMessages.value = messages.map(msg => ({
      ...msg,
      isMarkdown: msg.role === 'assistant'
    }))
  } catch (error) {
    console.error('加载对话消息失败:', error)
    activeMessages.value = []
  }
}

// 加载聊天历史
const loadConversationHistory = async () => {
  try {
    // chatType=4 表示私人知识库
    const history = await chatAPI.chatTypeHistoryList(4)
    conversationHistory.value = history || []
    if (history && history.length > 0) {
      await switchConversation(history[0].id)
    } else {
      activeConversationId.value = null
      activeMessages.value = []
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
    conversationHistory.value = []
    activeConversationId.value = null
    activeMessages.value = []
  }
}

// 开始新对话
const initiateNewConversation = async () => {
  const newChatId = Date.now().toString()
  activeConversationId.value = newChatId
  activeMessages.value = []
  // 添加新对话到历史列表
  const newChat = {
    id: newChatId,
    title: `新的知识库对话`
  }
  conversationHistory.value = [newChat, ...conversationHistory.value]
}

// 删除对话
const removeConversation = async (chatId) => {
  if (!confirm('确定要删除这个对话吗？')) {
    return
  }

  try {
    // 使用chatType为4表示私人知识库对话
    await chatAPI.deleteChat(chatId, 4)
    // 从历史记录中移除
    conversationHistory.value = conversationHistory.value.filter(chat => chat.id !== chatId)

    // 如果删除的是当前对话，则创建新对话
    if (activeConversationId.value === chatId) {
      await initiateNewConversation()
    }
    await loadConversationHistory() // 删除后刷新历史
  } catch (error) {
    console.error('删除对话失败:', error)
    alert('删除对话失败，请稍后重试')
  }
}

// 刷新历史并设置当前对话
const refreshHistoryAndSetActive = async () => {
  const history = await chatAPI.chatTypeHistoryList(4)
  conversationHistory.value = history || []
  // 找到最新的对话（通常是第一个）
  if (history && history.length > 0) {
    activeConversationId.value = history[0].id
  }
}

// 检查并更新聊天标题
const checkAndUpdateChatTitles = async () => {
  try {
    // 检查是否有标题为"新的知识库对话"的聊天记录
    const hasNewChatTitle = conversationHistory.value.some(chat =>
        chat.title === '新的知识库对话'
    )

    if (!hasNewChatTitle) {
      return
    }

    // 调用接口获取聊天记录列表
    const chatListData = await chatAPI.chatTypeHistoryList(4)

    // 检查响应是否有内容
    if (!chatListData || (Array.isArray(chatListData) && chatListData.length === 0)) {
      return
    }

    // 更新聊天记录标题
    if (Array.isArray(chatListData)) {
      let hasUpdated = false

      for (let i = 0; i < conversationHistory.value.length; i++) {
        const chat = conversationHistory.value[i]
        const matchedChat = chatListData.find(apiChat => apiChat.id === chat.id)

        if (matchedChat && matchedChat.title && matchedChat.title.trim()) {
          conversationHistory.value[i] = {
            ...chat,
            title: matchedChat.title
          }
          hasUpdated = true
        }
      }

      if (hasUpdated) {
        triggerRef(conversationHistory)
        await nextTick()
      }
    }
  } catch (error) {
    console.error('检查并更新聊天标题失败:', error)
  }
}

onMounted(async () => {
  await loadKnowledgeBases()
  await loadConversationHistory()
  adjustTextareaHeight()
})
</script>

<style scoped lang="scss">
.private-rag {
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
        background: #333;
        color: white;
        border: none;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          background: #000;
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
          background: rgba(0, 0, 0, 0.05);
          transform: translateX(2px);

          .delete-btn {
            opacity: 1;
          }
        }

        &.active {
          background: rgba(0, 0, 0, 0.1);
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
      display: flex;
      justify-content: space-between;
      align-items: center;

      .service-info {
        display: flex;
        align-items: center;
        gap: 1rem;

        .avatar {
          width: 48px;
          height: 48px;
          color: #333;
          padding: 0;
          background: transparent;
          border-radius: 12px;
          transition: all 0.3s ease;

          &:hover {
            transform: scale(1.05);
          }
        }

        .info {
          h3 {
            font-size: 1.25rem;
            margin-bottom: 0.25rem;
            font-weight: 600;
          }

          p {
            font-size: 0.875rem;
            color: #666;
          }
        }
      }

      .kb-selector {
        display: flex;
        align-items: center;
        gap: 0.5rem;

        label {
          font-size: 0.875rem;
          font-weight: 500;
          color: #666;
        }

        select {
          padding: 0.5rem 1rem;
          border: 1px solid rgba(0, 0, 0, 0.1);
          border-radius: 0.5rem;
          background: white;
          color: inherit;
          font-size: 0.875rem;
          min-width: 200px;
          transition: all 0.3s ease;

          &:focus {
            outline: none;
            border-color: #333;
            box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
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
        border: 1px solid rgba(0, 0, 0, 0.1);
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
          border-color: #333;
          box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
        }

        &:disabled {
          background: #f5f5f5;
          color: #999;
          cursor: not-allowed;
        }
      }

      .send-button {
        background: #f5f5f5;
        color: #333;
        border: 1px solid #e0e0e0;
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
          background: #e8e8e8;
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
          background: rgba(255, 255, 255, 0.05);
        }

        &.active {
          background: rgba(255, 255, 255, 0.1);
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

      .kb-selector {
        label {
          color: #999;
        }

        select {
          background: rgba(50, 50, 50, 0.95);
          border-color: rgba(255, 255, 255, 0.1);
          color: white;

          &:focus {
            border-color: #666;
            box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.1);
          }
        }
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
        border-color: rgba(255, 255, 255, 0.1);
        color: white;

        &:focus {
          border-color: #666;
          box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.1);
        }

        &:disabled {
          background: rgba(30, 30, 30, 0.95);
          color: #666;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .private-rag {
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
        flex-direction: column;
        gap: 1rem;
        align-items: flex-start;

        .kb-selector {
          width: 100%;

          select {
            flex: 1;
            min-width: auto;
          }
        }
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

@media (max-width: 480px) {
  .private-rag {
    .chat-main {
      .service-header {
        .service-info {
          .info {
            h3 {
              font-size: 1.1rem;
            }

            p {
              font-size: 0.8rem;
            }
          }
        }

        .kb-selector {
          label {
            font-size: 0.8rem;
          }

          select {
            font-size: 0.8rem;
          }
        }
      }

      .input-area {
        textarea {
          font-size: 0.95rem;
        }
      }
    }
  }
}
</style>