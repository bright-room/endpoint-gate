# webmvc/health-indicator

`EndpointGateHealthIndicator` によるエンドポイントゲート状態の Spring Boot Actuator ヘルスエンドポイント表示のサンプルです。

## デモする機能

- `GET /actuator/health` — エンドポイントゲート詳細を含むヘルスチェック
- `GET /actuator/health/endpointGate` — エンドポイントゲート専用ヘルス

## 設定

```yaml
endpoint-gate:
  gates:
    active-feature:
      enabled: true
    inactive-feature:
      enabled: false
    rollout-feature:
      enabled: true
      rollout: 50

management:
  endpoints:
    web:
      exposure:
        include: health,endpoint-gates
  endpoint:
    health:
      show-details: always
```

## 期待されるヘルスレスポンス

```json
{
  "status": "UP",
  "components": {
    "endpointGate": {
      "status": "UP",
      "details": {
        "provider": "MutableInMemoryEndpointGateProvider",
        "totalGates": 3,
        "enabledGates": 2,
        "disabledGates": 1,
        "defaultEnabled": false
      }
    }
  }
}
```
