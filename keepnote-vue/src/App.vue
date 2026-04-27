<template>
  <RouterView v-slot="{ Component, route }">
    <Transition :name="route.meta.transition || 'fade-up'" mode="out-in">
      <component :is="Component" :key="route.path" />
    </Transition>
  </RouterView>
</template>

<style>
/* ===== 登录页专属：向上淡出消失 ===== */
.fade-up-leave-active {
  transition: opacity 0.45s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.45s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-up-leave-to {
  opacity: 0;
  transform: translateY(-18px) scale(0.98);
}
.fade-up-enter-active {
  transition: opacity 0.01s;
}
.fade-up-enter-from {
  opacity: 0;
}

/* ===== 主页进场：从下方滑入 ===== */
.slide-up-enter-active {
  transition: opacity 0.5s cubic-bezier(0.22, 1, 0.36, 1),
              transform 0.5s cubic-bezier(0.22, 1, 0.36, 1);
  transition-delay: 0.05s;
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(22px);
}
.slide-up-leave-active {
  transition: opacity 0.25s ease;
}
.slide-up-leave-to {
  opacity: 0;
}

/* ===== 通用淡入淡出（其他页面）===== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
