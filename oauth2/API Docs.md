# API Document

## Auth Endpoint

### 1. Oauth Entry
- **URL:** `/oauth2/authorization/google`
- **Method:** `GET`
- **Description:** Google OAuth2 login entry
- **QueryString:** 
  - `redirect_uri` (Optional): Url for direct after google login success

## Token Management

### 1. Refresh Token
- **URL:** `/api/v1/token/refresh`
- **Method:** `POST`
- **Description:** Use refresh token retrieve new access token
- **RequestBody:**
  - `{'refreshToken': $refresh_token}`
- **Response: HTTP 201 Created**
  ```json
  {
    "access_token": "new-jwt-token",
    "token_type": "Bearer",
    "expires_in": 3600
  }
  ```

### 3. Revoke Token
- **URL:** `/api/v1/token/revoke`
- **Method:** `POST`
- **Description:** revoke current access token with refresh token
- **RequestBody:**
  - `{'refreshToken': $refresh_token}`
- **Response:** HTTP 204 No Content

## Error response

Every API endpoint will respond with same error format when error happened

```json
{
  "error": "boolean",
  "message": "Description about the error",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Security

1. Every API endpoint need to be access through HTTP or HTTPS
2. Every request need valid JWT Token except for login endpoint
3. Token will be invalid as follow：
   - Beyond the expire time
   - Revoked manually
   - User logout 

## Version control

Current API version：v1

- API URL prefix：`/api/v1/`

---

# TODO

## Rate Limited 速率限制
- **Unlimited for now.**
- 
  - 每個 IP 地址每分鐘最多 60 個請求
  - 超過限制將返回 429 Too Many Requests
  - 響應頭包含剩餘請求次數信息：
    - `X-RateLimit-Limit`
    - `X-RateLimit-Remaining`
    - `X-RateLimit-Reset`

