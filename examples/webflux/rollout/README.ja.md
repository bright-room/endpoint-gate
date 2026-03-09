# webflux/rollout

## 概要

このモジュールは、Spring WebFlux における `endpoint-gate` の段階的ロールアウト機能を示します。アノテーションベースのロールアウト、設定ファイルベースのロールアウト、およびカスタム `ReactiveEndpointGateContextResolver` を使ったスティッキーロールアウトを取り上げます。

## このサンプルで確認できること

- **アノテーションベースのロールアウト** -- `@EndpointGate(value = "feature-name", rollout = 50)` を使い、リクエストの一定割合に対してフィーチャーを有効にする（`AnnotationRolloutController`）。
- **設定ファイルベースのロールアウト** -- `endpoint-gates.features.<name>.rollout` で設定ファイルからロールアウト割合を指定する（`ConfigRolloutController`）。
- **スティッキーロールアウト** -- ユーザー ID を使って決定論的なロールアウト判定を行うカスタム `ReactiveEndpointGateContextResolver` の実装（`StickyRolloutController`、`UserIdReactiveContextResolver`）。
- **ロールアウトの優先順位** -- 設定ファイルのロールアウト値はアノテーションのロールアウト値より優先される。`features` にフィーチャーが定義されている場合、`RolloutPercentageProvider` の値が使用され、アノテーションの `rollout` 属性は無視される。

## 起動方法

デフォルトプロファイル（ランダム・非スティッキーロールアウト）で起動:

```bash
./gradlew :webflux:rollout:bootRun
```

ユーザー ID ベースのスティッキーロールアウトを有効にするには:

```bash
./gradlew :webflux:rollout:bootRun --args='--spring.profiles.active=sticky'
```

> **注意:** `ReactiveEndpointGateContextResolver` はアプリケーション全体で1つの Bean です。`sticky` プロファイルを有効にすると、sticky-rollout エンドポイントだけでなく、すべてのロールアウトエンドポイントに影響します。

## エンドポイント

| エンドポイント | フィーチャーフラグ | ロールアウト | 指定元 | 期待される動作 |
|---|---|---|---|---|
| `GET /api/annotation-rollout` | `annotation-rollout` | 50% | アノテーション | リクエストの約50%が成功（ランダム） |
| `GET /api/config-rollout` | `config-rollout` | 30% | 設定ファイル | リクエストの約30%が成功（ランダム） |
| `GET /api/sticky-rollout` | `sticky-rollout` | 50% | アノテーション | ユーザーの約50%が成功（`sticky` プロファイルで決定論的） |

`sticky` プロファイルでは、`userId` クエリパラメータを渡すと決定論的な結果が得られます:

```bash
curl http://localhost:8080/api/sticky-rollout?userId=user-123
```

同じ `userId` は、同じフィーチャーに対して常に同じ結果を返します。

## 設定

### application.yml

```yaml
endpoint-gates:
  default-enabled: true
  features:
    config-rollout:
      enabled: true
      rollout: 30
```

- `default-enabled: true` により fail-open 動作となり、`features` に未定義のフィーチャー（`annotation-rollout` や `sticky-rollout`）も有効として扱われます。
- `annotation-rollout` は意図的に `features` に**定義していません** -- これにより、アノテーションの `rollout = 50` 属性が有効になります。もし定義した場合、設定ファイルのデフォルトロールアウト値 `100` がアノテーションの値を上書きしてしまいます。

### application-sticky.yml

`sticky` プロファイルを有効にすると、`UserIdReactiveContextResolver` が `ReactiveEndpointGateContextResolver` Bean として登録されます。これによりデフォルトのリゾルバーが置き換えられ、`userId` クエリパラメータに基づく決定論的なロールアウトが実現されます。

## ロールアウトの仕組み

- **`DefaultRolloutStrategy`** は SHA-256 ハッシュによるバケッティングを使用します: `hash(gateId:userIdentifier) % 100`。
- 同じ入力は常に同じ結果を返すため、スティッキーロールアウトが実現できます。
- デフォルトのリゾルバー（プロファイルなし）はリクエストごとにランダムな UUID を生成するため、ロールアウトは非スティッキーになります。
- **`UserIdReactiveContextResolver`**（`sticky` プロファイル）は `userId` クエリパラメータを使用するため、ユーザーごとに決定論的なロールアウトになります。
