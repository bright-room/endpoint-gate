# webmvc/actuator-endpoint

Spring Boot Actuator エンドポイントを使ったエンドポイントゲートのランタイム管理のサンプルです。

## デモする機能

- `GET /actuator/endpoint-gates` — 全ゲート一覧取得
- `GET /actuator/endpoint-gates/{gateId}` — 個別ゲート取得
- `POST /actuator/endpoint-gates` — ゲートの更新 (有効/無効 + ロールアウト割合)
- `DELETE /actuator/endpoint-gates/{gateId}` — ゲートの削除
- `EndpointGateChangedEvent` / `EndpointGateRemovedEvent` の `@EventListener` ハンドリング

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

## デモ手順

1. アプリを起動する
2. 全ゲート確認: `GET /actuator/endpoint-gates`
3. 個別ゲート確認: `GET /actuator/endpoint-gates/demo-feature`
4. `another-feature` を有効化: `POST /actuator/endpoint-gates` (ボディ: `{"gateId":"another-feature","enabled":true}`)
5. `another-feature` を削除: `DELETE /actuator/endpoint-gates/another-feature`
6. コンソールログでイベント発行を確認する
