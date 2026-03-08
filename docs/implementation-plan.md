## 実装プラン

> 📋 Issue: #237
> 📅 作成日: 2026-03-08
> 📅 更新日: 2026-03-08

### 概要

プロジェクトを `feature-flag-spring-boot-starter` から `endpoint-gate` へ完全に移行する。新規リポジトリ `bright-room/endpoint-gate` に全機能を再実装し、旧リポジトリはアーカイブする。

Feature Flag、Conditional Access、Gradual Rollout、Schedule、Actuator、Health Indicator の全機能を移植する。#230（Pure Java core 分離）を同時に実施し、最初から `core`（Pure Java / JDK のみ）+ `reactive-core`（Reactor 依存）+ `spring-core`（Spring 依存）の6モジュール構成で構築する。

### 設計判断

#### 1. Evaluation Step の順序制御（`@Order` の代替）

現在 core モジュールの `EnabledEvaluationStep`、`ScheduleEvaluationStep`、`ConditionEvaluationStep`、`RolloutEvaluationStep`（および Reactive 版）は `@Order` アノテーション（Spring Framework）で評価順序を制御している。Pure Java core ではこの依存を排除する必要がある。

| アプローチ | メリット | デメリット |
|-----------|---------|-----------|
| A: `EvaluationPipeline` 内でハードコードした順序で呼び出す | シンプル、依存なし、順序が明確 | 柔軟性が低い（ステップ追加時にパイプライン変更が必要） |
| B: 各ステップに `int order()` メソッドを定義する独自 SPI | `@Order` と同等の柔軟性、Pure Java | 独自の仕組みを導入するコスト |
| C: `endpoint-gate-spring-core` で `@Order` を付与し、core では順序を規定しない | core が完全に Pure Java、Spring 側で自由に制御 | core 単体でのパイプライン実行時に順序が不定 |

**採用案**: **A（パイプライン内でハードコード）**。理由：
- 評価順序は Enabled → Schedule → Condition → Rollout で固定であり、変更する想定がない
- パイプラインクラスが順序を知っているのは責務として自然
- 外部からのステップ追加は spring-core 側で `@Order` を使って拡張可能

#### 2. Reactive クラスの配置先

現在の core には Reactor 依存（`compileOnly`）の Reactive クラスが 23 ファイルある。

| アプローチ | メリット | デメリット |
|-----------|---------|-----------|
| A: `core` に Reactor を `compileOnly` で残す | 現在と同じ構成、移行が容易 | Pure Java core に Reactor 型が混在。Reactor を使わない場合にも型が見える |
| B: Reactive クラスを `spring-core` に移動 | core が完全に Pure Java | spring-core が肥大化、Reactive SPI が Spring 層に属するのは不自然 |
| C: `reactive-core` を新設 | core が完全 Pure Java（JDK のみ）。将来 `coroutines-core` と対称的な構造 | モジュール数が1つ増加 |

**採用案**: **C（`reactive-core` を新設）**。理由：
- `core` が JDK のみの Pure Java になり、依存ゼロの意図が明確
- Kotlin/Ktor 対応時に `coroutines-core` を `reactive-core` と対称的に追加できる
- `spring-webflux` と `spring-actuator`（Reactive 部分）は `reactive-core` に依存するだけで、依存パスが自然

### 影響範囲

| モジュール | 移行先（ディレクトリ → artifactId） | 備考 |
|-----------|--------------------------------------|------|
| `core`（Pure Java 28ファイル） | `core/` → `core` | アノテーション、例外、SPI（sync）、Provider実装（sync）、Rollout（sync）、Properties POJO |
| `core`（Reactor 依存 23ファイル） | `reactive-core/` → `reactive-core` | Reactive SPI・InMemory実装、Reactive Evaluation、Reactive Rollout |
| `core`（Spring 依存 12ファイル） | `spring/core/` → `spring-core` | AutoConfiguration、SpEL評価、Event、ResponseBuilder、Properties バインディング |
| `webmvc` | `spring/webmvc/` → `spring-webmvc` | パッケージ名・クラス名変更 |
| `webflux` | `spring/webflux/` → `spring-webflux` | パッケージ名・クラス名変更、deprecated エイリアス除去 |
| `actuator` | `spring/actuator/` → `spring-actuator` | パッケージ名・クラス名・エンドポイントID変更、フィールド名変更 |
| `gradle-scripts` | `gradle-scripts/` | groupId・プロジェクト名変更、新規リポジトリに移植 |

### パッケージ構成

#### モジュール構成

