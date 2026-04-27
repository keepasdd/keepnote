import request from '../utils/request'

// 笔记列表（分页 + 筛选）
export function getNoteList(params) {
    return request.get('/note/list', { params })
}

// 笔记详情
export function getNoteDetail(id) {
    return request.get(`/note/${id}`)
}

// 新增笔记
export function addNote(data) {
    return request.post('/note/add', data)
}

// 修改笔记
export function updateNote(data) {
    return request.put('/note/update', data)
}

// 置顶/取消置顶
export function pinNote(data) {
    return request.put('/note/pin', data)
}

// 删除笔记
export function deleteNote(id) {
    return request.delete(`/note/${id}`)
}

// 上传附件（multipart/form-data）
export function uploadAttachment(noteId, file) {
    const form = new FormData()
    form.append('file', file)
    form.append('noteId', noteId)
    return request.post('/file/attachment/upload', form, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 删除附件
export function deleteAttachment(attachmentId) {
    return request.delete(`/file/attachment/${attachmentId}`)
}
