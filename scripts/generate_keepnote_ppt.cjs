const fs = require('fs');
const path = require('path');
const { createRequire } = require('module');

const bundledRequire = createRequire('C:/Users/a/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.pnpm/pptxgenjs@4.0.1/node_modules/');
const pptxgen = bundledRequire('pptxgenjs');

const outDir = 'D:/javaproject/keepnote/outputs/keepnote-final-presentation';
const oneDriveOut = 'C:/Users/a/OneDrive/文档/KeepNote-Vue期末小组考核PPT.pptx';
const assetDir = 'D:/javaproject/keepnote-vue/src/assets';
fs.mkdirSync(outDir, { recursive: true });

const pptx = new pptxgen();
pptx.layout = 'LAYOUT_WIDE';
pptx.author = 'KeepNote 独立开发项目';
pptx.subject = 'Vue 应用程序开发期末考核';
pptx.title = 'KeepNote Vue 笔记管理系统';
pptx.company = 'KeepNote';
pptx.lang = 'zh-CN';
pptx.theme = { headFontFace: 'Microsoft YaHei', bodyFontFace: 'Microsoft YaHei', lang: 'zh-CN' };
pptx.defineLayout({ name: 'LAYOUT_WIDE', width: 13.333, height: 7.5 });

const C = {
  bg: '101712',
  panel: '1E2A22',
  panel2: '26342B',
  ink: 'F5F7F2',
  muted: 'B7C2B5',
  dim: '788579',
  accent: '7EBA6C',
  accent2: '4ECDBA',
  blue: '5B9CFF',
  orange: 'F59E42',
  red: 'EF6666',
  purple: 'B78CFF',
  line: '385144',
  white: 'FFFFFF',
};

function addBg(slide) {
  slide.background = { color: C.bg };
  slide.addShape(pptx.ShapeType.rect, { x: 0, y: 0, w: 13.333, h: 7.5, fill: { color: C.bg }, line: { color: C.bg } });
  slide.addShape(pptx.ShapeType.rect, { x: 0, y: 7.17, w: 13.333, h: 0.33, fill: { color: C.accent, transparency: 84 }, line: { color: C.bg } });
}

function addTitle(slide, title, kicker = 'KEEPNOTE') {
  slide.addText(kicker, { x: 0.55, y: 0.35, w: 2.2, h: 0.2, fontSize: 8, bold: true, color: C.accent, charSpace: 1.2, margin: 0 });
  slide.addText(title, { x: 0.55, y: 0.58, w: 9.2, h: 0.42, fontSize: 20.5, bold: true, color: C.ink, margin: 0, fit: 'shrink', breakLine: false });
  slide.addShape(pptx.ShapeType.line, { x: 0.55, y: 1.1, w: 12.2, h: 0, line: { color: C.line, width: 1 } });
}

function footer(slide, owner, n) {
  slide.addText(`汇报重点：${owner}`, { x: 0.55, y: 7.12, w: 5.3, h: 0.18, fontSize: 7.5, color: C.dim, margin: 0 });
  slide.addText(`${String(n).padStart(2, '0')}`, { x: 12.25, y: 7.05, w: 0.55, h: 0.25, fontSize: 9, color: C.accent, bold: true, align: 'right', margin: 0 });
}

function pill(slide, text, x, y, w, color = C.accent) {
  slide.addShape(pptx.ShapeType.roundRect, { x, y, w, h: 0.34, rectRadius: 0.06, fill: { color, transparency: 84 }, line: { color, transparency: 40, width: 1 } });
  slide.addText(text, { x: x + 0.08, y: y + 0.075, w: w - 0.16, h: 0.16, fontSize: 8, color, bold: true, align: 'center', margin: 0 });
}

