# Project Context

## コードベース調査ガイド

### モジュール構成の把握方法

- `settings.gradle.kts` を読み込み、現在のモジュール一覧を把握する
- `gradle-scripts/` 配下の convention plugin を確認し、利用可能なプラグイン（ビルド慣習、テスト構成等）を把握する

### 既存パターンの調査手順

- **同種の実装**: 要件に最も近い既存モジュールを特定し、そのディレクトリ構成・クラス設計・テスト構成をリファレンスとする
- **階層パターン**: 変更が core 層の SPI に影響する場合、対応する非同期 adapter 層（reactive-core 等）やフレームワーク層への波及を確認する
- **拡張ポイント**: Auto-configuration、SPI、Bean 登録条件（`@ConditionalOnMissingBean` 等）の活用箇所を確認する

### テスト構成の確認方法

- 対象モジュールの `build.gradle.kts` を読み込み、適用されている convention plugin からテスト構成（ソースセット、実行タスク）を把握する
- `integration-test` plugin が適用されている場合、`src/integrationTest/java` ソースセットが利用可能

## 実装ガイド

### ビルド・フォーマットコマンド

```bash
# Google Java Format の適用
./gradlew spotlessApply

# 対象モジュールの部分ビルド（高速なフィードバックループ）
./gradlew :<module>:test
./gradlew :<module>:integrationTest  # integration-test plugin 適用モジュールのみ

# 全体ビルド（最終確認）
./gradlew check
```

**注意**: `spotlessApply` は必ずコミット前に実行すること。部分ビルドで失敗した場合は原因を特定し修正してから全体ビルドに進むこと。`check` はユニットテストと統合テストの両方を含む。

### 言語固有の実装規約

- **Bean 登録**: ユーザー定義 Bean で上書き可能にすべきか検討し、必要に応じて `@ConditionalOnMissingBean` を適用する（既存の Auto-configuration クラスのパターンを参照）
- **Auto-configuration 登録**: Auto-configuration クラスを追加した場合、`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` への登録を行う
- **core 層の変更**: SPI の追加・変更は、対応する非同期 adapter 層（reactive-core 等）への対称的な変更が必要か検討する
- **新モジュールの追加**: `settings.gradle.kts` への追加、適用する convention plugin の選定、依存グラフ上の位置づけを明記する

### テスト配置ルール

- ユニットテスト: `src/test/java` に配置
- 統合テスト: `src/integrationTest/java` に配置（`integration-test` plugin 適用モジュールのみ）
- 統合テストのインフラ（テスト用 Controller、Router、テスト用 AutoConfiguration 等）は同ソースセット内に既存のものがあればそのパターンに従う

### CI に委ねてよい項目

なし（すべてローカルで実行可能）

## レビューガイド

### ファイルパス → カテゴリマッピング

| 変更ファイルのパスパターン | 選択されるカテゴリ |
|--------------------------|-------------------|
| `**/src/main/java/`（core 層モジュール） | アーキテクチャ, プロダクトコード |
| `**/src/main/java/`（フレームワーク層モジュール） | プロダクトコード |
| `**/src/test/java/` | テストコード |
| `**/src/integrationTest/java/` | テストコード |
| `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle-scripts/` | ビルド・設定 |
| `**/resources/META-INF/` | ビルド・設定 |
| `*.md`（`.claude/` 配下を除く） | ドキュメント |
| `.claude/skills/`, `.claude/rules/` | ドキュメント |

core 層モジュールかフレームワーク層モジュールかの判断は、`settings.gradle.kts` のモジュール定義と `.claude/rules/architecture.md` のモジュール依存グラフを参照して行う。

### カテゴリ別レビュー観点

#### architecture
- 設計パターン、モジュール分割、依存関係の適切さ
- **階層整合性**: core 層の SPI 変更に対し、非同期 adapter 層・フレームワーク層に対称的な変更があるか
- **Bean 登録パターン**: `@ConditionalOnMissingBean` によるユーザー定義 Bean での上書き可能性が維持されているか
- Auto-configuration の順序設計
- SPI / 拡張ポイントの一貫性

