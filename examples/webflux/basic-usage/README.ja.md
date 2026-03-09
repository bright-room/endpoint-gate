# webflux/basic-usage

## 概要

このモジュールは、Spring WebFlux における `endpoint-gate` の基本的な使い方を示します。`Mono` や `Flux` を返すリアクティブコントローラーでのアノテーション配置パターンを取り上げます。

## このサンプルで確認できること

- **クラスレベルのアノテーション** -- コントローラークラスに `@EndpointGate` を付与し、クラス内のすべてのエンドポイントを1つのフラグで制御する（`GreetingController`）。
- **メソッドレベルのアノテーション** -- ハンドラーメソッド個別に `@EndpointGate` を付与し、エンドポイントごとに制御する。アノテーションのないエンドポイントは常にアクセス可能（`UserController`）。
- **アノテーションの優先度** -- クラスレベルとメソッドレベルの両方に `@EndpointGate` がある場合、メソッドレベルが優先される（`LegacyController`）。
- **リアクティブな戻り値の型** -- `Mono<T>` や `Flux<T>` を返すメソッドでも、フィーチャーフラグの評価が透過的に動作する。

## 起動方法

```bash
./gradlew :webflux:basic-usage:bootRun
```

## エンドポイント

| エンドポイント | アノテーション | フィーチャーフラグ | フラグ値 | 期待される動作 |
|---|---|---|---|---|
| `GET /hello` | クラスレベル | `greeting` | `true` | 200 -- レスポンスを返す |
| `GET /hello/stream` | クラスレベル | `greeting` | `true` | 200 -- ストリーミングレスポンスを返す |
| `GET /users/search` | メソッドレベル | `new-search` | `true` | 200 -- レスポンスを返す |
| `GET /users/export` | メソッドレベル | `new-export` | `false` | フィーチャーフラグによりブロック |
| `GET /users/list` | なし | -- | -- | 200 -- 常にアクセス可能 |
| `GET /legacy/data` | クラスレベル | `legacy-api` | `false` | フィーチャーフラグによりブロック |
| `GET /legacy/special` | クラスレベル + メソッドレベル | `special-endpoint` | `true` | 200 -- メソッドレベルのフラグが優先 |

## 設定

### application.yml

```yaml
endpoint-gates:
  features:
    greeting:
      enabled: true
    new-search:
      enabled: true
    new-export:
      enabled: false
    legacy-api:
      enabled: false
    special-endpoint:
      enabled: true
```
