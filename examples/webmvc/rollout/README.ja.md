# rollout

## 概要

このモジュールは `endpoint-gate-spring-boot-starter` の段階的ロールアウト機能を示します。設定ファイルベースのロールアウト、およびカスタム `EndpointGateContextResolver` を使ったスティッキーロールアウトを取り上げます。

## このサンプルで確認できること

- **設定ファイルベースのロールアウト** -- `@EndpointGate("annotation-rollout")` をコントローラーに付与し、`endpoint-gate.gates.<name>.rollout` で設定ファイルからロールアウト割合を指定する例 (`AnnotationRolloutController`)。
- **設定ファイルベースのロールアウト** -- `endpoint-gate.gates.<name>.rollout` で設定ファイルからロールアウト割合を指定する例 (`ConfigRolloutController`)。
- **スティッキーロールアウト** -- ユーザー ID を使って決定論的なロールアウト判定を行うカスタム `EndpointGateContextResolver` の実装例 (`StickyRolloutController`, `UserIdContextResolver`)。
- **ロールアウトの設定** -- ロールアウト割合は常に `endpoint-gate.gates.<name>.rollout` で YAML に設定します。`@EndpointGate` アノテーションには `rollout` 属性がありません。設定ファイルの `RolloutPercentageProvider` の値が常に使用されます。

## 起動方法

デフォルトプロファイル（ランダム・非スティッキーロールアウト）でアプリケーションを起動します:

```bash
./gradlew :examples:webmvc:rollout:bootRun
```

ユーザー ID ベースのスティッキーロールアウトを有効にするには:

```bash
./gradlew :examples:webmvc:rollout:bootRun --args='--spring.profiles.active=sticky'
```

> **注意:** `EndpointGateContextResolver` はアプリケーション全体で1つの Bean です。`sticky` プロファイルを有効にすると、sticky-rollout エンドポイントだけでなく、全てのロールアウトエンドポイントに影響します。

## エンドポイント

| エンドポイント | Endpoint Gate | ロールアウト | 指定元 | 期待される動作 |
|---|---|---|---|---|
| `GET /api/annotation-rollout` | `annotation-rollout` | 50% | 設定ファイル | リクエストの約50%が成功（ランダム） |
| `GET /api/config-rollout` | `config-rollout` | 30% | 設定ファイル | リクエストの約30%が成功（ランダム） |
| `GET /api/sticky-rollout` | `sticky-rollout` | 50% | 設定ファイル | ユーザーの約50%が成功（`sticky` プロファイルで決定論的） |

`sticky` プロファイルでは、`userId` クエリパラメータを渡すと決定論的な結果が得られます:

```bash
curl http://localhost:8080/api/sticky-rollout?userId=user-123
```

同じ `userId` は、同じゲートに対して常に同じ結果を返します。

## 設定

### application.yml

```yaml
endpoint-gate:
  default-enabled: true
  gates:
    annotation-rollout:
      enabled: true
      rollout: 50
    config-rollout:
      enabled: true
      rollout: 30
    sticky-rollout:
      enabled: true
      rollout: 50
```

- `default-enabled: true` により fail-open 動作となり、`gates` に未定義のゲートも有効として扱われます。
- 3つのゲート（`annotation-rollout`、`config-rollout`、`sticky-rollout`）すべてのロールアウト割合が YAML で設定されています。`@EndpointGate` アノテーションには `rollout` 属性がありません -- ロールアウトは常に `endpoint-gate.gates.<name>.rollout` で指定します。

### application-sticky.yml

`sticky` プロファイルを有効にすると、`UserIdContextResolver` が `EndpointGateContextResolver` Bean として登録されます。これによりデフォルトの `RandomEndpointGateContextResolver` が置き換えられ、ユーザー ID に基づく決定論的なロールアウトが可能になります。

## ロールアウトの仕組み

- **`DefaultRolloutStrategy`** は SHA-256 ハッシュによるバケッティングを使用します: `hash(gateId:userIdentifier) % 100`。
- 同じ入力は常に同じ結果を返すため、スティッキーロールアウトが実現できます。
- **`RandomEndpointGateContextResolver`**（デフォルト）はリクエストごとにランダムな UUID を生成するため、ロールアウトは非スティッキーになります。
- **`UserIdContextResolver`**（sticky プロファイル）は `userId` クエリパラメータを使用するため、ユーザーごとに決定論的なロールアウトになります。
