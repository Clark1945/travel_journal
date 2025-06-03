# 安裝和設定指南

## 前置需求
1. JDK 17 或更高版本
2. Redis 7.x
3. Docker（可選）
4. Google OAuth2 憑證

## 安裝步驟

### 1. 安裝 Redis
使用 Docker 安裝 Redis：
```bash
docker run -d \
  --name redis-auth-service \
  -p 6399:6379 \
  redis:7-alpine
```

驗證 Redis 安裝：
```bash
docker exec -it redis-auth-service redis-cli ping
```
應該返回 "PONG"

### 2. 配置 Google OAuth2

1. 訪問 [Google Cloud Console](https://console.cloud.google.com/)
2. 創建新專案或選擇現有專案
3. 啟用 OAuth2 API
4. 創建憑證（OAuth2 Client ID）
5. 設定授權的重定向 URI
6. 記下 Client ID 和 Client Secret

### 3. 配置應用程式

在 `src/main/resources/application.yml` 中配置以下內容：

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your-client-id
            client-secret: your-client-secret
            scope:
              - email
              - profile

  redis:
    host: localhost
    port: 6399

jwt:
  secret: your-jwt-secret
  expiration: 3600000  # 1小時
```

### 4. 建置與運行

使用 Gradle 建置專案：
```bash
./gradlew build
```

運行應用程式：
```bash
./gradlew bootRun
```

## 驗證安裝

1. 訪問 http://localhost:8080
2. 點擊登入按鈕
3. 應該重定向到 Google 登入頁面
4. 登入成功後應該重定向回應用程式

## 常見問題

### Redis 連接問題
- 確認 Redis 服務正在運行
- 確認端口映射正確
- 檢查防火牆設定

### Google OAuth2 問題
- 確認重定向 URI 正確配置
- 檢查 Client ID 和 Secret 是否正確
- 確認 API 已啟用

### JWT 相關問題
- 確保設定了有效的 JWT secret
- 檢查 token 過期時間設定

## 安全建議

1. 永遠不要在版本控制中提交敏感資訊
2. 使用環境變數或配置服務存儲敏感資訊
3. 在生產環境中使用強密碼和長的 JWT secret
4. 定期更新依賴包版本
5. 啟用 HTTPS

## 下一步

- 查看 [API 文件](../API%20Docs.md) 了解可用的端點
- 查看 [系統架構](ARCHITECTURE.md) 了解系統設計
- 配置日誌
- 設定監控
- 配置備份策略 