#### code
- ロジックの正確性、エッジケース
- 命名規則、コーディングガイドライン（`.claude/rules/coding.md`）への準拠
- 既存コードベースの慣習への準拠

#### test
- テストカバレッジ、テストケースの網羅性
- ユニットテスト / 統合テストの品質
- テストの独立性と再現性
- **ソースセット配置**: ユニットテストと統合テストが対象モジュールの convention plugin に応じた正しいソースセットに配置されているか

#### security
- 認証・認可の実装、入力バリデーション
- 機密情報管理: ハードコーディング、ログ出力
- CORS・ヘッダー設定、安全でないデシリアライゼーション
- 依存ライブラリの既知脆弱性

#### docs
- Javadoc: 公開 API のドキュメント（CG-2 準拠）
- README / CLAUDE.md の更新
- 設定プロパティの説明

#### build
- Gradle 設定の正確性、convention plugin の適切な適用
- Auto-configuration 登録ファイルの内容が実装と一致しているか
- `settings.gradle.kts` のモジュール登録
- プロパティメタデータ（`additional-spring-configuration-metadata.json`）の整合性

### セキュリティチェックリスト

| チェック項目 | 結果 | 備考 |
|-------------|:----:|------|
| SQLインジェクション対策 | ✅ / ❌ / N/A | |
| XSS対策 | ✅ / ❌ / N/A | |
| CSRF対策 | ✅ / ❌ / N/A | |
| 認証・認可の適切な実装 | ✅ / ❌ / N/A | |
| 機密情報のハードコーディング | ✅ / ❌ / N/A | |
| 機密情報のログ出力 | ✅ / ❌ / N/A | |
| 依存ライブラリの既知脆弱性 | ✅ / ❌ / N/A | |
| エラー情報の外部露出 | ✅ / ❌ / N/A | |
| 安全でないデシリアライゼーション | ✅ / ❌ / N/A | |
| CORS設定の適切さ | ✅ / ❌ / N/A | |
| セキュリティヘッダーの設定 | ✅ / ❌ / N/A | |
| 暗号化アルゴリズムの適切さ | ✅ / ❌ / N/A | |

### テストカバレッジマトリクス

| 対象クラス | メソッド/機能 | ユニットテスト | 統合テスト | 備考 |
|-----------|-------------|:-------------:|:---------:|------|

## プランテンプレート補足

### 影響範囲テーブル

<!-- settings.gradle.kts から読み取ったモジュール一覧に基づき、変更の影響があるモジュールのみ行を作成する -->

| モジュール | 影響 | 備考 |
|-----------|------|------|
| `<module-path>` | 新規 / 変更 | <概要> |

### ファイル構成の記述例

```
module/src/main/java/...
├── subpackage/
│   ├── NewClass.java          (new)
│   └── ExistingClass.java     (modify)
└── another/
    └── AnotherNewClass.java   (new)
```

### テスト戦略テーブル

| テスト種別 | ソースセット | 対象 | テスト内容 |
|-----------|-------------|------|-----------|
| ユニットテスト | `src/test/java` | `ClassName` | <テスト内容> |
| 統合テスト | `src/integrationTest/java` | `ClassName` | <テスト内容> |

### ドキュメント更新対象

| ドキュメント | 更新条件 |
|-------------|---------|
| `README.md` | 新機能・設定変更・公開 API 変更 |
| `CLAUDE.md` | コマンド変更 |
| `.claude/rules/architecture.md` | モジュール構成変更・依存グラフ変更・Auto-configuration 順序変更・拡張ポイント変更・設定プロパティ変更 |
| `.claude/rules/coding.md` | コーディング規約変更 |
| `<module>/src/main/resources/META-INF/additional-spring-configuration-metadata.json` | 設定プロパティ追加時 |
| `<module>/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | Auto-configuration 追加時 |
| `.claude/skills/references/project-context.md` | モジュール構成変更・ビルドコマンド変更・レビュー観点変更 |

## ラベル・ワークフロー規約

### Issue/PR ラベルの prefix

`Type:` prefix を使用する（例: `Type: Bug Fix`, `Type: Feature`）。
