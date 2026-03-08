# functional-endpoint

## 概要

このモジュールは `endpoint-gate-spring-boot-starter` を Spring MVC の Functional Endpoints（`RouterFunction`）で使用する方法を示します。`EndpointGateHandlerFilterFunction` を使ってルートへのアクセスを制御します。

## このサンプルで確認できること

- **フルゲーティング** -- `endpointGateFilter.of("gate-id")` を使い、ルート全体を Endpoint Gate で制御する例 (`stableRoute`, `experimentalRoute`)。
- **ロールアウト付きゲーティング** -- `endpointGateFilter.of("gate-id", 50)` を使い、リクエストの一定割合に対してルートを有効にする例 (`betaRoute`)。
- **RouterFunction との統合** -- `RouterFunctions.route()` ビルダーチェーン内で Endpoint Gate フィルターを適用する例 (`RoutingConfiguration`)。

## 起動方法

```bash
./gradlew :examples:webmvc:functional-endpoint:bootRun
```

## エンドポイント

| エンドポイント | Endpoint Gate | ロールアウト | ゲートの値 | 期待される動作 |
|---|---|---|---|---|
| `GET /api/stable` | `stable-api` | 100% | `true` | 200 -- レスポンスを返す |
| `GET /api/beta` | `beta-api` | 50% | `true` | リクエストの約50%が成功（ランダム） |
| `GET /api/experimental` | `experimental-api` | 100% | `false` | Endpoint Gate によりブロック |

## 設定

### application.yml

```yaml
endpoint-gate:
  gates:
    stable-api:
      enabled: true
    beta-api:
      enabled: true
    experimental-api:
      enabled: false
```

## アノテーション方式との主な違い

- Endpoint Gate はアノテーション (`@EndpointGate`) ではなく、`EndpointGateHandlerFilterFunction` を `RouterFunction` のフィルターとして適用します。
- webmvc の `EndpointGateHandlerFilterFunction` は `RolloutPercentageProvider` を**使用しません** -- 設定ファイルのロールアウト値 (`endpoint-gate.gates.<name>.rollout`) は Functional Endpoints には適用されません。
- ロールアウト割合は `of(gateId, rollout)` パラメータでのみ指定できます。
