# custom-provider-simple

## 概要

このモジュールは、カスタム `EndpointGateProvider` Bean を実装し、Spring Profile で切り替える方法を示します。外部設定プロパティから読み取るプロバイダと、環境変数から読み取るプロバイダの 2 つが含まれています。

## このサンプルで確認できること

- **外部設定プロバイダ** -- カスタム `@ConfigurationProperties` からゲートの値を読み取る `EndpointGateProvider` の実装 (`ExternalConfigEndpointGateProvider`、`external-config` プロファイルで有効)。
- **環境変数プロバイダ** -- `EG_` プレフィックス付きの環境変数からゲートの値を読み取る `EndpointGateProvider` の実装 (`EnvironmentVariableEndpointGateProvider`、`env-variable` プロファイルで有効)。
- **プロファイルによるプロバイダ切り替え** -- `@Profile` を使用して、アクティブな Spring Profile に応じて 1 つのプロバイダ実装を選択する仕組み。

## 起動方法

### 外部設定プロファイル

```bash
./gradlew :examples:webmvc:custom-provider-simple:bootRun --args='--spring.profiles.active=external-config'
```

### 環境変数プロファイル

```bash
EG_DARK_MODE=true ./gradlew :examples:webmvc:custom-provider-simple:bootRun --args='--spring.profiles.active=env-variable'
```

`dark-mode` エンドポイントゲートを有効にするには、アプリケーション起動前に環境変数 `EG_DARK_MODE` を `true` に設定してください。

## エンドポイント

### external-config プロファイル

| エンドポイント | エンドポイントゲート | ゲートの値 | 期待される動作 |
|---|---|---|---|
| `GET /api/cloud` | `cloud-feature` | `true` | 200 -- レスポンスを返す |
| `GET /api/beta` | `beta-feature` | `false` | エンドポイントゲートによりブロック |

### env-variable プロファイル

| エンドポイント | エンドポイントゲート | 環境変数 | 期待される動作 |
|---|---|---|---|
| `GET /ui/dark-mode` | `dark-mode` | `EG_DARK_MODE` | `EG_DARK_MODE=true` の場合 200、それ以外はブロック |

## 設定

### application-external-config.yml

`external-config` プロファイルでは、カスタム設定プレフィックスの下にエンドポイントゲートの値を定義しています:

```yaml
external-endpoint-gates:
  gates:
    cloud-feature: true
    beta-feature: false
```

`ExternalConfigProperties` は `@ConfigurationProperties` で `external-endpoint-gates` プレフィックスにバインドし、`ExternalConfigEndpointGateProvider` にゲートのマップを提供します。

### 環境変数の命名規則

`EnvironmentVariableEndpointGateProvider` は、エンドポイントゲート名を以下のルールで環境変数キーに変換します:

1. `EG_` プレフィックスを付与する。
2. 大文字に変換する。
3. ハイフン (`-`) をアンダースコア (`_`) に置換する。

| エンドポイントゲート | 環境変数 |
|---|---|
| `dark-mode` | `EG_DARK_MODE` |
