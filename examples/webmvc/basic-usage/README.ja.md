# basic-usage

## 概要

このモジュールは `endpoint-gate-spring-boot-starter` の基本的な使い方を示します。アノテーションの配置パターンと、Endpoint Gate が無効な場合のレスポンスタイプの設定を取り上げます。

## このサンプルで確認できること

- **クラスレベルのアノテーション** -- `@EndpointGate` をコントローラクラスに付与し、クラス内の全エンドポイントを単一のゲートで制御する例 (`GreetingController`)。
- **メソッドレベルのアノテーション** -- `@EndpointGate` を個々のハンドラメソッドに付与し、エンドポイントごとに制御する例。アノテーションのないエンドポイントは常にアクセス可能 (`UserController`)。
- **アノテーションの優先順位** -- クラスレベルとメソッドレベルの `@EndpointGate` が両方存在する場合、メソッドレベルが優先される (`LegacyController`)。
- **JSON レスポンスタイプ** -- 無効なゲートに対して JSON 形式のレスポンスを返す設定 (`JsonDemoController`)。
- **プレーンテキストレスポンスタイプ** -- 無効なゲートに対してプレーンテキスト形式のレスポンスを返す設定 (`PlainTextDemoController`)。
- **HTML レスポンスタイプ** -- 無効なゲートに対して HTML 形式のレスポンスを返す設定 (`HtmlDemoController`)。

## 起動方法

デフォルトプロファイルでアプリケーションを起動します:

```bash
./gradlew :examples:webmvc:basic-usage:bootRun
```

レスポンスタイプの動作を確認するには、プロファイルを指定して起動します:

```bash
# JSON レスポンス
./gradlew :examples:webmvc:basic-usage:bootRun --args='--spring.profiles.active=json-response'

# プレーンテキストレスポンス
./gradlew :examples:webmvc:basic-usage:bootRun --args='--spring.profiles.active=plain-text-response'

# HTML レスポンス
./gradlew :examples:webmvc:basic-usage:bootRun --args='--spring.profiles.active=html-response'
```

プロファイルを指定しない場合は、ライブラリのデフォルトのレスポンスタイプが使用されます。

## エンドポイント

### アノテーション配置

| エンドポイント | アノテーション | Endpoint Gate | ゲートの値 | 期待される動作 |
|---|---|---|---|---|
| `GET /hello` | クラスレベル | `greeting` | `true` | 200 -- レスポンスを返す |
| `GET /goodbye` | クラスレベル | `greeting` | `true` | 200 -- レスポンスを返す |
| `GET /users/search` | メソッドレベル | `new-search` | `true` | 200 -- レスポンスを返す |
| `GET /users/export` | メソッドレベル | `new-export` | `false` | Endpoint Gate によりブロック |
| `GET /users/list` | なし | -- | -- | 200 -- 常にアクセス可能 |
| `GET /legacy/data` | クラスレベル | `legacy-api` | `false` | Endpoint Gate によりブロック |
| `GET /legacy/special` | クラスレベル + メソッドレベル | `special-endpoint` | `true` | 200 -- メソッドレベルのゲートが優先 |

### レスポンスタイプ

以下のエンドポイントは全て Endpoint Gate が `false` に設定されているため、無効時のレスポンスが返されます。そのレスポンスの形式は、有効なプロファイルによって決まります。

| エンドポイント | Endpoint Gate | ゲートの値 |
|---|---|---|
| `GET /response/json` | `json-demo` | `false` |
| `GET /response/plain-text` | `plain-text-demo` | `false` |
| `GET /response/html` | `html-demo` | `false` |

## 設定

### application.yml

デフォルトの設定ファイルでは、各コントローラが使用する Endpoint Gate の値を定義しています:

```yaml
endpoint-gate:
  gates:
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
    json-demo:
      enabled: false
    plain-text-demo:
      enabled: false
    html-demo:
      enabled: false
```

### プロファイル別 YAML

各プロファイルは `endpoint-gate.response.type` を上書きし、Endpoint Gate が無効な場合のレスポンス形式を制御します:

- `application-json-response.yml` -- `endpoint-gate.response.type: JSON` を設定
- `application-plain-text-response.yml` -- `endpoint-gate.response.type: PLAIN_TEXT` を設定
- `application-html-response.yml` -- `endpoint-gate.response.type: HTML` を設定
