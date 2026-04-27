import request from '../utils/request'

export function getTagList() {
    return request.get('/tag/list')
}

export function addTag(data) {
    return request.post('/tag/add', data)
}

export function updateTag(data) {
    return request.put('/tag/update', data)
}

export function deleteTag(id) {
    return request.delete(`/tag/${id}`)
}
