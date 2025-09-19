<template>
  <div class="upload-kb">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">私人知识库管理</h1>
      <p class="page-subtitle">上传、管理和查询您的知识库文件</p>
    </div>

    <!-- 上传表单 -->
    <div class="upload-section">
      <h2 class="section-title">上传新知识库</h2>
      <form @submit.prevent="uploadFile" class="upload-form">
        <div class="form-grid">
          <div class="form-group">
            <label for="kbName">知识库名称</label>
            <input
                id="kbName"
                v-model="kbName"
                type="text"
                placeholder="请输入知识库名称"
                required
            />
          </div>
          <div class="form-group">
            <label for="kbRemark">备注信息</label>
            <input
                id="kbRemark"
                v-model="kbRemark"
                type="text"
                placeholder="请输入备注信息（可选）"
            />
          </div>
          <div class="form-group">
            <label for="fileInput">选择文件</label>
            <div class="file-input-wrapper">
              <input
                  id="fileInput"
                  type="file"
                  @change="onFileChange"
                  class="file-input"
                  accept=".txt,.pdf,.doc,.docx"
              />
              <span class="file-input-text">
                {{ selectedFile ? selectedFile.name : '点击选择文件' }}
              </span>
            </div>
          </div>
        </div>
        <button type="submit" class="btn btn-primary" :disabled="!selectedFile || !kbName">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7,10 12,15 17,10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          上传知识库
        </button>
      </form>
      <div v-if="uploadMsg" class="upload-message" :class="{ 'success': uploadMsg.includes('成功') }">
        {{ uploadMsg }}
      </div>
    </div>

    <!-- 查询和表格区域 -->
    <div class="table-section">
      <div class="section-header">
        <h2 class="section-title">知识库列表</h2>
        <div class="search-container">
          <div class="search-wrapper">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.35-4.35"/>
            </svg>
            <input
                v-model="query"
                type="text"
                placeholder="搜索知识库名称..."
                class="search-input"
                @keyup.enter="handleSearch"
            />
          </div>
          <button @click="handleSearch" class="btn btn-secondary">
            查询
          </button>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在加载数据...</p>
      </div>

      <!-- 数据表格 -->
      <div v-else class="table-container">
        <table class="data-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>备注</th>
            <th>文件名</th>
            <th>上传时间</th>
            <th>操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="item in tableData" :key="item.id">
            <td>{{ item.id }}</td>
            <td class="name-cell">{{ item.name }}</td>
            <td class="remark-cell">{{ item.remark || '-' }}</td>
            <td>{{ item.fileName }}</td>
            <td>{{ item.uploadTime }}</td>
            <td class="action-cell">
              <button @click="handleView(item)" class="btn-action btn-view">查看</button>
              <button @click="handleEdit(item)" class="btn-action btn-edit">编辑</button>
              <button @click="handleDelete(item)" class="btn-action btn-delete">删除</button>
            </td>
          </tr>
          </tbody>
        </table>

        <!-- 空状态 -->
        <div v-if="!tableData.length" class="empty-state">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14,2 14,8 20,8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10,9 9,9 8,9"/>
          </svg>
          <p>暂无知识库数据</p>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <button
            @click="handleCurrentChange(page - 1)"
            :disabled="page <= 1"
            class="pagination-btn"
        >
          上一页
        </button>
        <div class="pagination-info">
          第 {{ page }} 页，共 {{ Math.ceil(total / size) }} 页，总计 {{ total }} 条
        </div>
        <button
            @click="handleCurrentChange(page + 1)"
            :disabled="page >= Math.ceil(total / size)"
            class="pagination-btn"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="editDialogVisible" class="modal-overlay" @click="closeModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>编辑知识库</h3>
          <button @click="editDialogVisible = false" class="modal-close">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>名称</label>
            <input v-model="editForm.name" type="text" maxlength="255" />
          </div>
          <div class="form-group">
            <label>备注</label>
            <input v-model="editForm.remark" type="text" maxlength="512" />
          </div>
          <div class="form-group">
            <label>TopK（召回率，默认值为算法最优值）</label>
            <input v-model.number="editForm.topK" type="number" min="1" max="100" />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="editDialogVisible = false" class="btn btn-secondary">取消</button>
          <button @click="handleEditSubmit" class="btn btn-primary">保存</button>
        </div>
      </div>
    </div>

    <!-- 查看弹窗 -->
    <div v-if="viewDialogVisible" class="modal-overlay" @click="closeViewModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>知识库详情</h3>
          <button @click="viewDialogVisible = false" class="modal-close">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="detail-item">
            <label>知识库名称：</label>
            <span>{{ viewData.name }}</span>
          </div>
          <div class="detail-item">
            <label>备注：</label>
            <span>{{ viewData.remark || '无' }}</span>
          </div>
          <div class="detail-item">
            <label>文件名：</label>
            <span>{{ viewData.fileName }}</span>
          </div>
          <div class="detail-item">
            <label>上传时间：</label>
            <span>{{ viewData.uploadTime }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 确认删除弹窗 -->
    <div v-if="deleteDialogVisible" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal modal-sm" @click.stop>
        <div class="modal-header">
          <h3>确认删除</h3>
        </div>
        <div class="modal-body">
          <p>确定要删除知识库 "{{ deleteItem.name }}" 吗？此操作不可恢复。</p>
        </div>
        <div class="modal-footer">
          <button @click="deleteDialogVisible = false" class="btn btn-secondary">取消</button>
          <button @click="confirmDelete" class="btn btn-danger">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { knowledgeBaseAPI } from '../api/KnowledgeBase'
import { fileRagAPI } from '../api/api'

// 上传相关
const kbName = ref('')
const kbRemark = ref('')
const selectedFile = ref(null)
const uploadMsg = ref('')

const onFileChange = (e) => {
  selectedFile.value = e.target.files[0]
}

const uploadFile = async () => {
  if (!selectedFile.value || !kbName.value) return

  try {
    const res = await fileRagAPI.uploadKnowledgeBase({
      file: selectedFile.value,
      name: kbName.value,
      remark: kbRemark.value
    })
    uploadMsg.value = res.message || '上传成功'
    kbName.value = ''
    kbRemark.value = ''
    selectedFile.value = null
    document.getElementById('fileInput').value = ''
    fetchData()

    // 3秒后清除消息
    setTimeout(() => {
      uploadMsg.value = ''
    }, 3000)
  } catch (error) {
    uploadMsg.value = '上传失败，请重试'
  }
}

// 分页与查询
const query = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const loading = ref(false)

// 编辑弹窗
const editDialogVisible = ref(false)
const editForm = reactive({
  id: null,
  name: '',
  remark: '',
  topK: null
})

// 查看弹窗
const viewDialogVisible = ref(false)
const viewData = ref({})

// 删除确认弹窗
const deleteDialogVisible = ref(false)
const deleteItem = ref({})

// 查询数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await knowledgeBaseAPI.page({
      page: page.value,
      size: size.value,
      name: query.value
    })
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (e) {
    console.error('加载失败:', e)
  }
  loading.value = false
}

