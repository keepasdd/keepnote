import request from '../utils/request'

// 注册
export function register(data) {
    return request.post('/user/register', data)
}

// 登录
export function login(data) {
    return request.post('/user/login', data)
}

// 获取当前用户信息
export function getUserInfo() {
    return request.get('/user/info')
}

// 更新用户信息（nickname, email, avatar）
export function updateUserInfo(data) {
    return request.put('/user/update', data)
}

// 注销（退出登录）
export function logout() {
    return request.post('/user/logout')
}
