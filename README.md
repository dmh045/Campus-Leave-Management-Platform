# Campus Leave Management Platform

大学生请假信息管理平台，面向学生、辅导员、任课教师和管理员四类角色，覆盖请假申请、审批流转、课程影响确认、课堂签到、考勤记录、统计分析和基础教务数据维护等场景。

## 项目特点

- 多角色登录与权限控制：学生、辅导员、任课教师、管理员按角色进入不同工作台。
- 请假闭环管理：学生提交请假申请，辅导员审批，涉及课程的任课教师进行确认。
- 按课程关联请假影响：请假时间可匹配课表，自动生成受影响课程记录。
- 辅导员批量处理：支持待办审批、批量审批、发起公假、班级请假统计。
- 任课教师考勤：支持课堂签到会话、签到详情查看、缺勤确认和考勤导出。
- 管理员基础数据维护：支持学期、班级、课程、开课信息和课表数据管理。
- 前后端分离：前端基于 Vue 3 + Vite，后端基于 Spring Boot + MyBatis + MySQL。

## 技术栈

### 后端

- Java 17
- Spring Boot 4.0.0
- Spring Web MVC
- MyBatis Spring Boot Starter 4.0.0
- MySQL Connector/J
- Lombok
- Apache POI
- JUnit / MockMvc

### 前端

- Vue 3
- TypeScript
- Vite 7
- Vue Router
- Pinia
- Element Plus
- Tailwind CSS
- Axios
- ECharts
- Day.js
- xlsx
- qrcode

## 目录结构

```text
.
├── backend/                                # Spring Boot 后端
│   ├── src/main/java/com/example/leavesystem
│   │   ├── common/                         # 统一响应、异常处理
│   │   ├── controller/                     # REST 接口
│   │   ├── dto/                            # 请求/响应数据对象
│   │   ├── entity/                         # 数据库实体
│   │   ├── mapper/                         # MyBatis Mapper
│   │   ├── security/                       # Token 认证与角色拦截
│   │   └── service/                        # 业务服务
│   ├── src/main/resources
│   │   ├── application.properties          # 后端端口与数据库配置
│   │   └── leave_system_database.sql       # 数据库初始化脚本
│   └── src/test/java/...                   # 接口与业务测试
├── frontend/                               # Vue 前端
│   ├── src/api/                            # 前端接口封装
│   ├── src/components/                     # 通用组件
│   ├── src/config/                         # 角色菜单配置
│   ├── src/router/                         # 路由配置
│   ├── src/store/                          # Pinia 状态
│   ├── src/views/                          # 页面视图
│   └── vite.config.ts                      # Vite 配置与代理
├── backend/TestCase/                       # HTTP 调试用例
├── 测试案例.xlsx
├── 大学生请假信息管理平台需求文档.pdf
└── 校假通项目报告.pdf
```

## 快速开始

### 1. 环境准备

- JDK 17+
- MySQL 8+
- Node.js 20+，建议使用 Node.js 20.19 或更高版本
- npm

### 2. 初始化数据库

创建数据库：

```sql
CREATE DATABASE leave_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

导入初始化脚本：

```bash
mysql -u root -p leave_system < backend/src/main/resources/leave_system_database.sql
```

修改后端数据库连接配置：

```properties
# backend/src/main/resources/application.properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/leave_system?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=你的数据库密码
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

mybatis.configuration.map-underscore-to-camel-case=true
```

注意：当前仓库中的 `application.properties` 带有本地数据库密码，部署或提交公开仓库前建议改为环境变量或本机私有配置。

### 3. 启动后端

```bash
cd backend
./mvnw spring-boot:run
```

Windows PowerShell：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

默认服务地址：

```text
http://localhost:8080
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务默认地址通常为：

```text
http://localhost:5173
```

`frontend/vite.config.ts` 已配置开发代理：

- `/api` 转发到 `http://localhost:8080`
- `/admin` 转发到 `http://localhost:8080`

因此本地开发时前端可以直接请求 `/api/...` 和 `/admin/...`。

## 测试账号

初始化 SQL 中包含以下学生测试账号：

| 角色 | 账号 | 密码 | 说明 |
| --- | --- | --- | --- |
| 学生 | `20210001` | `123456` | 小明 |
| 学生 | `20210002` | `123456` | 小红 |

初始化 SQL 中包含以下教职工数据，但 `staff.password` 默认为 `NULL`。如果需要登录辅导员或任课教师账号，请先设置密码：

```sql
UPDATE staff SET password = '123456' WHERE staff_no IN ('T2024001', 'T2024002');
```

| 角色 | 账号 | 建议测试密码 | 说明 |
| --- | --- | --- | --- |
| 辅导员 | `T2024001` | `123456` | 张辅导 |
| 任课教师 | `T2024002` | `123456` | 李老师 |

如需管理员登录，需要在 `staff` 表中准备管理员账号，并在 `staff_role` 表中为该账号配置 `ADMIN` 角色。

## 核心功能

### 学生端

- 查看个人课表
- 发起课程请假或时间段请假
- 查看请假记录与详情
- 被退回后重新提交申请
- 参与课堂签到

### 辅导员端

- 查看待审批请假申请
- 同意、拒绝或退回学生请假
- 批量审批请假单
- 发起公假并批量生成请假记录
- 按班级查看请假统计
- 查询所管理班级、学生和课程安排

### 任课教师端