function bullet(slide, items, x, y, w, opts = {}) {
  const fs = opts.fontSize || 12.5;
  const gap = opts.gap || 0.5;
  items.forEach((item, i) => {
    const yy = y + i * gap;
    slide.addShape(pptx.ShapeType.ellipse, { x, y: yy + 0.08, w: 0.12, h: 0.12, fill: { color: opts.color || C.accent }, line: { color: opts.color || C.accent } });
    slide.addText(item, { x: x + 0.24, y: yy, w, h: 0.36, fontSize: fs, color: opts.textColor || C.ink, margin: 0, fit: 'shrink', breakLine: false });
  });
}

function card(slide, x, y, w, h, title, body, color = C.accent) {
  slide.addShape(pptx.ShapeType.roundRect, { x, y, w, h, rectRadius: 0.08, fill: { color: C.panel2 }, line: { color: C.line, width: 1 } });
  slide.addShape(pptx.ShapeType.rect, { x, y, w: 0.06, h, fill: { color }, line: { color } });
  slide.addText(title, { x: x + 0.24, y: y + 0.2, w: w - 0.45, h: 0.25, fontSize: 12.2, bold: true, color: C.ink, margin: 0 });
  slide.addText(body, { x: x + 0.24, y: y + 0.58, w: w - 0.42, h: h - 0.75, fontSize: 9.3, color: C.muted, margin: 0.02, fit: 'shrink', breakLine: false, valign: 'top' });
}

function codeBox(slide, text, x, y, w, h) {
  slide.addShape(pptx.ShapeType.roundRect, { x, y, w, h, rectRadius: 0.05, fill: { color: '0A0F0C' }, line: { color: C.line, width: 1 } });
  slide.addText(text, { x: x + 0.18, y: y + 0.18, w: w - 0.35, h: h - 0.28, fontFace: 'Consolas', fontSize: 8.8, color: 'CDE8C2', margin: 0, fit: 'shrink', breakLine: false });
}

function scriptBox(slide, text, x = 0.85, y = 6.02, w = 11.65, h = 0.68) {
  slide.addShape(pptx.ShapeType.roundRect, { x, y, w, h, rectRadius: 0.05, fill: { color: C.panel }, line: { color: C.line, width: 1 } });
  slide.addText('讲稿', { x: x + 0.18, y: y + 0.12, w: 0.42, h: 0.16, fontSize: 7.5, bold: true, color: C.accent, margin: 0 });
  slide.addText(text, { x: x + 0.72, y: y + 0.1, w: w - 0.95, h: h - 0.18, fontSize: 9.35, color: C.muted, margin: 0, fit: 'shrink', breakLine: false });
}

function quote(slide, text, x, y, w) {
  slide.addText(text, { x, y, w, h: 0.65, fontSize: 22.5, bold: true, color: C.ink, margin: 0, fit: 'shrink', breakLine: false });
}

// 1 封面
{
  const s = pptx.addSlide();
  addBg(s);
  const img = path.join(assetDir, 'login-bg.jpg');
  if (fs.existsSync(img)) {
    s.addImage({ path: img, x: 0, y: 0, w: 13.333, h: 7.5, transparency: 38 });
    s.addShape(pptx.ShapeType.rect, { x: 0, y: 0, w: 13.333, h: 7.5, fill: { color: '0B120E', transparency: 18 }, line: { color: '0B120E', transparency: 100 } });
  }
  s.addText('KN', { x: 0.7, y: 0.6, w: 0.55, h: 0.42, fontSize: 14, bold: true, color: C.ink, fill: { color: C.white, transparency: 86 }, line: { color: C.white, transparency: 72 }, align: 'center', valign: 'mid', margin: 0 });
  s.addText('Vue 应用程序开发 · 期末项目考核', { x: 1.38, y: 0.69, w: 4.3, h: 0.2, fontSize: 9, color: C.muted, margin: 0 });
  s.addText('KeepNote', { x: 0.7, y: 2.0, w: 5.8, h: 0.65, fontSize: 38, bold: true, color: C.ink, margin: 0 });
  s.addText('个人笔记管理系统', { x: 0.72, y: 2.73, w: 4.6, h: 0.35, fontSize: 20, bold: true, color: C.accent, margin: 0 });
  s.addText('围绕记录、分类、检索和沉淀的个人知识管理场景，使用 Vue 3 构建一个完整的前端应用。', { x: 0.74, y: 3.33, w: 6.0, h: 0.56, fontSize: 14, color: C.ink, fit: 'shrink', breakLine: false, margin: 0 });
  pill(s, 'Vue 3', 0.74, 4.18, 0.85);
  pill(s, 'Vite', 1.72, 4.18, 0.72, C.accent2);
  pill(s, 'Element Plus', 2.58, 4.18, 1.35, C.blue);
  pill(s, 'Pinia', 4.06, 4.18, 0.78, C.orange);
  s.addText('开发者：兰天 · 独立完成', { x: 0.74, y: 6.58, w: 4.5, h: 0.25, fontSize: 11, color: C.muted, margin: 0 });
  footer(s, '兰天：项目名称和整体定位', 1);
}

