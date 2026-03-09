# webflux/error-handling

## 概要

このモジュールは、Spring WebFlux アプリケーションでフィーチャーフラグによるアクセス拒否時のエラーハンドリングをカスタマイズする方法を示します。デフォルトでは `endpoint-gate` が `EndpointGateAccessDeniedException` をスローします。このサンプルでは Spring Profile で切り替える3つのアプローチを示します。

- **JSON エラーレスポンス**（`json-error` プロファイル）: `@ControllerAdvice` により構造化された 403 JSON レスポンスを返す。
- **リダイレクト**（`redirect` プロファイル）: `@ControllerAdvice` により `/api/coming-soon` にリダイレクトする。
- **Functional エンドポイントのエラー解決**（`functional-error` プロファイル）: `AccessDeniedHandlerFilterResolution` によりファンクショナルエンドポイントで 403 JSON レスポンスを返す。

## このサンプルで確認できること

- `EndpointGateAccessDeniedException` をキャッチして JSON エラーレスポンスを返す `@ControllerAdvice` の実装方法。
- `EndpointGateAccessDeniedException` をキャッチして別のページにリダイレクトする `@ControllerAdvice` の実装方法。
- `EndpointGateHandlerFilterFunction` を使ったファンクショナルエンドポイントのアクセス拒否レスポンスをカスタマイズする `AccessDeniedHandlerFilterResolution` の実装方法。
- Spring の `@Profile` を使用して異なるエラーハンドリング戦略を切り替える方法。

## 起動方法

### JSON エラーレスポンス

```bash
./gradlew :webflux:error-handling:bootRun --args='--spring.profiles.active=json-error'
```

### リダイレクト

```bash
./gradlew :webflux:error-handling:bootRun --args='--spring.profiles.active=redirect'
```

### Functional エンドポイントのエラー解決

```bash
./gradlew :webflux:error-handling:bootRun --args='--spring.profiles.active=functional-error'
```

## エンドポイント

### PremiumController（アノテーションベース）

| エンドポイント | フィーチャーフラグ | フラグ値 | 期待される結果 |
|---|---|---|---|
| `GET /api/premium` | `premium-feature` | `false` | ブロック -- アクティブなエラーハンドラーによって処理 |
| `GET /api/coming-soon` | （なし） | -- | 200 OK -- 常にアクセス可能（リダイレクト先） |

### ファンクショナルルート（`functional-error` プロファイルのみ）

| エンドポイント | フィーチャーフラグ | フラグ値 | 期待される結果 |
|---|---|---|---|
| `GET /functional/premium` | `premium-feature` | `false` | ブロック -- `CustomHandlerFilterResolution` によって処理 |

### プロファイル別のエラーハンドリング動作

| プロファイル | ハンドラー | 動作 |
|---|---|---|
| `json-error` | `CustomExceptionHandler`（`@ControllerAdvice`） | `error`、`feature`、`message` フィールドを含む 403 JSON を返す |
| `redirect` | `RedirectExceptionHandler`（`@ControllerAdvice`） | `/api/coming-soon` にリダイレクト（302） |
| `functional-error` | `CustomHandlerFilterResolution` | `AccessDeniedHandlerFilterResolution` によりファンクショナルルートで 403 JSON を返す |

## 設定

### application.yml

```yaml
endpoint-gates:
  features:
    premium-feature:
      enabled: false
```

`premium-feature` が `false` に設定されているため、保護されたすべてのエンドポイントは `EndpointGateAccessDeniedException` をスローします。アクティブなエラーハンドラー（Spring Profile によって決定）がレスポンスの内容を決定します。