```
endpoint-gate/                       ← rootProject.name = "endpoint-gate"
├── core/                            ← artifactId: core（Pure Java / JDK のみ）
├── reactive-core/                   ← artifactId: reactive-core（Reactor 依存、core に依存）
├── spring/
│   ├── core/                        ← artifactId: spring-core（Spring 依存、core に依存）
│   ├── webmvc/                      ← artifactId: spring-webmvc（spring-core に依存）
│   ├── webflux/                     ← artifactId: spring-webflux（spring-core + reactive-core に依存）
│   └── actuator/                    ← artifactId: spring-actuator（spring-core + reactive-core に依存）
└── gradle-scripts/                  ← Convention plugins（composite build）
```

**Maven 座標**:
- `net.bright-room.endpoint-gate:core`
- `net.bright-room.endpoint-gate:reactive-core`
- `net.bright-room.endpoint-gate:spring-core`
- `net.bright-room.endpoint-gate:spring-webmvc`
- `net.bright-room.endpoint-gate:spring-webflux`
- `net.bright-room.endpoint-gate:spring-actuator`

**モジュール依存グラフ**:

```
core
├── reactive-core
│   ├── spring-webflux
│   └── spring-actuator (ReactiveConfiguration)
└── spring-core
    ├── spring-webmvc
    ├── spring-webflux
    └── spring-actuator
```

**Gradle settings.gradle.kts**:

```kotlin
rootProject.name = "endpoint-gate"

include("core")
include("reactive-core")
include("spring:core")
include("spring:webmvc")
include("spring:webflux")
include("spring:actuator")

// ディレクトリパス → artifactId のマッピング
project(":spring:core").name = "spring-core"
project(":spring:webmvc").name = "spring-webmvc"
project(":spring:webflux").name = "spring-webflux"
project(":spring:actuator").name = "spring-actuator"
```

**モジュール間依存の参照**:

```kotlin
// reactive-core/build.gradle.kts
api(projects.core)
implementation(libs.reactor.core)

// spring/core/build.gradle.kts
api(projects.core)

// spring/webmvc/build.gradle.kts
api(projects.springCore)

// spring/webflux/build.gradle.kts
api(projects.springCore)
api(projects.reactiveCore)

// spring/actuator/build.gradle.kts
api(projects.springCore)
implementation(projects.reactiveCore)  // ReactiveConfiguration 用
```

#### `core`（Pure Java / JDK のみ）の配置

```
core/src/main/java/net/brightroom/endpointgate/core/
├── annotation/
│   └── EndpointGate.java                          (旧 FeatureFlag)
├── condition/
│   ├── ConditionVariables.java
│   ├── ConditionVariablesBuilder.java
│   └── EndpointGateConditionEvaluator.java         (旧 FeatureFlagConditionEvaluator, SPI)
├── context/
│   └── EndpointGateContext.java                    (旧 FeatureFlagContext)
├── evaluation/
│   ├── AccessDecision.java
│   ├── ConditionEvaluationStep.java
│   ├── EnabledEvaluationStep.java
│   ├── EndpointGateEvaluationPipeline.java         (旧 FeatureFlagEvaluationPipeline, 順序ハードコード)
│   ├── EvaluationContext.java
│   ├── EvaluationStep.java
│   ├── RolloutEvaluationStep.java
│   └── ScheduleEvaluationStep.java
├── exception/
│   └── EndpointGateAccessDeniedException.java      (旧 FeatureFlagAccessDeniedException)
├── properties/
│   ├── ConditionProperties.java
│   ├── GateProperties.java                         (旧 FeatureProperties)
│   ├── ResponseProperties.java
│   ├── ResponseType.java
│   └── ScheduleProperties.java
├── provider/
│   ├── ConditionProvider.java
│   ├── EndpointGateProvider.java                   (旧 FeatureFlagProvider, SPI)
│   ├── InMemoryConditionProvider.java
│   ├── InMemoryEndpointGateProvider.java            (旧 InMemoryFeatureFlagProvider)
│   ├── InMemoryRolloutPercentageProvider.java
│   ├── InMemoryScheduleProvider.java
│   ├── MutableConditionProvider.java
│   ├── MutableEndpointGateProvider.java             (旧 MutableFeatureFlagProvider, SPI)
│   ├── MutableInMemoryConditionProvider.java
│   ├── MutableInMemoryEndpointGateProvider.java     (旧 MutableInMemoryFeatureFlagProvider)
│   ├── MutableInMemoryRolloutPercentageProvider.java
│   ├── MutableRolloutPercentageProvider.java
│   ├── RolloutPercentageProvider.java
│   ├── Schedule.java
│   └── ScheduleProvider.java
├── resolution/
│   └── PlainTextResponseBuilder.java
└── rollout/
    ├── DefaultRolloutStrategy.java
    └── RolloutStrategy.java
```

#### `reactive-core`（Reactor 依存）の配置

