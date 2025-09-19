const BASE_URL = 'http://localhost:8989'

export const knowledgeBaseAPI = {
  // 分页查询
  async page({ page = 1, size = 10, name = '' }) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    })

    // 只有当name不为空时才添加到参数中
    if (name.trim()) {
      params.append('name', name)
    }

    const url = `${BASE_URL}/file-rag/page?${params.toString()}`
    const response = await fetch(url)

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return response.json()
  },

  // 删除
  async delete(id) {
    const url = `${BASE_URL}/file-rag/delete`
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ id: id.toString() })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return response.json()
  },

  // 修改
  async update({ id, name, remark, topK }) {
    const url = `${BASE_URL}/file-rag/update`
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        id,
        name,
        remark: remark || '',
        topK: topK || null
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return response.json()
  }
}