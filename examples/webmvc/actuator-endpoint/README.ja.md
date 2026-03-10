# webmvc/actuator-endpoint

Spring Boot Actuator エンドポイントを使ったエンドポイントゲートのランタイム管理のサンプルです。

## デモする機能

- `GET /actuator/endpoint-gates` — 全ゲート一覧取得
- `GET /actuator/endpoint-gates/{gateId}` — 個別ゲート取得
- `POST /actuator/endpoint-gates` — ゲートの更新 (有効/無効 + ロールアウト割合 + スケジュール)
- `DELETE /actuator/endpoint-gates/{gateId}` — ゲートの削除
- `EndpointGateChangedEvent` / `EndpointGateRemovedEvent` / `EndpointGateScheduleChangedEvent` の `@EventListener` ハンドリング

## 設定

```yaml
endpoint-gate:
  gates:
    demo-feature:
      enabled: true
      rollout: 80
    another-feature:
      enabled: false

management:
  endpoints:
    web:
      exposure:
        include: endpoint-gates,health
```

## 仕組み

`actuator` モジュールは `MutableInMemoryEndpointGateProvider` と `MutableInMemoryRolloutPercentageProvider` を自動登録します。
これらは `@ConditionalOnMissingBean` により `webmvc` モジュールのデフォルト読み取り専用プロバイダーより優先されます。

`EndpointGateEndpoint` が `/actuator/endpoint-gates` に公開され、アプリを再起動せずにランタイムでゲートのCRUD操作が可能です。

イベントは書き込み・削除のたびに発行されます:
- `EndpointGateChangedEvent`: `gateId()`, `enabled()`, `rolloutPercentage()` (変更なしの場合 null)
- `EndpointGateRemovedEvent`: `gateId()` (ゲートが実際に存在した場合のみ発行)
- `EndpointGateScheduleChangedEvent`: `gateId()`, `schedule()` (スケジュール削除時は null)

## デモ手順

1. アプリを起動する
2. 全ゲート確認: `GET /actuator/endpoint-gates`
3. 個別ゲート確認: `GET /actuator/endpoint-gates/demo-feature`
4. `another-feature` を有効化: `POST /actuator/endpoint-gates` (ボディ: `{"gateId":"another-feature","enabled":true}`)
5. `another-feature` を削除: `DELETE /actuator/endpoint-gates/another-feature`
6. `demo-feature` にスケジュールを設定する ([スケジュール管理](#スケジュール管理) 参照)
7. コンソールログでイベント発行を確認する

## スケジュール管理

`POST /actuator/endpoint-gates` はスケジュール関連フィールドも受け付けます: `scheduleStart`、`scheduleEnd`、`scheduleTimezone`、`removeSchedule`。

### スケジュールの設定

```bash
curl -X POST http://localhost:8080/actuator/endpoint-gates \
  -H "Content-Type: application/json" \
  -d '{
    "gateId": "demo-feature",
    "enabled": true,
    "scheduleStart": "2026-03-10T09:00:00",
    "scheduleEnd": "2026-03-10T18:00:00",
    "scheduleTimezone": "Asia/Tokyo"
  }'
```

### スケジュールの更新

スケジュールの設定は部分更新ではなく**全置換**です。更新時は3つのスケジュールフィールドをすべて指定してください。

```bash
curl -X POST http://localhost:8080/actuator/endpoint-gates \
  -H "Content-Type: application/json" \
  -d '{
    "gateId": "demo-feature",
    "enabled": true,
    "scheduleStart": "2026-04-01T00:00:00",
    "scheduleEnd": "2026-04-30T23:59:59",
    "scheduleTimezone": "Asia/Tokyo"
  }'
```

### スケジュールの削除

```bash
curl -X POST http://localhost:8080/actuator/endpoint-gates \
  -H "Content-Type: application/json" \
  -d '{
    "gateId": "demo-feature",
    "enabled": true,
    "removeSchedule": true
  }'
```

### スケジュール付きゲートの取得

```bash
curl http://localhost:8080/actuator/endpoint-gates
```

```json
{
  "gates": [
    {
      "gateId": "demo-feature",
      "enabled": true,
      "rollout": 80,
      "condition": null,
      "schedule": {
        "start": "2026-03-10T09:00:00",
        "end": "2026-03-10T18:00:00",
        "timezone": "Asia/Tokyo",
        "active": true
      }
    }
  ]
}
```

### 注意事項

- スケジュール外のアクセスは `503 Service Unavailable` と `Retry-After` ヘッダーで応答されます。
- スケジュールの設定・削除どちらも `EndpointGateScheduleChangedEvent` を発行します。削除時は `schedule()` が `null` を返します。
- `scheduleTimezone` のみ指定して `scheduleStart` または `scheduleEnd` を省略すると `400 Bad Request` になります。