```
reactive-core/src/main/java/net/brightroom/endpointgate/reactive/core/
├── condition/
│   └── ReactiveEndpointGateConditionEvaluator.java (旧 ReactiveFeatureFlagConditionEvaluator, SPI)
├── evaluation/
│   ├── ReactiveConditionEvaluationStep.java
│   ├── ReactiveEnabledEvaluationStep.java
│   ├── ReactiveEndpointGateEvaluationPipeline.java (旧 ReactiveFeatureFlagEvaluationPipeline, 順序ハードコード)
│   ├── ReactiveEvaluationStep.java
│   ├── ReactiveRolloutEvaluationStep.java
│   └── ReactiveScheduleEvaluationStep.java
├── provider/
│   ├── InMemoryReactiveConditionProvider.java
│   ├── InMemoryReactiveEndpointGateProvider.java    (旧 InMemoryReactiveFeatureFlagProvider)
│   ├── InMemoryReactiveRolloutPercentageProvider.java
│   ├── InMemoryReactiveScheduleProvider.java
│   ├── MutableInMemoryReactiveConditionProvider.java
│   ├── MutableInMemoryReactiveEndpointGateProvider.java (旧 MutableInMemoryReactiveFeatureFlagProvider)
│   ├── MutableInMemoryReactiveRolloutPercentageProvider.java
│   ├── MutableReactiveConditionProvider.java
│   ├── MutableReactiveEndpointGateProvider.java     (旧 MutableReactiveFeatureFlagProvider, SPI)
│   ├── MutableReactiveRolloutPercentageProvider.java
│   ├── ReactiveConditionProvider.java
│   ├── ReactiveEndpointGateProvider.java            (旧 ReactiveFeatureFlagProvider, SPI)
│   ├── ReactiveRolloutPercentageProvider.java
│   └── ReactiveScheduleProvider.java
└── rollout/
    ├── DefaultReactiveRolloutStrategy.java
    └── ReactiveRolloutStrategy.java
```

#### `spring-core`（Spring 依存）の配置

```
spring/core/src/main/java/net/brightroom/endpointgate/spring/core/
├── autoconfigure/
│   └── EndpointGateAutoConfiguration.java       (旧 FeatureFlagAutoConfiguration)
├── condition/
│   ├── SpelEndpointGateConditionEvaluator.java   (旧 SpelFeatureFlagConditionEvaluator)
│   └── SpelReactiveEndpointGateConditionEvaluator.java (旧 SpelReactiveFeatureFlagConditionEvaluator)
├── event/
│   ├── EndpointGateChangedEvent.java             (旧 FeatureFlagChangedEvent)
│   └── EndpointGateRemovedEvent.java             (旧 FeatureFlagRemovedEvent)
├── properties/
│   └── EndpointGateProperties.java               (旧 FeatureFlagProperties, @ConfigurationProperties)
└── resolution/
    ├── HtmlResponseBuilder.java
    └── ProblemDetailBuilder.java
```

#### `spring-webmvc` の配置

```
spring/webmvc/src/main/java/net/brightroom/endpointgate/spring/webmvc/
├── autoconfigure/
│   ├── EndpointGateMvcAutoConfiguration.java
│   └── EndpointGateMvcInterceptorRegistrationAutoConfiguration.java
├── condition/
│   └── HttpServletConditionVariables.java
├── context/
│   ├── EndpointGateContextResolver.java          (旧 FeatureFlagContextResolver)
│   └── RandomEndpointGateContextResolver.java    (旧 RandomFeatureFlagContextResolver)
├── exception/
│   └── EndpointGateExceptionHandler.java         (旧 FeatureFlagExceptionHandler)
├── filter/
│   └── EndpointGateHandlerFilterFunction.java    (旧 FeatureFlagHandlerFilterFunction)
├── interceptor/
│   └── EndpointGateInterceptor.java              (旧 FeatureFlagInterceptor)
└── resolution/
    ├── AccessDeniedInterceptResolution.java
    ├── AccessDeniedInterceptResolutionFactory.java
    ├── AccessDeniedInterceptResolutionViaHtmlResponse.java
    ├── AccessDeniedInterceptResolutionViaJsonResponse.java
    ├── AccessDeniedInterceptResolutionViaPlainTextResponse.java
    └── handlerfilter/
        ├── AccessDeniedHandlerFilterResolution.java
        ├── AccessDeniedHandlerFilterResolutionFactory.java
        ├── AccessDeniedHandlerFilterResolutionViaHtmlResponse.java
        ├── AccessDeniedHandlerFilterResolutionViaJsonResponse.java
        └── AccessDeniedHandlerFilterResolutionViaPlainTextResponse.java
```

