# Error Handling Example

## 概要

このモジュールは、エンドポイントゲートによってアクセスが拒否された場合のエラーハンドリングをカスタマイズする方法を示します。デフォルトでは、`endpoint-gate-spring-boot-starter` は `EndpointGateAccessDeniedException` をスローします。このサンプルでは、`@ControllerAdvice` を使用した4つのアプローチを Spring プロファイルで切り替えて確認できます。

- **JSON エラーレスポンス**（`json-error` プロファイル）: error、gate、message フィールドを含む構造化された 403 JSON レスポンスを返します。
- **XML エラーレスポンス**（`xml-error` プロファイル）: Jackson XML を使用して、構造化された 403 XML レスポンスを返します。
- **Thymeleaf エラーページ**（`thymeleaf-error` プロファイル）: ゲート ID を表示する Thymeleaf エラーページをレンダリングします。
- **リダイレクト**（`redirect` プロファイル）: ユーザーを `/coming-soon` ページにリダイレクトします。

## このサンプルで確認できること

- `EndpointGateAccessDeniedException` をキャッチして JSON エラーレスポンスを返すカスタム `@ControllerAdvice` の実装方法。
- `EndpointGateAccessDeniedException` をキャッチして Jackson XML で XML エラーレスポンスを返すカスタム `@ControllerAdvice` の実装方法。
- `EndpointGateAccessDeniedException` をキャッチして Thymeleaf エラーページをレンダリングする `@ControllerAdvice` の実装方法。
- `EndpointGateAccessDeniedException` をキャッチして別のページにリダイレクトする `@ControllerAdvice` の実装方法。
- Spring の `@Profile` を使用して異なるエラーハンドリング戦略を切り替える方法。

## 実行方法

### JSON エラーレスポンス

```shell
./gradlew :examples:webmvc:error-handling:bootRun --args='--spring.profiles.active=json-error'
```

### XML エラーレスポンス

```shell
./gradlew :examples:webmvc:error-handling:bootRun --args='--spring.profiles.active=xml-error'
```

### Thymeleaf エラーページ

```shell
./gradlew :examples:webmvc:error-handling:bootRun --args='--spring.profiles.active=thymeleaf-error'
```

### リダイレクト

```shell
./gradlew :examples:webmvc:error-handling:bootRun --args='--spring.profiles.active=redirect'
```

## エンドポイント

### PremiumController

| エンドポイント | エンドポイントゲート | ゲート値 | 期待される結果 |
|----------|--------------|------------|-----------------|
| `GET /api/premium` | `premium-feature` | `false` | ブロック -- アクティブな `@ControllerAdvice` によって処理 |

### DashboardController

| エンドポイント | エンドポイントゲート | ゲート値 | 期待される結果 |
|----------|--------------|------------|-----------------|
| `GET /dashboard` | `new-dashboard` | `false` | ブロック -- アクティブな `@ControllerAdvice` によって処理 |

### ComingSoonController

| エンドポイント | エンドポイントゲート | ゲート値 | 期待される結果 |
|----------|--------------|------------|-----------------|
| `GET /coming-soon` | (なし) | -- | 200 OK -- 常にアクセス可能（リダイレクト先） |

### プロファイル別のエラーハンドリング動作

| プロファイル | ハンドラー | 動作 |
|---------|---------|----------|
| `json-error` | `CustomEndpointGateExceptionHandler` | `error`、`gate`、`message` フィールドを含む 403 JSON を返す |
| `xml-error` | `XmlEndpointGateExceptionHandler` | `<endpoint-gate-error>` ルート要素に `error`、`gate`、`message` 要素を含む 403 XML を返す |
| `thymeleaf-error` | `ThymeleafEndpointGateExceptionHandler` | `error/feature-unavailable` Thymeleaf テンプレートを 403 ステータスでレンダリング |
| `redirect` | `RedirectEndpointGateExceptionHandler` | `/coming-soon` にリダイレクト |

## 設定

### `application.yml`

```yaml
endpoint-gate:
  gates:
    premium-feature:
      enabled: false
    new-dashboard:
      enabled: false
```

両方のエンドポイントゲートが `false` に設定されているため、保護されたすべてのエンドポイントは `EndpointGateAccessDeniedException` をスローします。アクティブな `@ControllerAdvice`（Spring プロファイルによって決定）が例外の処理方法を決定します。
