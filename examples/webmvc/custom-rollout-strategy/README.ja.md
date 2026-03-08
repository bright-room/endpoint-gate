# custom-rollout-strategy

## 概要

このモジュールは、カスタム `RolloutStrategy` Bean を実装し、Spring Profile で切り替える方法を示します。リクエストヘッダーで指定したリージョンに基づいてアクセスを制御するストラテジーと、ビジネスアワー内のみロールアウトを許可するストラテジーの2つが含まれています。

## このサンプルで確認できること

- **リージョンベースのロールアウト** -- `X-Region` リクエストヘッダーでリージョンを取得し、リージョンごとに設定されたウェイトでロールアウト対象かを判定する `RolloutStrategy` の実装（`RegionBasedRolloutStrategy`、`region` プロファイルで有効）。
- **時間ベースのロールアウト** -- 09:00〜17:00 のビジネスアワー外はすべてのアクセスをブロックし、ビジネスアワー内は `DefaultRolloutStrategy` に委譲する `RolloutStrategy` の実装（`TimeBasedRolloutStrategy`、`time-based` プロファイルで有効）。
- **カスタムコンテキストリゾルバー** -- `X-Region` ヘッダーからリージョン識別子を取得して `EndpointGateContext` のユーザー識別子として設定する `EndpointGateContextResolver` の実装（`RegionContextResolver`、`region` プロファイルで有効）。
- **プロファイルによるストラテジー切り替え** -- `@Profile` を使用して、アクティブな Spring Profile に応じて1つの `RolloutStrategy` 実装を選択する仕組み。

## 起動方法

### デフォルト（ハッシュベースロールアウト）

```bash
./gradlew :examples:webmvc:custom-rollout-strategy:bootRun
```

### リージョンベースロールアウト

```bash
./gradlew :examples:webmvc:custom-rollout-strategy:bootRun --args='--spring.profiles.active=region'
```

### 時間ベースロールアウト

```bash
./gradlew :examples:webmvc:custom-rollout-strategy:bootRun --args='--spring.profiles.active=time-based'
```

## エンドポイント

| エンドポイント | エンドポイントゲート | ロールアウト | 期待される動作 |
|---|---|---|---|
| `GET /api/gradual` | `gradual-feature` | 60% | アクティブなロールアウトストラテジーによって異なる |

## ストラテジーの動作

### デフォルトプロファイル

組み込みの `DefaultRolloutStrategy`（SHA-256 ハッシュバケッティング）を使用します。デフォルトの `EndpointGateContextResolver` はリクエストごとにランダムな UUID を生成するため、ロールアウトは非スティッキーになります。

### `region` プロファイル

`RegionBasedRolloutStrategy` は各リージョンにウェイトを割り当て、ロールアウト割合と比較します:

| リージョン | ウェイト | 結果（rollout=60） |
|---|---|---|
| `us-east` | 0 | ロールアウト対象（0 < 60） |
| `eu-west` | 50 | ロールアウト対象（50 < 60） |
| `ap-northeast` | 80 | ロールアウト対象外（80 >= 60） |
| （その他） | 100 | ロールアウト対象外 |

`X-Region` ヘッダーでリージョンを指定します:

```bash
curl -H "X-Region: us-east" http://localhost:8080/api/gradual
curl -H "X-Region: ap-northeast" http://localhost:8080/api/gradual
```

`RegionContextResolver` は `X-Region` ヘッダーを読み取り、`RegionBasedRolloutStrategy` が参照する `EndpointGateContext` のユーザー識別子として設定します。

### `time-based` プロファイル

`TimeBasedRolloutStrategy` は 09:00〜17:00（サーバーローカル時刻）以外の時間帯はすべてのアクセスをブロックします。ビジネスアワー内では、設定されたロールアウト割合で `DefaultRolloutStrategy` に委譲します。

## 設定

### application.yml

```yaml
endpoint-gate:
  default-enabled: true
  gates:
    gradual-feature:
      enabled: true
      rollout: 60
```
