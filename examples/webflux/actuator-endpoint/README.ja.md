# webflux/actuator-endpoint

Spring WebFlux アプリケーションにおける、Spring Boot Actuator エンドポイントを使ったランタイムでのフィーチャーフラグ管理を示すサンプルです。

## 機能

- `GET /actuator/endpoint-gates` — 全ゲート一覧取得
- `GET /actuator/endpoint-gates/{gateId}` — 単一ゲート取得
- `POST /actuator/endpoint-gates` — ゲートの更新（有効/無効・ロールアウト割合・スケジュール）
- `DELETE /actuator/endpoint-gates/{gateId}` — ゲートの削除
- `EndpointGateChangedEvent` / `EndpointGateRemovedEvent` — `@EventListener` によるイベントハンドリング

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

## 動作の仕組み

`actuator` モジュールは `MutableInMemoryEndpointGateProvider` と
`MutableInMemoryRolloutPercentageProvider` を自動的に登録します。
これらは `@ConditionalOnMissingBean` により `webflux` モジュールのデフォルト（読み取り専用）プロバイダーを置き換えます。

内部的には `EndpointGateEndpoint` ではなく `ReactiveEndpointGateEndpoint` が使用されます。
API のリクエスト/レスポンス形式は WebMVC 版と同一です。

`EndpointGateEndpoint` は `/actuator/endpoint-gates` に公開され、アプリケーションを再起動せずにランタイムでゲートへの完全なCRUD操作を可能にします。

書き込みまたは削除のたびにイベントが発行されます:
- `EndpointGateChangedEvent`: `gateId()`、`enabled()`、`rolloutPercentage()`（変更されていない場合は null）
- `EndpointGateRemovedEvent`: `gateId()`（ゲートが実際に存在した場合のみ発行）

## デモ手順

1. アプリケーションを起動する
2. 全ゲートを一覧表示: `GET /actuator/endpoint-gates`
3. 単一ゲートを取得: `GET /actuator/endpoint-gates/demo-feature`
4. another-feature を有効化: `POST /actuator/endpoint-gates` にボディ `{"gateId":"another-feature","enabled":true}` を送信
5. another-feature を削除: `DELETE /actuator/endpoint-gates/another-feature`
6. demo-feature にスケジュールを設定する:
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
7. demo-feature のスケジュールを削除する:
   ```bash
   curl -X POST http://localhost:8080/actuator/endpoint-gates \
     -H "Content-Type: application/json" \
     -d '{
       "gateId": "demo-feature",
       "enabled": true,
       "removeSchedule": true
     }'
   ```
8. コンソールログで発行されたイベントを確認する
