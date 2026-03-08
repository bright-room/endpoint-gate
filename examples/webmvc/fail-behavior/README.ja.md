# Fail Behavior Example

## 概要

このモジュールは、`endpoint-gate-spring-boot-starter` における **fail-closed**（デフォルト）と **fail-open** の動作を示します。設定に明示的に定義されていないエンドポイントゲートがどのように扱われるかを確認できます。

- **Fail-closed**（デフォルト）: 未定義のエンドポイントゲートは無効として扱われます。未知のゲートで保護されたエンドポイントへのアクセスはブロックされます。
- **Fail-open**: 未定義のエンドポイントゲートは有効として扱われます。ゲートが明示的に `false` に設定されていない限り、アクセスが許可されます。

## このサンプルで確認できること

- デフォルトの `fail-closed` ポリシー（`default-enabled: false`）における未定義エンドポイントゲートの動作。
- `default-enabled: true` を設定して `fail-open` ポリシーに切り替える方法。
- 各ポリシーにおける、明示的に定義されたゲートと未定義ゲートの違い。

## 実行方法

### Fail-closed（デフォルト）

```shell
./gradlew :examples:webmvc:fail-behavior:bootRun
```

### Fail-open

```shell
./gradlew :examples:webmvc:fail-behavior:bootRun --args='--spring.profiles.active=fail-open'
```

## エンドポイント

### FailClosedController（デフォルトプロファイル）

| エンドポイント              | エンドポイントゲート    | 定義済み | 期待される結果             |
|-----------------------|----------------------|---------|-----------------------------|
| `GET /fail-closed/known`   | `known-feature`      | はい（`true`）  | 200 OK -- アクセス許可   |
| `GET /fail-closed/unknown` | `undefined-feature`  | いいえ      | ブロック -- ゲートが未定義かつ `default-enabled` が `false` |

### FailOpenController（fail-open プロファイル）

| エンドポイント                     | エンドポイントゲート    | 定義済み | 期待される結果             |
|------------------------------|----------------------|---------|-----------------------------|
| `GET /fail-open/known-disabled` | `known-disabled`     | はい（`false`） | ブロック -- 明示的に無効 |
| `GET /fail-open/unknown`        | `undefined-feature`  | いいえ      | 200 OK -- ゲートは未定義だが `default-enabled` が `true`  |

## 設定

### デフォルトプロファイル（`application.yml`）

```yaml
endpoint-gate:
  gates:
    known-feature:
      enabled: true
```

`default-enabled` はデフォルトで `false` のため、`gates` に記載されていないエンドポイントゲートは無効として扱われます。

### Fail-open プロファイル（`application-fail-open.yml`）

```yaml
endpoint-gate:
  gates:
    known-disabled:
      enabled: false
  default-enabled: true
```

`default-enabled: true` を設定すると、`gates` に記載されていないエンドポイントゲートは有効として扱われます。