#### `spring-webflux` の配置

```
spring/webflux/src/main/java/net/brightroom/endpointgate/spring/webflux/
├── aspect/
│   └── EndpointGateAspect.java                   (旧 FeatureFlagAspect)
├── autoconfigure/
│   └── EndpointGateWebFluxAutoConfiguration.java
├── condition/
│   └── ServerHttpConditionVariables.java
├── context/
│   ├── ReactiveEndpointGateContextResolver.java  (旧 ReactiveFeatureFlagContextResolver)
│   └── RandomReactiveEndpointGateContextResolver.java (旧 RandomReactiveFeatureFlagContextResolver)
├── exception/
│   └── EndpointGateExceptionHandler.java         (旧 FeatureFlagExceptionHandler)
├── filter/
│   └── EndpointGateHandlerFilterFunction.java    (旧 FeatureFlagHandlerFilterFunction)
└── resolution/
    ├── exceptionhandler/
    │   ├── AccessDeniedExceptionHandlerResolution.java
    │   ├── AccessDeniedExceptionHandlerResolutionFactory.java
    │   ├── AccessDeniedExceptionHandlerResolutionViaHtmlResponse.java
    │   ├── AccessDeniedExceptionHandlerResolutionViaJsonResponse.java
    │   └── AccessDeniedExceptionHandlerResolutionViaPlainTextResponse.java
    └── handlerfilter/
        ├── AccessDeniedHandlerFilterResolution.java
        ├── AccessDeniedHandlerFilterResolutionFactory.java
        ├── AccessDeniedHandlerFilterResolutionViaHtmlResponse.java
        ├── AccessDeniedHandlerFilterResolutionViaJsonResponse.java
        └── AccessDeniedHandlerFilterResolutionViaPlainTextResponse.java
```

#### `spring-actuator` の配置

```
spring/actuator/src/main/java/net/brightroom/endpointgate/spring/actuator/
├── autoconfigure/
│   └── EndpointGateActuatorAutoConfiguration.java
├── endpoint/
│   ├── EndpointGateEndpoint.java                 (旧 FeatureFlagEndpoint, @Endpoint(id="endpoint-gates"))
│   ├── EndpointGateEndpointResponse.java
│   ├── EndpointGatesEndpointResponse.java
│   ├── ReactiveEndpointGateEndpoint.java
│   └── ScheduleEndpointResponse.java
└── health/
    ├── EndpointGateHealthIndicator.java
    ├── EndpointGateHealthProperties.java
    ├── HealthDetailsContributor.java
    ├── ReactiveEndpointGateHealthIndicator.java
    └── ReactiveHealthDetailsContributor.java
```

#### 命名規則まとめ

| 項目 | 旧 | 新 |
|------|-----|-----|
| パッケージ（core） | `net.brightroom.featureflag.core` | `net.brightroom.endpointgate.core` |
| パッケージ（reactive-core） | — | `net.brightroom.endpointgate.reactive.core` |
| パッケージ（spring-core） | — | `net.brightroom.endpointgate.spring.core` |
| パッケージ（webmvc） | `net.brightroom.featureflag.webmvc` | `net.brightroom.endpointgate.spring.webmvc` |
| パッケージ（webflux） | `net.brightroom.featureflag.webflux` | `net.brightroom.endpointgate.spring.webflux` |
| パッケージ（actuator） | `net.brightroom.featureflag.actuator` | `net.brightroom.endpointgate.spring.actuator` |
| 設定プレフィックス | `feature-flags.*` | `endpoint-gate.*` |
| 設定マップキー | `feature-flags.features.*` | `endpoint-gate.gates.*` |
| Actuator エンドポイント | `/actuator/feature-flags` | `/actuator/endpoint-gates` |
| groupId | `net.bright-room.feature-flag-spring-boot-starter` | `net.bright-room.endpoint-gate` |

#### フィールド名変更

| 対象 | 旧フィールド名 | 新フィールド名 | 備考 |
|------|---------------|---------------|------|
| `EndpointGateEndpointResponse` | `featureName` | `gateId` | Actuator 単一ゲート応答 |
| `EndpointGatesEndpointResponse` | `features` | `gates` | Actuator 全ゲート応答のリスト |
| `EndpointGateChangedEvent` | `featureName` | `gateId` | ゲート変更イベント |
| `EndpointGateRemovedEvent` | `featureName` | `gateId` | ゲート削除イベント |
| `EndpointGateAccessDeniedException` | `featureName` | `gateId` | アクセス拒否例外 |
| `EndpointGateProperties` | `Map<String, GateProperties> features` | `Map<String, GateProperties> gates` | 設定バインディング |
| `EndpointGateEndpoint` POST body | `featureName` | `gateId` | Actuator 更新リクエスト |
| `EndpointGateHealthIndicator` details | `totalFlags` / `enabledFlags` / `disabledFlags` | `totalGates` / `enabledGates` / `disabledGates` | ヘルスチェック詳細 |
| `EndpointGateProperties` | `boolean defaultEnabled` | `boolean defaultEnabled` | 変更なし（汎用的な名前のため） |