// 2 项目背景
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '项目背景：为什么选择做 KeepNote');
  quote(s, '我们选择笔记管理系统，是因为它既贴近日常使用，又能覆盖 Vue 项目开发中的核心知识点。', 0.72, 1.35, 8.3);
  card(s, 0.85, 2.7, 3.65, 1.65, '真实需求', '学习资料、课程任务、灵感想法经常分散在聊天记录、文档和浏览器收藏中。', C.accent);
  card(s, 4.85, 2.7, 3.65, 1.65, '课程匹配', '笔记系统天然包含登录、列表、详情、筛选、编辑、上传等典型前端场景。', C.blue);
  card(s, 8.85, 2.7, 3.65, 1.65, '展示清晰', '老师可以通过页面演示直接看到功能，也可以通过代码页看到实现思路。', C.orange);
  scriptBox(s, '这一页介绍项目选题。我不是为了做一个复杂但难演示的系统，而是选择一个贴近日常学习的笔记应用，让功能、代码和课程知识点都能对应起来。');
  footer(s, '兰天：选题背景和课程价值', 2);
}

// 3 需求目标
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '需求分析：从“能记录”到“能管理”');
  bullet(s, [
    '基础需求：用户可以注册、登录，并且只能访问自己的笔记空间。',
    '内容需求：可以新增、编辑、删除和查看笔记，支持标题、正文、分类和标签。',
    '管理需求：可以按关键词、日期、分类、标签、收藏状态快速筛选笔记。',
    '扩展需求：笔记可以上传附件，支持图片预览、普通文件下载和删除。',
    '体验需求：界面要清晰、操作路径短，适合长期作为个人知识库使用。',
  ], 0.95, 1.55, 7.3, { fontSize: 12.2, gap: 0.58, color: C.accent });
  card(s, 8.9, 1.6, 3.35, 1.15, '核心目标', '让用户把零散信息沉淀成可分类、可搜索、可复用的个人知识库。', C.accent2);
  card(s, 8.9, 3.05, 3.35, 1.15, '验收方式', '功能可以现场演示，代码可以说明关键实现，而不是只停留在静态页面。', C.blue);
  scriptBox(s, '这一页讲需求时，可以按从基础到扩展的顺序读。我们的目标不是只实现一个输入框，而是让笔记具备完整生命周期：登录进入、创建内容、分类整理、筛选查找、附件补充。');
  footer(s, '兰天：需求拆解和项目目标', 3);
}

