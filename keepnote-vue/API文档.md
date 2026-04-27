# keepnote-vue 接口文档

## 基本信息

- **Base URL**: `http://localhost:8080/api`
- **认证方式**: Bearer Token（JWT）
- **请求头**: `Authorization: Bearer {token}`
- **统一响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

---

## 一、用户认证

### 1.1 注册

- **POST** `/user/register`
- **请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应**: 成功消息

---

### 1.2 登录

- **POST** `/user/login`
- **请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应**:
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

## 二、笔记管理

### 2.1 获取笔记列表

- **GET** `/note/list`
- **Query 参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | number | 是 | 页码（从1开始） |
| pageSize | number | 是 | 每页数量（默认20） |
| keyword | string | 否 | 搜索关键词 |
| categoryId | number | 否 | 分类ID筛选 |
| tagId | number | 否 | 标签ID筛选 |
| isFavorite | number | 否 | 1=只看收藏 |
| dateRange | string | 否 | `today` / `week` / `month` |

- **响应**:
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "title": "笔记标题",
        "content": "笔记内容",
        "categoryId": 1,
        "categoryName": "工作",
        "categoryColor": "#409EFF",
        "tags": [{ "id": 1, "name": "重要", "color": "#F56C6C" }],
        "isFavorite": 0,
        "createdAt": "2026-04-01T10:00:00Z",
        "updatedAt": "2026-04-02T08:00:00Z"
      }
    ],
    "total": 100
  }
}
```

---

### 2.2 获取笔记详情

- **GET** `/note/{id}`
- **路径参数**: `id` — 笔记ID
- **响应**: 单个 Note 对象（同上）

---

### 2.3 新增笔记

- **POST** `/note/add`
- **请求体**:
```json
{
  "title": "string",
  "content": "string",
  "categoryId": 1,
  "tagIds": [1, 2]
}
```
- **响应**: 创建的 Note 对象

---

### 2.4 更新笔记

- **PUT** `/note/update`
- **请求体**:
```json
{
  "id": 1,
  "title": "string",
  "content": "string",
  "categoryId": 1,
  "tagIds": [1, 2],
  "isFavorite": 1
}
```
- **响应**: 更新后的 Note 对象

---

### 2.5 删除笔记

- **DELETE** `/note/{id}`
- **路径参数**: `id` — 笔记ID
- **响应**: 成功消息

---

## 三、分类管理

### 3.1 获取分类列表

- **GET** `/category/list`
- **响应**:
```json
{
  "code": 200,
  "data": [
    { "id": 1, "name": "工作", "color": "#409EFF", "noteCount": 10 }
  ]
}
```

---

### 3.2 新增分类

- **POST** `/category/add`
- **请求体**:
```json
{
  "name": "string",
  "color": "#409EFF"
}
```

---

### 3.3 更新分类

- **PUT** `/category/update`
- **请求体**:
```json
{
  "id": 1,
  "name": "string",
  "color": "#409EFF"
}
```

---

### 3.4 删除分类

- **DELETE** `/category/{id}`
- **路径参数**: `id` — 分类ID

---

## 四、标签管理

### 4.1 获取标签列表

- **GET** `/tag/list`
- **响应**:
```json
{
  "code": 200,
  "data": [
    { "id": 1, "name": "重要", "color": "#F56C6C" }
  ]
}
```

---

### 4.2 新增标签

- **POST** `/tag/add`
- **请求体**:
```json
{
  "name": "string",
  "color": "#F56C6C"
}
```

---

### 4.3 删除标签

- **DELETE** `/tag/{id}`
- **路径参数**: `id` — 标签ID

---

## 数据模型

### Note

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 笔记ID |
| title | string | 标题 |
| content | string | 内容（富文本） |
| categoryId | number\|null | 分类ID |
| categoryName | string | 分类名称 |
| categoryColor | string | 分类颜色 |
| tags | Tag[] | 标签列表 |
| isFavorite | 0\|1 | 是否收藏 |
| createdAt | string | 创建时间（ISO） |
| updatedAt | string | 更新时间（ISO） |

### Category

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 分类ID |
| name | string | 分类名称 |
| color | string | 颜色（hex） |
| noteCount | number | 笔记数量 |

### Tag

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 标签ID |
| name | string | 标签名称 |
| color | string | 颜色（hex） |