#### YAML 設定変更

```yaml
# 旧
feature-flags:
  features:
    user-find:
      enabled: true
      rollout: 50
  default-enabled: false
  response:
    type: JSON
  condition:
    fail-on-error: true

# 新
endpoint-gate:
  gates:
    user-find:
      enabled: true
      rollout: 50
  default-enabled: false
  response:
    type: JSON
  condition:
    fail-on-error: true
```

#### Actuator レスポンス変更

```json
// 旧: GET /actuator/feature-flags
{
  "features": [
    { "featureName": "user-find", "enabled": true, "rollout": 50 }
  ],
  "defaultEnabled": false
}

// 新: GET /actuator/endpoint-gates
{
  "gates": [
    { "gateId": "user-find", "enabled": true, "rollout": 50 }
  ],
  "defaultEnabled": false
}

// 旧: GET /actuator/feature-flags/{featureName}
{ "featureName": "user-find", "enabled": true, "rollout": 50 }

// 新: GET /actuator/endpoint-gates/{gateId}
{ "gateId": "user-find", "enabled": true, "rollout": 50 }

// 旧: POST /actuator/feature-flags
{ "featureName": "user-find", "enabled": true, "rollout": 50 }

// 新: POST /actuator/endpoint-gates
{ "gateId": "user-find", "enabled": true, "rollout": 50 }
```

#### クラス名変更

全クラスの `FeatureFlag` → `EndpointGate` リネーム対象は旧プランのクラス名変更テーブルと同一。追加で `FeatureProperties` → `GateProperties`。

### 実装ステップ

#### Phase 1: プロジェクト基盤の構築

新規リポジトリ `bright-room/endpoint-gate` にビルド基盤を構築する。

##### Step 1: Gradle ラッパー・ルート設定の構築

**対象**: `settings.gradle.kts`, `build.gradle.kts`, `gradle/`

- Gradle Wrapper（現リポジトリと同じバージョン）をコピー
- `settings.gradle.kts` に6モジュールを定義:

```kotlin
rootProject.name = "endpoint-gate"

include("core")
include("reactive-core")
include("spring:core")
include("spring:webmvc")
include("spring:webflux")
include("spring:actuator")

// ディレクトリパス → artifactId のマッピング
project(":spring:core").name = "spring-core"
project(":spring:webmvc").name = "spring-webmvc"
project(":spring:webflux").name = "spring-webflux"
project(":spring:actuator").name = "spring-actuator"
```

- `gradle.properties`、`gradle/libs.versions.toml`（バージョンカタログ）をコピー

##### Step 2: Convention plugins の移植

**対象**: `gradle-scripts/`

現リポジトリの `gradle-scripts/` をコピーし、以下を変更:
- `spring-boot-starter.gradle.kts`: `group = "net.bright-room.endpoint-gate"`
- `publish-plugin.gradle.kts`: POM の URL・SCM・Issue Management を `bright-room/endpoint-gate` に変更

##### Step 3: 各モジュールの `build.gradle.kts` 作成

各モジュールの依存関係を定義:

```kotlin
// core/build.gradle.kts
plugins {
    id("spring-boot-starter")  // Java ツールチェーン等のみ利用（bootJar は無効）
    id("publish-plugin")
    id("spotless-java")
}
dependencies {
    testImplementation(libs.spring.boot.starter.test)
}

// reactive-core/build.gradle.kts
plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
}
dependencies {
    api(projects.core)
    implementation(libs.reactor.core)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.reactor.test)
}

// spring/core/build.gradle.kts
plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
}
dependencies {
    api(projects.core)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.web)
    compileOnly(libs.reactor.core)  // SpelReactiveEndpointGateConditionEvaluator 用
    testImplementation(libs.spring.boot.starter.test)
}

// spring/webmvc/build.gradle.kts
plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
    id("integration-test")
}
dependencies {
    api(projects.springCore)
    implementation(libs.spring.boot.starter.webmvc)
    // test dependencies...
}

// spring/webflux/build.gradle.kts
plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
    id("integration-test")
}
dependencies {
    api(projects.springCore)
    api(projects.reactiveCore)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.aspectj)
    // test dependencies...
}

// spring/actuator/build.gradle.kts
plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
    id("integration-test")
}
dependencies {
    api(projects.springCore)
    implementation(projects.reactiveCore)
    implementation(libs.spring.boot.starter.actuator)
    // test dependencies...
}
```