// 4 功能模块
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '功能模块：围绕笔记生命周期组织页面');
  const modules = [
    ['用户认证', '登录 / 注册\nJWT Token 保存\n路由守卫保护页面', C.accent],
    ['笔记管理', '新建 / 编辑 / 删除\n详情查看\n字数与阅读时长统计', C.accent2],
    ['组织能力', '分类管理\n多级标签\n收藏与置顶', C.blue],
    ['检索筛选', '关键词搜索\n按日期筛选\n按分类、标签筛选', C.orange],
    ['资料扩展', '附件上传\n图片预览\n文件下载与删除', C.red],
    ['个性化', '个人资料\n头像上传\n主题色与明暗背景', C.purple],
  ];
  modules.forEach((m, i) => card(s, 0.72 + (i % 3) * 4.15, 1.42 + Math.floor(i / 3) * 2.18, 3.65, 1.58, m[0], m[1], m[2]));
  scriptBox(s, '这一页做功能总览。可以先说系统分成六个模块，再强调这些模块不是孤立的：用户登录以后进入首页，通过分类、标签和搜索管理笔记，最后在详情页完成查看、编辑和附件管理。');
  footer(s, '兰天：功能模块总览', 4);
}

// 5 用户流程
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '使用流程：老师可以按这条路径观看演示');
  const steps = [
    ['1', '登录 / 注册', '进入个人笔记空间'],
    ['2', '新建笔记', '填写标题、内容、分类、标签'],
    ['3', '整理笔记', '收藏、置顶、按分类和标签归档'],
    ['4', '查找笔记', '关键词、日期、标签组合筛选'],
    ['5', '补充资料', '上传图片或文件作为附件'],
  ];
  steps.forEach((st, i) => {
    const x = 0.78 + i * 2.48;
    s.addShape(pptx.ShapeType.roundRect, { x, y: 2.0, w: 2.05, h: 2.2, rectRadius: 0.08, fill: { color: C.panel2 }, line: { color: C.line } });
    s.addText(st[0], { x: x + 0.68, y: 2.22, w: 0.7, h: 0.5, fontSize: 22, bold: true, color: C.accent, align: 'center', margin: 0 });
    s.addText(st[1], { x: x + 0.18, y: 2.95, w: 1.68, h: 0.26, fontSize: 12.2, bold: true, color: C.ink, align: 'center', margin: 0 });
    s.addText(st[2], { x: x + 0.18, y: 3.35, w: 1.68, h: 0.42, fontSize: 9.2, color: C.muted, align: 'center', margin: 0, fit: 'shrink', breakLine: false });
    if (i < steps.length - 1) s.addShape(pptx.ShapeType.line, { x: x + 2.08, y: 3.12, w: 0.35, h: 0, line: { color: C.accent, width: 2, endArrowType: 'triangle' } });
  });
  scriptBox(s, '这页可以直接作为现场演示顺序。我们先登录系统，然后新建一篇笔记，给它加上分类和标签，再演示搜索、筛选、收藏、置顶，最后展示附件上传和图片预览。');
  footer(s, '兰天：演示流程和操作顺序', 5);
}

// 6 架构
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '技术架构：Vue 前端与后端 API 解耦');
  const boxes = [
    ['视图层', 'Login / Home / NoteView\nSidebar / NoteList / NoteDetail', 0.8, 1.5, C.accent],
    ['状态与路由', 'Pinia 管理用户状态\nVue Router 控制访问权限', 4.7, 1.5, C.blue],
    ['接口层', 'Axios 实例统一封装\n请求/响应拦截器处理 Token 与错误', 8.6, 1.5, C.orange],
    ['后端服务', '/api/user\n/api/note\n/api/category\n/api/tag\n/file/*', 4.7, 4.15, C.accent2],
  ];
  boxes.forEach(([t, b, x, y, c]) => card(s, x, y, 3.05, 1.38, t, b, c));
  [[3.85, 2.17, 0.65, 0], [7.75, 2.17, 0.65, 0], [10.05, 2.96, -2.05, 0.98], [6.2, 3.96, 0, -0.95]].forEach(([x, y, w, h]) => s.addShape(pptx.ShapeType.line, { x, y, w, h, line: { color: C.accent, width: 2, endArrowType: 'triangle' } }));
  scriptBox(s, '这里进入技术讲解。整个项目是典型的前后端分离结构：页面组件负责展示，Pinia 和路由负责状态与访问控制，Axios 负责统一请求，真正的数据由后端 API 提供。');
  footer(s, '兰天：整体架构和技术栈', 6);
}