// 分页
const handleCurrentChange = (val) => {
  if (val < 1 || val > Math.ceil(total.value / size.value)) return
  page.value = val
  fetchData()
}

// 查询按钮
const handleSearch = () => {
  page.value = 1
  fetchData()
}

// 删除
const handleDelete = (row) => {
  deleteItem.value = row
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  try {
    await knowledgeBaseAPI.delete(deleteItem.value.id)
    deleteDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const closeDeleteModal = () => {
  deleteDialogVisible.value = false
}

// 编辑
const handleEdit = (row) => {
  Object.assign(editForm, row)
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  try {
    await knowledgeBaseAPI.update(editForm)
    editDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('修改失败:', error)
  }
}

const closeModal = () => {
  editDialogVisible.value = false
}

// 查看
const handleView = (row) => {
  viewData.value = row
  viewDialogVisible.value = true
}

const closeViewModal = () => {
  viewDialogVisible.value = false
}

onMounted(fetchData)
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.upload-kb {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
  color: #333;
  line-height: 1.6;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
}

/* 上传区域 */
.upload-section {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 0 24px 0;
  color: #374151;
}

.upload-form {
  margin-bottom: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 500;
  margin-bottom: 8px;
  color: #374151;
}

.form-group input {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 文件上传 */
.file-input-wrapper {
  position: relative;
  cursor: pointer;
}

.file-input {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.file-input-text {
  display: block;
  padding: 12px 16px;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  text-align: center;
  color: #64748b;
  transition: all 0.2s;
}

.file-input-wrapper:hover .file-input-text {
  border-color: #667eea;
  background-color: #f8faff;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover {
  background: #dc2626;
}

.btn-icon {
  width: 20px;
  height: 20px;
}

/* 上传消息 */
.upload-message {
  padding: 12px 16px;
  border-radius: 8px;
  margin-top: 16px;
  background: #fef3cd;
  color: #92400e;
  border: 1px solid #f59e0b;
}

.upload-message.success {
  background: #d1fae5;
  color: #065f46;
  border-color: #10b981;
}

/* 表格区域 */
.table-section {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-container {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-wrapper {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #9ca3af;
}

.search-input {
  padding: 10px 16px 10px 44px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  width: 250px;
  transition: border-color 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

/* 加载状态 */
.loading-container {
  text-align: center;
  padding: 60px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 表格样式 */
.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.data-table th,
.data-table td {
  padding: 16px 12px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.data-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-table tbody tr:hover {
  background: #f9fafb;
}

.name-cell {
  font-weight: 500;
  color: #1f2937;
}

.remark-cell {
  color: #6b7280;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.action-cell {
  white-space: nowrap;
}

.btn-action {
  padding: 6px 12px;
  font-size: 12px;
  margin-right: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-view {
  background: #e0f2fe;
  color: #0369a1;
}

.btn-view:hover {
  background: #bae6fd;
}

.btn-edit {
  background: #fef3c7;
  color: #92400e;
}

.btn-edit:hover {
  background: #fde68a;
}

.btn-delete {
  background: #fee2e2;
  color: #dc2626;
}

.btn-delete:hover {
  background: #fecaca;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  opacity: 0.5;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.pagination-btn {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background: #f3f4f6;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  color: #6b7280;
  font-size: 14px;
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
  padding: 20px;
}

.modal {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.modal-sm {
  max-width: 400px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0;
}

.modal-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.modal-close:hover {
  background: #f3f4f6;
}

.modal-close svg {
  width: 20px;
  height: 20px;
  color: #6b7280;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  max-height: 60vh;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 24px 24px;
}

.detail-item {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.detail-item label {
  font-weight: 500;
  min-width: 100px;
  color: #374151;
}

.detail-item span {
  color: #6b7280;
  word-break: break-all;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .upload-kb {
    padding: 16px;
  }

  .page-header {
    padding: 24px 16px;
  }

  .page-title {
    font-size: 2rem;
  }

  .upload-section,
  .table-section {
    padding: 20px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-container {
    flex-direction: column;
  }

  .search-input {
    width: 100%;
  }

  .data-table {
    font-size: 14px;
  }

  .data-table th,
  .data-table td {
    padding: 12px 8px;
  }

  .btn-action {
    padding: 4px 8px;
    font-size: 11px;
    margin-right: 4px;
  }

  .modal {
    margin: 16px;
    max-width: none;
  }
}
</style>