##### Step 4: CI 設定の移植

**対象**: `.github/workflows/`

現リポジトリの GitHub Actions ワークフローをコピーし、必要に応じて調整。

##### Step 5: ビルド確認

```bash
./gradlew build
```

空のモジュールでビルドが通ることを確認。

#### Phase 2: `core`（Pure Java / JDK のみ）の実装

現 `core` モジュールから Spring・Reactor 非依存の 28 ファイルを移植・リネームする。

##### Step 1: アノテーション・例外・コンテキスト

- `@FeatureFlag` → `@EndpointGate`
- `FeatureFlagAccessDeniedException` → `EndpointGateAccessDeniedException`（フィールド `featureName` → `gateId`）
- `FeatureFlagContext` → `EndpointGateContext`

##### Step 2: Provider SPI・実装（sync のみ）

すべての sync Provider インターフェースと InMemory 実装を移植・リネーム。`FeatureFlag` → `EndpointGate`。

##### Step 3: Condition SPI（sync のみ）

- `FeatureFlagConditionEvaluator` → `EndpointGateConditionEvaluator`（Pure Java SPI）
- `ConditionVariables`、`ConditionVariablesBuilder` をそのまま移植

##### Step 4: Rollout（sync のみ）

- `RolloutStrategy`、`DefaultRolloutStrategy` をそのまま移植

##### Step 5: Properties POJO

- `FeatureProperties` → `GateProperties`
- `ConditionProperties`、`ResponseProperties`、`ResponseType`、`ScheduleProperties` → そのまま
- `PlainTextResponseBuilder` → そのまま（文言の「Feature」→「Gate」更新は含む）

##### Step 6: Evaluation パイプライン（sync のみ）

- 各 `EvaluationStep` から `@Order` アノテーションを除去
- `FeatureFlagEvaluationPipeline` → `EndpointGateEvaluationPipeline`（ステップ順序をコンストラクタ/メソッド内でハードコード）

##### Step 7: テストの移植

現 `core/src/test/` から sync 対応するテストを移植・リネーム。

##### Step 8: ビルド確認

```bash
./gradlew :core:check
```

#### Phase 3: `reactive-core`（Reactor 依存）の実装

現 `core` モジュールから Reactor 依存の 23 ファイルを移植・リネームする。

##### Step 1: Reactive Provider SPI・実装

- `ReactiveFeatureFlagProvider` → `ReactiveEndpointGateProvider`
- `MutableReactiveFeatureFlagProvider` → `MutableReactiveEndpointGateProvider`
- `InMemoryReactiveFeatureFlagProvider` → `InMemoryReactiveEndpointGateProvider`
- その他 Reactive Provider（ConditionProvider, RolloutPercentageProvider, ScheduleProvider）を移植

##### Step 2: Reactive Condition SPI

- `ReactiveFeatureFlagConditionEvaluator` → `ReactiveEndpointGateConditionEvaluator`

##### Step 3: Reactive Rollout

- `ReactiveRolloutStrategy`、`DefaultReactiveRolloutStrategy` を移植

##### Step 4: Reactive Evaluation パイプライン

- `ReactiveFeatureFlagEvaluationPipeline` → `ReactiveEndpointGateEvaluationPipeline`（順序ハードコード）
- 各 `ReactiveEvaluationStep` から `@Order` を除去

##### Step 5: テストの移植

##### Step 6: ビルド確認

```bash
./gradlew :reactive-core:check
```

#### Phase 4: `spring-core`（Spring 依存）の実装

現 `core` モジュールから Spring 依存の 12 ファイルを移植・リネームする。

##### Step 1: AutoConfiguration

- `FeatureFlagAutoConfiguration` → `EndpointGateAutoConfiguration`
- `@ConfigurationProperties` バインディングを担当

##### Step 2: Properties バインディング

- `FeatureFlagProperties` → `EndpointGateProperties`（`@ConfigurationProperties(prefix = "endpoint-gate")`）
- フィールド `Map<String, FeatureProperties> features` → `Map<String, GateProperties> gates`
- `core` の POJO プロパティクラスを参照

##### Step 3: SpEL 条件評価

- `SpelFeatureFlagConditionEvaluator` → `SpelEndpointGateConditionEvaluator`
- `SpelReactiveFeatureFlagConditionEvaluator` → `SpelReactiveEndpointGateConditionEvaluator`
- `core` / `reactive-core` の SPI インターフェースを implements

##### Step 4: Event クラス

- `FeatureFlagChangedEvent` → `EndpointGateChangedEvent`（フィールド `featureName` → `gateId`）
- `FeatureFlagRemovedEvent` → `EndpointGateRemovedEvent`（フィールド `featureName` → `gateId`）