// 7 代码认证
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '代码重点一：登录状态、路由守卫和请求拦截');
  codeBox(s, `router.beforeEach((to) => {\n  const token = sessionStorage.getItem('token')\n  if (to.meta.requiresAuth && !token) return '/login'\n  if (to.path === '/login' && token) return '/'\n})`, 0.75, 1.38, 5.8, 1.9);
  codeBox(s, `request.interceptors.request.use(config => {\n  const token = sessionStorage.getItem('token')\n  if (token) config.headers.Authorization = \`Bearer \${token}\`\n  return config\n})\n\nrequest.interceptors.response.use(response => {\n  const res = response.data\n  if (res.code === 200) return res.data\n  ElMessage.error(res.message || '请求失败')\n})`, 6.85, 1.38, 5.65, 3.45);
  bullet(s, [
    '路由 meta.requiresAuth 用来判断页面是否需要登录。',
    'Token 自动添加到 Authorization 请求头，组件不用重复处理。',
    '401 或业务错误统一提示，减少每个页面重复写 try/catch 的成本。',
  ], 0.95, 4.25, 5.8, { fontSize: 11.4, gap: 0.45, color: C.accent });
  scriptBox(s, '这一页重点讲安全和复用。用户登录以后，Token 被保存起来；访问需要登录的页面时，路由守卫会检查 Token；每次请求接口时，Axios 拦截器会自动带上 Token，并统一处理错误提示。');
  footer(s, '兰天：认证与请求封装代码', 7);
}

// 8 代码状态
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '代码重点二：Pinia 用户状态管理');
  codeBox(s, `export const useUserStore = defineStore('user', () => {\n  const token = ref(sessionStorage.getItem('token') || '')\n  const userInfo = ref(null)\n\n  async function login(username, password) {\n    const data = await loginApi({ username, password })\n    token.value = data.token\n    sessionStorage.setItem('token', data.token)\n    userInfo.value = data.user\n    router.push('/')\n  }\n\n  async function logout() {\n    token.value = ''\n    userInfo.value = null\n    sessionStorage.removeItem('token')\n    router.push('/login')\n  }\n})`, 0.75, 1.33, 6.3, 4.25);
  bullet(s, [
    '把登录、退出、获取资料等用户逻辑集中在 store 中。',
    '页面只调用 userStore.login 或 userStore.logout，职责更清晰。',
    '刷新页面时从 sessionStorage 恢复 Token，保证体验连续。',
    '后续如果要加用户头像、昵称、邮箱，也可以继续放在同一个状态模块中。',
  ], 7.35, 1.52, 4.9, { fontSize: 11.2, gap: 0.56, color: C.blue });
  scriptBox(s, '这页说明为什么使用 Pinia。我们没有把登录状态写在某一个页面里，而是抽成全局 store，这样 Login 页面、Sidebar 用户区和路由逻辑都能共享同一份状态。');
  footer(s, '兰天：Pinia 状态管理代码', 8);
}

