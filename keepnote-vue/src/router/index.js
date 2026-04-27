import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { requiresAuth: true, transition: 'slide-up' }
    },
    {
        path: '/note/:id',
        name: 'NoteView',
        component: () => import('../views/NoteView.vue'),
        meta: { requiresAuth: true, transition: 'fade' }
    },
    // 未匹配路由重定向首页
    { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫：未登录跳转到 /login
router.beforeEach((to) => {
    const token = sessionStorage.getItem('token')
    if (to.meta.requiresAuth && !token) {
        return '/login'
    }
    if (to.path === '/login' && token) {
        return '/'
    }
})

export default router