##### Step 5: レスポンスビルダー

- `ProblemDetailBuilder` → 移植（`type` URL を `endpoint-gate` リポジトリに変更）
- `HtmlResponseBuilder` → 移植（HTML テンプレートの文言更新）

##### Step 6: META-INF 設定

- `org.springframework.boot.autoconfigure.AutoConfiguration.imports`:
  ```
  net.brightroom.endpointgate.spring.core.autoconfigure.EndpointGateAutoConfiguration
  ```
- `additional-spring-configuration-metadata.json`: プレフィックスを `endpoint-gate.*` に変更、`feature-flags.features` → `endpoint-gate.gates`、各プロパティのパスも `endpoint-gate.gates.[*].*` に更新

##### Step 7: テストの移植

##### Step 8: ビルド確認

```bash
./gradlew :spring-core:check
```

#### Phase 5: `spring-webmvc` の実装

現 `webmvc` モジュールを移植・リネームする。依存先を `spring-core` に変更。

##### Step 1: Interceptor・Filter・ExceptionHandler

- `FeatureFlagInterceptor` → `EndpointGateInterceptor`
- `FeatureFlagHandlerFilterFunction` → `EndpointGateHandlerFilterFunction`
- `FeatureFlagExceptionHandler` → `EndpointGateExceptionHandler`

##### Step 2: AutoConfiguration

- `FeatureFlagMvcAutoConfiguration` → `EndpointGateMvcAutoConfiguration`
- `FeatureFlagMvcInterceptorRegistrationAutoConfiguration` → `EndpointGateMvcInterceptorRegistrationAutoConfiguration`
- `after =` 参照を `EndpointGateAutoConfiguration.class` に変更

##### Step 3: Context・Condition

- `FeatureFlagContextResolver` → `EndpointGateContextResolver`
- `RandomFeatureFlagContextResolver` → `RandomEndpointGateContextResolver`
- `HttpServletConditionVariables` → そのまま

##### Step 4: Resolution クラス

AccessDeniedInterceptResolution 系、handlerfilter 系をそのまま移植（名前に FeatureFlag を含まない）。

##### Step 5: META-INF 設定

```
net.brightroom.endpointgate.spring.webmvc.autoconfigure.EndpointGateMvcAutoConfiguration
net.brightroom.endpointgate.spring.webmvc.autoconfigure.EndpointGateMvcInterceptorRegistrationAutoConfiguration
```

##### Step 6: テスト・統合テストの移植

テスト内の `feature-flags.*` 設定値を `endpoint-gate.*` に変更。テストクラス名もリネーム。

##### Step 7: ビルド確認

```bash
./gradlew :spring-webmvc:check
```

#### Phase 6: `spring-webflux` の実装

現 `webflux` モジュールを移植・リネームする。`spring-core` + `reactive-core` に依存。deprecated エイリアス（`webflux.provider.InMemoryReactiveFeatureFlagProvider`）は除去。

##### Step 1: Aspect・Filter・ExceptionHandler

- `FeatureFlagAspect` → `EndpointGateAspect`
- `FeatureFlagHandlerFilterFunction` → `EndpointGateHandlerFilterFunction`
- `FeatureFlagExceptionHandler` → `EndpointGateExceptionHandler`

##### Step 2: AutoConfiguration

- `FeatureFlagWebFluxAutoConfiguration` → `EndpointGateWebFluxAutoConfiguration`

##### Step 3: Context・Condition・Resolution

Context、Condition、Resolution クラスを移植・リネーム。

##### Step 4: META-INF 設定・テスト

##### Step 5: ビルド確認

```bash
./gradlew :spring-webflux:check
```

#### Phase 7: `spring-actuator` の実装

現 `actuator` モジュールを移植・リネームする。`spring-core` + `reactive-core` に依存。

##### Step 1: Endpoint クラス

- `FeatureFlagEndpoint` → `EndpointGateEndpoint`（`@Endpoint(id = "endpoint-gates")`）
- `ReactiveFeatureFlagEndpoint` → `ReactiveEndpointGateEndpoint`
- レスポンス DTO のリネーム（`featureName` → `gateId`、`features` → `gates`）
- リクエスト DTO のリネーム（POST body の `featureName` → `gateId`）
- パスパラメータ `{featureName}` → `{gateId}`

##### Step 2: Health Indicator

- `FeatureFlagHealthIndicator` → `EndpointGateHealthIndicator`（details: `totalFlags` → `totalGates`、`enabledFlags` → `enabledGates`、`disabledFlags` → `disabledGates`）
- `ReactiveFeatureFlagHealthIndicator` → `ReactiveEndpointGateHealthIndicator`（同上）
- `FeatureFlagHealthProperties` → `EndpointGateHealthProperties`