// 9 代码组件联动
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '代码重点三：首页三栏组件联动');
  s.addShape(pptx.ShapeType.roundRect, { x: 0.8, y: 1.42, w: 2.55, h: 3.5, rectRadius: 0.07, fill: { color: C.panel2 }, line: { color: C.line } });
  s.addText('Sidebar\n搜索 / 导航 / 分类', { x: 1.05, y: 2.77, w: 2.05, h: 0.52, fontSize: 13.3, bold: true, color: C.ink, align: 'center', margin: 0, fit: 'shrink' });
  s.addShape(pptx.ShapeType.roundRect, { x: 3.75, y: 1.42, w: 4.6, h: 3.5, rectRadius: 0.07, fill: { color: C.panel2 }, line: { color: C.line } });
  s.addText('NoteList\n列表 / 筛选 / 标签树 / 分页', { x: 4.35, y: 2.77, w: 3.35, h: 0.52, fontSize: 13.3, bold: true, color: C.ink, align: 'center', margin: 0, fit: 'shrink' });
  s.addShape(pptx.ShapeType.roundRect, { x: 8.75, y: 1.42, w: 3.65, h: 3.5, rectRadius: 0.07, fill: { color: C.panel2 }, line: { color: C.line } });
  s.addText('NoteDetail\n查看 / 编辑 / 附件', { x: 9.2, y: 2.77, w: 2.75, h: 0.52, fontSize: 13.3, bold: true, color: C.ink, align: 'center', margin: 0, fit: 'shrink' });
  s.addShape(pptx.ShapeType.line, { x: 3.36, y: 3.05, w: 0.36, h: 0, line: { color: C.accent, width: 2, endArrowType: 'triangle' } });
  s.addShape(pptx.ShapeType.line, { x: 8.36, y: 3.05, w: 0.36, h: 0, line: { color: C.accent, width: 2, endArrowType: 'triangle' } });
  bullet(s, [
    'Home.vue 统一维护 notes、categories、tags、activeNote 等状态。',
    '子组件通过 props 接收数据，通过 emit 把用户操作交给父组件处理。',
    '保存或删除后由父组件统一刷新列表、详情和侧边栏统计，避免数据不同步。',
  ], 1.0, 5.28, 11.1, { fontSize: 10.9, gap: 0.34, color: C.accent2 });
  scriptBox(s, '这一页可以讲组件通信。首页不是一个大组件写到底，而是拆成侧边栏、列表和详情三块；父组件 Home 负责统一状态，子组件负责具体界面和事件触发。');
  footer(s, '兰天：组件化设计与状态联动', 9);
}

// 10 代码列表
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '代码重点四：列表查询、筛选和加载状态');
  codeBox(s, `async function loadNotes() {\n  listLoading.value = true\n  try {\n    const params = {\n      page: currentPage.value,\n      pageSize,\n      keyword: keyword.value || undefined,\n      categoryId: activeCategoryId.value || undefined,\n      tagId: activeTagId.value || undefined,\n      isFavorite: activeNav.value === 'favorite' ? 1 : undefined,\n      dateRange: dateRange.value || undefined,\n    }\n    const data = await getNoteList(params)\n    notes.value = data.records\n    total.value = data.total\n  } finally {\n    listLoading.value = false\n  }\n}`, 0.75, 1.3, 6.55, 4.45);
  bullet(s, [
    '一个 loadNotes 方法覆盖搜索、分类、标签、收藏和日期筛选。',
    '空筛选条件转成 undefined，避免给后端传递无意义参数。',
    'loading 放在 finally 中关闭，接口成功或失败都不会卡住页面。',
    '列表数据和总数一起更新，便于分页组件同步显示。',
  ], 7.6, 1.52, 4.6, { fontSize: 11.4, gap: 0.58, color: C.accent2 });
  scriptBox(s, '这页说明列表功能的核心。用户在页面上点击分类、标签、收藏夹或者搜索框，本质上都是改变响应式状态，再调用 loadNotes 重新请求数据。');
  footer(s, '兰天：列表查询和筛选代码', 10);
}