- 查看待确认的课程请假影响
- 确认学生请假对课程的影响
- 发起课堂签到
- 查看签到会话与签到详情
- 关闭签到会话
- 导出考勤数据
- 处理缺勤转请假或缺勤确认

### 管理员端

- 学期管理
- 班级管理
- 课程管理
- 开课信息管理
- 课表数据维护
- 学生选课关系维护

## 主要接口

### 认证

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/auth/login` | 登录 |
| `POST` | `/api/auth/logout` | 登出 |

登录请求示例：

```json
{
  "loginType": "STUDENT",
  "username": "20210001",
  "password": "123456"
}
```

教职工登录时 `loginType` 使用 `STAFF`，后端会根据 `staff_role` 返回具体 `roleCode`，例如 `COUNSELOR`、`TEACHER` 或 `ADMIN`。

### 请假

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/leaves/apply` | 学生发起请假 |
| `GET` | `/api/leaves/my` | 学生查看自己的请假记录 |
| `GET` | `/api/leaves/{id}/detail` | 查看请假详情 |
| `PUT` | `/api/leaves/{id}/resubmit` | 学生重新提交请假 |
| `GET` | `/api/leaves/pending/counselor` | 辅导员待办 |
| `POST` | `/api/leaves/{id}/counselor-approve` | 辅导员审批 |
| `POST` | `/api/leaves/counselor-approve/batch` | 辅导员批量审批 |
| `POST` | `/api/leaves/public/batch` | 辅导员发起公假 |
| `GET` | `/api/leaves/pending/teacher` | 教师待确认 |
| `POST` | `/api/leaves/impact/{impactId}/teacher-confirm` | 教师确认课程影响 |

### 课表与统计

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/timetable/student/day` | 查询学生某日课表 |
| `GET` | `/api/timetable/teacher/day` | 查询教师某日课表 |
| `GET` | `/api/stats/class-leave` | 查询班级请假统计 |
| `GET` | `/api/counselor/classes` | 查询辅导员管理班级 |
| `GET` | `/api/counselor/classes/{classId}/students` | 查询班级学生 |
| `GET` | `/api/counselor/offerings/by-term-class` | 查询班级开课信息 |

### 考勤

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/attendance/session/start` | 教师开启签到 |
| `POST` | `/api/attendance/session/{sessionId}/close` | 教师关闭签到 |
| `POST` | `/api/attendance/checkin` | 学生签到 |
| `GET` | `/api/attendance/teacher/sessions` | 查询教师签到会话 |
| `GET` | `/api/attendance/session/{sessionId}/detail` | 查询签到详情 |
| `GET` | `/api/teacher/attendance/export` | 导出教师考勤 |
| `POST` | `/api/absences/{absenceId}/convert-to-leave` | 缺勤转请假 |
| `POST` | `/api/absences/{absenceId}/confirm` | 确认缺勤 |

### 管理端

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET/POST/PUT/DELETE` | `/admin/terms` | 学期管理 |
| `GET/POST/PUT/DELETE` | `/admin/classes` | 班级管理 |
| `GET/POST/PUT/DELETE` | `/admin/courses` | 课程管理 |
| `GET/POST/PUT/DELETE` | `/admin/offerings` | 开课信息管理 |
| `GET/POST/PUT/DELETE` | `/admin/enrollments` | 选课关系管理 |

## 权限说明

后端通过 `Authorization: Bearer <token>` 识别登录用户。登录成功后，前端会将 `token`、`userId`、`displayName` 和 `roleCode` 存入 `localStorage`，后续请求由 Axios 拦截器自动带上 Token。

角色权限由 `@RequiresRoles` 注解控制：

- `STUDENT`：学生请假、查看个人课表、签到。
- `COUNSELOR`：审批请假、公假、班级统计、辅导员数据查询。
- `TEACHER`：确认请假影响、课堂签到、考勤导出。
- `ADMIN`：学期、班级、课程、开课、选课等基础数据维护。

## 运行测试

后端包含多组接口集成测试：

```bash
cd backend
./mvnw test
```

Windows PowerShell：

```powershell
cd backend
.\mvnw.cmd test
```

也可以使用 `backend/TestCase/scratch.http` 中的 HTTP 请求进行手动接口调试。

## 构建部署

### 前端构建

```bash
cd frontend
npm run build
```

构建产物默认输出到 `frontend/dist`。

### 后端打包

```bash
cd backend
./mvnw clean package
```

Windows PowerShell：

```powershell
cd backend
.\mvnw.cmd clean package
```

打包后可运行：

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## 常见问题

### 1. 前端请求 404 或跨域失败

确认后端已启动在 `8080` 端口，并且前端通过 Vite 开发服务访问。开发环境下 `/api` 和 `/admin` 会由 Vite 代理到后端。

### 2. 教师或辅导员无法登录

检查 `staff.password` 是否为空。初始化脚本中的教职工密码默认为 `NULL`，需要先设置测试密码。

### 3. 管理员页面无法正常使用

确认当前登录账号的 `staff_role.role_code` 为 `ADMIN`。初始化数据默认没有管理员账号，需要手动添加或修改角色。

### 4. 数据库连接失败

确认 MySQL 服务已启动，`leave_system` 数据库已创建，且 `application.properties` 中的用户名、密码和端口与本机一致。

## 相关文档

- `大学生请假信息管理平台需求文档.pdf`
- `校假通项目报告.pdf`
- `测试案例.xlsx`
- `backend/TestCase/scratch.http`
