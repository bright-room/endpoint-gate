# webflux/health-indicator

Spring WebFlux アプリケーションにおける、`EndpointGateHealthIndicator` によるエンドポイントゲート状態の Spring Boot Actuator ヘルスエンドポイントへの公開を示すサンプルです。

## 機能

- `GET /actuator/health` -- エンドポイントゲートの詳細を含むヘルスチェック
- `GET /actuator/health/endpointGate` -- エンドポイントゲート専用のヘルス情報

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

## エンドポイント

| エンドポイント | エンドポイントゲート | 有効 | 期待される動作 |
|---|---|---|---|
| `GET /api/active` | `active-feature` | `true` | 200 -- レスポンスを返す |
| `GET /api/inactive` | `inactive-feature` | `false` | エンドポイントゲートによりブロック |

## 期待されるヘルスレスポンス

```json
{
  "status": "UP",
  "components": {
    "endpointGate": {
      "status": "UP",
      "details": {
        "provider": "MutableInMemoryReactiveEndpointGateProvider",
        "totalGates": 3,
        "enabledGates": 2,
        "disabledGates": 1,
        "defaultEnabled": false
      }
    }
  }
}
```
