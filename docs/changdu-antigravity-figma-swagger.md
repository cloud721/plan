# google Antigravity + figma + swagger协同

Source page: https://docs.changdu.vip/pages/viewpage.action?pageId=158725193

- Space: 研发中心 (`SPC1`)
- Page ID: `158725193`
- Version: `5`
- Last updated: `2026-04-15T17:32:22.970+08:00`
- Author shown by Confluence: 戴云飞

## Overview

协同 UI 设计及服务端接口完成开发，使用 Antigravity（Gemini 3.1 Pro）。

## 1. 简单服务接口设计及 Swagger 接口说明文档生成

开一个 agent，生成一个简单本地服务，实现一个简单账号密码登录接口，并生成 Swagger doc 说明文档。

Referenced attachment:

- `image2026-3-27_18-43-50.png`

## 2. 生成 App 测试工程，协同 Figma 生成主题页面

创建另一个 agent，导入一个空应用，创建新的 agent 导入该工程，实现一个简单 App：登录页登录后进入主页面，初版 UI 较粗糙。

Figma 设计有通用模板可以使用，并调通 Antigravity 调用 Figma。

Figma link:

https://www.figma.com/design/0GHD76v1mojjy4fTxCHijF/newte?node-id=1-3091&p=f&t=8dDjQSoIcZgDvNAq-0

Referenced attachment:

- `image2026-3-27_18-47-37.png`

启动后登录页面使用 Sign in 页；进入主页面后包含 Ecommerce、Booking、Activity 等 tab 切换。

## 3. 协同 Swagger 生成接口调用完成登录

Antigravity 指示登录页面读取本地 Swagger：

http://localhost:3000/api-docs

根据登录接口，实现当前登录页面的接口调用。

本地模拟器调试登录失败后，代码中请求域名改成：

http://192.168.21.35:3000/login

接口调用成功。

整体 AI 生成过程顺畅，速度很快，约 1 小时。

最终效果附件：

- `20260327185535.mp4`

## Additional Note

原文提到本想继续体验 Google Stitch 做 UI 设计，但国内账号不支持。

Referenced article:

https://mp.weixin.qq.com/s/niuJbumnvqvfZBR6DGX9Cw

## Local Code Mapping

- Android client: `client-android/`
- Login implementation: `client-android/app/src/main/java/com/example/simplewebview/LoginActivity.kt`
- Mock server: `server/server.js`
- OpenAPI JSON: `swagger/openapi.json`

## Confluence Attachments

The source page lists these attachments:

| Filename | Media Type | Size |
| --- | --- | ---: |
| `20260327185535.mp4` | `video/mp4` | 5,192,495 bytes |
| `image2026-3-27_18-47-37.png` | `image/png` | 654,683 bytes |
| `image2026-3-27_18-43-50.png` | `image/png` | 71,942 bytes |
| `image2026-3-27_18-42-1.png` | `image/png` | 325,536 bytes |