// 11 代码保存附件
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '代码重点五：笔记保存与附件上传');
  codeBox(s, `async function save() {\n  if (!editForm.title.trim())\n    return ElMessage.warning('标题不能为空')\n  saving.value = true\n  try {\n    if (editForm.id) await updateNote(editForm)\n    else editForm.id = await addNote(editForm)\n    emit('saved', { ...editForm })\n  } finally {\n    saving.value = false\n  }\n}\n\nexport function uploadAttachment(noteId, file) {\n  const form = new FormData()\n  form.append('file', file)\n  form.append('noteId', noteId)\n  return request.post('/file/attachment/upload', form)\n}`, 0.75, 1.3, 6.25, 4.45);
  card(s, 7.35, 1.5, 4.85, 0.95, '编辑与新增复用', 'editForm 有 id 时更新，没有 id 时新增，减少重复页面和重复逻辑。', C.accent);
  card(s, 7.35, 2.68, 4.85, 0.95, '父子通信', '保存成功后 emit saved，由 Home.vue 统一刷新列表、详情和统计。', C.blue);
  card(s, 7.35, 3.86, 4.85, 0.95, '附件扩展', 'FormData 传输文件，并用 noteId 关联到对应笔记。', C.orange);
  scriptBox(s, '最后一页代码讲笔记的写入能力。保存函数同时支持新增和修改，附件上传使用 FormData，把文件和 noteId 一起传给后端，让笔记不只保存文字，也能保存学习资料。');
  footer(s, '兰天：编辑保存与附件上传代码', 11);
}

// 12 界面设计
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '界面设计：工作台式布局，降低记录和查找成本');
  const img = path.join(assetDir, 'login-bg.png');
  if (fs.existsSync(img)) {
    s.addImage({ path: img, x: 8.15, y: 1.28, w: 4.05, h: 4.05, transparency: 14 });
    s.addShape(pptx.ShapeType.rect, { x: 8.15, y: 1.28, w: 4.05, h: 4.05, fill: { color: C.bg, transparency: 35 }, line: { color: C.line } });
  }
  bullet(s, [
    '登录页使用沉浸式背景和登录/注册切换，形成完整入口体验。',
    '首页采用三栏工作台：左侧导航，中间列表，右侧详情。',
    '标签、分类和主题色使用颜色作为视觉线索，帮助用户快速辨认内容归属。',
    '设置抽屉整合个人资料、头像和界面主题，让应用具备个性化能力。',
  ], 0.95, 1.55, 6.55, { fontSize: 12.6, gap: 0.62, color: C.orange });
  card(s, 8.25, 5.45, 3.75, 0.75, '设计关键词', '清晰 / 可扫读 / 少干扰 / 有品牌感', C.accent);
  scriptBox(s, '这一页讲界面体验。可以按页面从登录页讲到首页三栏，再讲分类、标签、主题这些视觉线索，让老师看到项目不仅能运行，也考虑了用户使用体验。');
  footer(s, '兰天：界面体验和视觉设计', 12);
}

// 13 项目亮点
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '项目亮点：功能完整，代码结构也比较清晰');
  card(s, 0.85, 1.45, 3.55, 1.55, '完整用户闭环', '从登录进入，到创建、整理、查找、查看、编辑、上传附件，形成完整使用链路。', C.accent);
  card(s, 4.9, 1.45, 3.55, 1.55, '组件职责清楚', '页面拆分为视图、组件、api、store 和 utils，便于定位问题和后续维护。', C.blue);
  card(s, 8.95, 1.45, 3.55, 1.55, '交互细节较多', '收藏、置顶、标签树、日期筛选、附件预览、主题切换等都能作为演示点。', C.orange);
  bullet(s, [
    '课程知识点对应：Vue 组件化、组合式 API、路由守卫、Pinia、Axios、Element Plus、Vite 工程配置。',
    '答辩展示方式：先讲需求和功能，再讲核心代码，最后现场演示用户流程。',
    '本项目由兰天独立完成，因此答辩会从需求、功能、界面、代码和总结五个角度完整说明。',
  ], 0.95, 4.0, 11.0, { fontSize: 11.1, gap: 0.42, color: C.accent2 });
  scriptBox(s, '这一页可以作为代码讲解后的过渡。我们总结项目亮点时，不只说功能多，还要说这些功能对应了课程知识点，并且代码分层比较清楚，方便老师理解我们的实现质量。');
  footer(s, '兰天：项目亮点和展示价值', 13);
}

