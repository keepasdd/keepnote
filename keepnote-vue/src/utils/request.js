import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
    baseURL: '/api',        // 走 Vite 代理，开发时转发到 localhost:8080
    timeout: 10000,
})

// 请求拦截器：自动带上 token
request.interceptors.request.use(
    config => {
        const token = sessionStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器：统一处理错误
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code === 200) {
            return res.data  // 直接返回 data 字段，调用方不用每次 .data.data
        }
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message))
    },
    error => {
        if (error.response?.status === 401) {
            sessionStorage.removeItem('token')
            router.push('/login')
            ElMessage.error('登录已过期，请重新登录')
        } else {
            ElMessage.error(error.response?.data?.message || '网络错误')
        }
        return Promise.reject(error)
    }
)

export default request