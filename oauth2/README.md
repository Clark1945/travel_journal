# OAuth2 認證系統

## 系統簡介
這是一個基於 Spring Boot 實現的 OAuth2 認證系統，提供 Google 登入整合服務，特別適用於微服務架構。系統使用 Redis 作為 token 存儲，並實現了 JWT (JSON Web Token) 的支援。

## 主要功能
- Google 單點登入整合
- Token 管理（簽發、刷新、撤銷）
- 跨站存取支援（CORS）
- Redis 分布式 Session 管理
- JWT 整合

## 快速開始
1. [安裝指南](docs/SETUP.md)
2. [API 文件](API%20Docs.md)
3. [系統架構](docs/ARCHITECTURE.md)

## 系統需求
- JDK 17+
- Redis 7.x
- Docker（選用）

## 技術棧
- Spring Boot 3.5.0
- Spring Security
- Spring OAuth2 Client
- Redis
- JWT

## 特色
- 統一的登入入口
- 靈活的 Token 管理
- 微服務架構支援
- 完整的安全防護
- 可擴展的設計

## 授權
本專案採用 MIT 授權條款。

## 貢獻指南
歡迎提交 Issue 和 Pull Request。

## 聯絡方式
如有任何問題，請開啟 Issue 進行討論。 