// 14 独立开发说明
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '独立开发说明：从需求到实现由本人完整完成');
  const rows = [
    ['需求分析', '确定项目目标', '围绕个人笔记管理场景，拆解登录、笔记管理、分类标签、筛选检索和附件能力', C.accent],
    ['前端实现', '完成核心开发', '使用 Vue 3、Pinia、Vue Router、Axios、Element Plus 完成页面、状态和接口交互', C.blue],
    ['展示整理', '准备答辩材料', '整理功能流程、代码讲解、界面设计、项目亮点和后续优化方向，形成完整展示逻辑', C.orange],
  ];
  rows.forEach((r, i) => {
    const y = 1.5 + i * 1.4;
    s.addShape(pptx.ShapeType.roundRect, { x: 0.85, y, w: 11.65, h: 1.02, rectRadius: 0.07, fill: { color: C.panel2 }, line: { color: C.line } });
    s.addShape(pptx.ShapeType.rect, { x: 0.85, y, w: 0.09, h: 1.02, fill: { color: r[3] }, line: { color: r[3] } });
    s.addText(r[0], { x: 1.15, y: y + 0.22, w: 1.05, h: 0.28, fontSize: 16, bold: true, color: C.ink, margin: 0 });
    s.addText(r[1], { x: 2.35, y: y + 0.26, w: 2.2, h: 0.22, fontSize: 11.2, color: r[3], bold: true, margin: 0 });
    s.addText(r[2], { x: 4.75, y: y + 0.22, w: 7.1, h: 0.36, fontSize: 10.8, color: C.muted, margin: 0, fit: 'shrink', breakLine: false });
  });
  scriptBox(s, '这一页说明项目完成方式。本项目由我独立完成，从选题、需求拆解、前端页面、接口封装、状态管理、界面样式到答辩材料整理都由本人负责，因此后面的答疑也可以直接对应到代码实现。');
  footer(s, '兰天：独立开发过程说明', 14);
}

// 15 总结
{
  const s = pptx.addSlide();
  addBg(s); addTitle(s, '总结与展望：从课程项目走向真实可用工具');
  card(s, 0.85, 1.45, 3.65, 2.05, '已完成', '认证登录、笔记 CRUD、分类标签、搜索筛选、收藏置顶、附件上传、主题和个人资料。', C.accent);
  card(s, 4.85, 1.45, 3.65, 2.05, '课程收获', '把 Vue 单文件组件、组合式 API、路由、状态管理和接口交互整合成完整应用。', C.blue);
  card(s, 8.85, 1.45, 3.65, 2.05, '后续优化', '富文本编辑器、Markdown 预览、笔记分享、回收站、移动端适配和数据统计。', C.orange);
  s.addText('KeepNote 的完成度体现在：不是只展示页面，而是把用户流程、接口调用和组件状态真正串起来。', { x: 1.1, y: 4.45, w: 11.1, h: 0.55, fontSize: 17.5, bold: true, color: C.ink, align: 'center', margin: 0, fit: 'shrink' });
  s.addText('谢谢观看，欢迎老师和同学提问', { x: 2.1, y: 5.52, w: 9.2, h: 0.35, fontSize: 18, color: C.accent, bold: true, align: 'center', margin: 0 });
  scriptBox(s, '最后总结时可以强调：这个项目覆盖了 Vue 课程中的核心知识点，并且每个功能都能和代码实现对应起来。后续如果继续完善，可以把它升级成真正可长期使用的个人知识管理工具。', 0.85, 6.34, 11.65, 0.56);
  footer(s, '兰天：总结与答疑', 15);
}

const out = path.join(outDir, 'KeepNote-Vue期末小组考核PPT.pptx');
pptx.writeFile({ fileName: out }).then(() => {
  fs.mkdirSync(path.dirname(oneDriveOut), { recursive: true });
  fs.copyFileSync(out, oneDriveOut);
});