##### Step 3: AutoConfiguration

- `FeatureFlagActuatorAutoConfiguration` → `EndpointGateActuatorAutoConfiguration`
- `after =` / `beforeName =` 参照を新クラス名に更新

##### Step 4: META-INF 設定・テスト

##### Step 5: ビルド確認

```bash
./gradlew :spring-actuator:check
```

#### Phase 8: ドキュメント・最終確認

##### Step 1: README.md の作成

新リポジトリの README.md を全面書き直し。`@EndpointGate` アノテーション、`endpoint-gate.*` 設定、新 Maven 座標でのインストール手順を記載。

##### Step 2: CLAUDE.md の作成

新リポジトリ用の CLAUDE.md を作成。6モジュール構成、新パッケージ名、新クラス名を反映。

##### Step 3: CONTRIBUTING.md の作成

##### Step 4: 全体ビルド確認

```bash
./gradlew check
```

##### Step 5: 旧リポジトリのアーカイブ

旧リポジトリ `bright-room/feature-flag-spring-boot-starter` の README に「このプロジェクトは `bright-room/endpoint-gate` に移行しました」と記載し、アーカイブする。

### ドキュメント更新

| ドキュメント | 更新内容 |
|-------------|---------|
| `README.md`（新リポジトリ） | プロジェクト名 `endpoint-gate`、`@EndpointGate` アノテーション、`endpoint-gate.*` 設定プレフィックス、新 Maven 座標（6モジュール）、Actuator エンドポイント `/actuator/endpoint-gates`、全コード例を新名前で記載 |
| `CLAUDE.md`（新リポジトリ） | 6モジュール構成（`core/` + `reactive-core/` + `spring/core/` + `spring/webmvc/` + `spring/webflux/` + `spring/actuator/`）、新パッケージ名・クラス名・設定プレフィックスを全面記載 |
| `additional-spring-configuration-metadata.json`（`spring/core`） | すべてのプロパティ名を `endpoint-gate.*` プレフィックスで定義（`endpoint-gate.gates.[*].*`） |
| `.github/CONTRIBUTING.md`（新リポジトリ） | 新リポジトリ名で記載 |
| `README.md`（旧リポジトリ） | 移行先リポジトリへの案内を追記 |

### テスト戦略

| テスト種別 | 対象 | テスト内容 |
|-----------|------|-----------|
| ユニットテスト | `core` | Provider SPI・InMemory 実装（sync）、Rollout ロジック（sync）、EvaluationPipeline の順序制御、Properties POJO のテスト。Spring・Reactor 非依存であることの検証 |
| ユニットテスト | `reactive-core` | Reactive Provider・InMemory 実装、Reactive EvaluationPipeline、Reactive Rollout のテスト |
| ユニットテスト | `spring-core` | SpEL 条件評価、Properties バインディング、Event 生成のテスト |
| ユニットテスト | `spring-webmvc` | Interceptor、ContextResolver、Resolution のテスト |
| ユニットテスト | `spring-webflux` | Aspect、HandlerFilterFunction のテスト |
| ユニットテスト | `spring-actuator` | Endpoint レスポンス（`gateId` フィールド）、Health Indicator（`totalGates` 等）のテスト |
| 統合テスト | `spring-webmvc` | `@EndpointGate` アノテーションによるエンドポイント制御の E2E テスト |
| 統合テスト | `spring-webflux` | Reactive 環境での E2E テスト |
| 統合テスト | `spring-actuator` | Actuator エンドポイント `/actuator/endpoint-gates` の CRUD テスト |
| ビルド検証 | 全体 | `./gradlew check` がすべてのモジュールで成功することを確認 |

### 今後の展望

- **examples モジュールの追加**: 旧 `feature-flag-spring-boot-starter-examples` リポジトリを廃止し、`bright-room/endpoint-gate` リポジトリ内に examples モジュールを作成する。ビルドには含めるがパブリッシュ対象外とする
- **`coroutines-core` モジュールの追加**: Kotlin coroutines 用の SPI（`suspend fun`）を定義する `coroutines-core` モジュールを `reactive-core` と対称的に追加。Ktor 対応の基盤となる
- **非 Spring フレームワーク対応**: `core` が JDK のみの Pure Java になったことで、`ktor/`、`jakarta-servlet/` 等のディレクトリを `spring/` と並列に追加可能
- **`endpoint-gate-micronaut` / `endpoint-gate-quarkus`**: 各フレームワークの DI・AOP 機構に合わせたアダプタモジュール

---
🤖 *Generated by Claude Code*
