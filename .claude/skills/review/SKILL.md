---
name: review
description: PR 番号を指定してコードレビューを実施し、インラインコメントとして投稿する。コードベース全体のレビューも可能（ローカル出力）。
argument-hint: "<pr-number> [--category <category>] [--full]"
---

# Code Review Skill

PR に対してコードレビューを実施し、指摘事項をインラインコメントとして GitHub 上に投稿する。コードベース全体レビューの場合はローカルファイルとして出力する。

## レビュー方針

- 行番号と**文脈情報（背景知識など）**を必ず含める
- レビュー結果はチームメンバーに共有されるため、細部まで入念に確認すること
- 書き始める前に深く考察し、すべての箇所を漏れなくチェックすること
- 事実に基づかない内容（ハルシネーション）を含めないこと

## 前提条件

- `gh` CLI が認証済みであること

## レビューモードの解決ルール

引数に応じて、レビューモードを以下の優先順で決定する:

| 条件 | モード | レビュー対象 | 出力先 |
|------|--------|-------------|--------|
| PR 番号を指定（数値のみ） | **PR レビュー** | PR の差分 | GitHub インラインコメント |
| `--full-codebase` 指定 | **コードベース全体レビュー** | main ブランチの全コード | ローカルファイル |

PR 番号が指定された場合は必ず PR レビューモードとなる。

## カテゴリ選択ルール

レビューするカテゴリを以下の優先順で決定する:

| 条件 | カテゴリ |
|------|---------|
| `--full` 指定 | 全6カテゴリ |
| `--category` 指定 | 指定されたカテゴリのみ |
| コードベース全体レビュー | 全6カテゴリ |
| PR レビュー（カテゴリ指定なし） | **変更ファイルから自動選択**（後述） |

### 変更ファイルからのカテゴリ自動選択（PR レビュー時のデフォルト）

変更されたファイルのパスに基づいて、レビューするカテゴリを自動で絞り込む。

core 層モジュールかフレームワーク層モジュールかの判断は、`settings.gradle.kts` のモジュール定義と `.claude/rules/architecture.md` のモジュール依存グラフを参照して行う。

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

- 1つのファイルが複数カテゴリにマッチする場合はすべて選択する
- **プロダクトコード**は PR レビュー時に常に含める（最低1カテゴリ）

### 使用可能なカテゴリ名

`--category` で指定可能な値（複数指定はカンマ区切り: `--category security,code`）:

| カテゴリ名 | 説明 |
|-----------|------|
| `architecture` | 設計パターン、モジュール分割、依存関係の適切さ |
| `code` | ロジックの正確性、エッジケース、命名規則、コーディングガイドラインへの準拠 |
| `test` | テストカバレッジ、テストケースの網羅性、テストの品質 |
| `security` | 認証・認可の実装、入力バリデーション、機密情報の取り扱い |
| `docs` | Javadoc の整備状況、README の更新、公開 API の説明 |
| `build` | Gradle 設定、Auto-configuration 登録、プロパティメタデータ |

## 手順

### 1. レビュー対象の特定

#### モード A: PR レビュー

引数に PR 番号が指定された場合。

##### 1-1. PR 情報の取得

```bash
# PR のベースブランチと最新コミット SHA を取得
gh pr view <pr-number> --json baseRefName,headRefOid,headRefName

# PR で変更されたファイル一覧を取得
gh api repos/{owner}/{repo}/pulls/<pr-number>/files --jq '.[].filename'
```

##### 1-2. レビュー済み判定と差分取得

前回レビュー以降の変更分のみをレビュー対象にする。

```bash
# 過去のレビュー一覧を取得
gh api repos/{owner}/{repo}/pulls/<pr-number>/reviews
```

- 自身（claude）の最新レビューの `commit_id`（レビュー時点の HEAD SHA）を特定する
- 自身のレビューが見つかった場合: `git diff <前回レビューSHA>..<現在のHEAD SHA>` で新規変更分のみを差分として取得する
- 自身のレビューが見つからない場合（初回レビュー）: ベースブランチからの全差分を対象とする

##### 1-3. Resolved 済みスレッドの除外

GraphQL API で Resolved 済みのレビュースレッドを取得し、既に解決済みの指摘を再度行わないようにする。

```bash
gh api graphql -f query='
{
  repository(owner: "{owner}", name: "{repo}") {
    pullRequest(number: <pr-number>) {
      reviewThreads(first: 100) {
        nodes {
          isResolved
          comments(first: 10) {
            nodes { body, path, line }
          }
        }
      }
    }
  }
}'
```

- `isResolved: true` のスレッドに含まれる指摘は、同じ内容を再度指摘しない
- bot コメント（CI 等）も除外する

#### モード B: コードベース全体レビュー（`--full-codebase` 指定）

- リポジトリ内のすべての Java ソースファイル（`*.java`）を読み込む
- テストファイル、設定ファイル（`build.gradle.kts`、`*.properties` 等）、ドキュメントも対象とする
- `build/`、`.gradle/` ディレクトリはレビュー対象外とする

### 2. カテゴリの決定

引数とモードに基づいて、レビューするカテゴリを決定する（「カテゴリ選択ルール」参照）。

### 3. コードの読解

以下を読み込み、レビューの前提知識を把握する:

1. `.claude/rules/coding.md` — コーディングガイドラインの各ルール（CG-*）を把握する
2. `settings.gradle.kts` — モジュール構成を把握する
3. `.claude/rules/architecture.md` が存在する場合 — アーキテクチャ上の制約を把握する

レビュー対象のファイルをすべて読み込み、内容を深く理解する。対象ファイルだけでなく、関連するファイル（呼び出し元、インターフェース、設定ファイルなど）も確認すること。

### 4. レビューの実施

選択されたカテゴリについてのみレビューを行う。各カテゴリの観点:

#### architecture（アーキテクチャ）
- 設計パターン、モジュール分割、依存関係の適切さ
- **階層整合性**: core 層の SPI 変更に対し、非同期 adapter 層・フレームワーク層に対称的な変更があるか
- **Bean 登録パターン**: `@ConditionalOnMissingBean` によるユーザー定義 Bean での上書き可能性が維持されているか
- Auto-configuration の順序設計（`.claude/rules/architecture.md` の Auto-configuration Registration 参照）
- SPI / 拡張ポイントの一貫性

#### code（プロダクトコード）
- ロジックの正確性、エッジケース
- 命名規則、**コーディングガイドライン（`.claude/rules/coding.md`）への準拠**
  - `.claude/rules/coding.md` に定義された各ガイドライン（CG-*）への違反を 1つずつ確認すること。ガイドラインの NG 例に該当するコードがないか精査する
- 既存コードベースの慣習への準拠

#### test（テストコード）
- テストカバレッジ、テストケースの網羅性
- ユニットテスト / 統合テストの品質
- テストの独立性と再現性
- **ソースセット配置**: ユニットテストと統合テストが対象モジュールの convention plugin に応じた正しいソースセットに配置されているか

#### security（セキュリティ）
- 認証・認可の実装、入力バリデーション
- 機密情報管理: ハードコーディング、ログ出力
- CORS・ヘッダー設定、安全でないデシリアライゼーション
- 依存ライブラリの既知脆弱性

#### docs（ドキュメント）
- Javadoc: 公開 API のドキュメント
- README / CLAUDE.md の更新
- 設定プロパティの説明

#### build（ビルド・設定）
- Gradle 設定の正確性、convention plugin の適切な適用
- Auto-configuration 登録ファイル（`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`）の内容が実装と一致しているか
- `settings.gradle.kts` のモジュール登録
- プロパティメタデータ（`additional-spring-configuration-metadata.json`）の整合性

### 5. レビュー結果の出力

#### モード A: PR レビュー — GitHub インラインコメントとして投稿

指摘事項を GitHub PR レビューとして1回の API コールで投稿する。

```bash
gh api repos/{owner}/{repo}/pulls/<pr-number>/reviews \
  --method POST \
  --input /tmp/review-payload.json
```

レビューペイロードの構造:

```json
{
  "commit_id": "<PR の最新コミット SHA>",
  "body": "<!-- claude:review -->\n<レビュー総括コメント>",
  "event": "COMMENT",
  "comments": [
    {
      "path": "src/main/java/Example.java",
      "line": 42,
      "side": "RIGHT",
      "body": "**[Critical]** <指摘内容>\n\n**背景**: <なぜ問題なのか>\n\n**修正案**:\n```java\n// 修正コード\n```"
    }
  ]
}
```

##### event の選択基準

| 条件 | event |
|------|-------|
| Critical の指摘がある | `REQUEST_CHANGES` |
| High 以下の指摘のみ | `COMMENT` |
| 指摘なし | `APPROVE` |

##### 総括コメントのフォーマット

```markdown
<!-- claude:review -->
## Code Review 総括

> レビュー日: YYYY-MM-DD
> レビューカテゴリ: <選択されたカテゴリ一覧>

### 総合評価

| 観点 | 評価 |
|------|------|
| <カテゴリ名> | ⭐⭐⭐⭐☆ |

### 指摘サマリ

| # | 優先度 | カテゴリ | 概要 |
|---|--------|----------|------|
| 1 | 🔴 Critical | <カテゴリ> | <概要> |
| 2 | 🟠 High | <カテゴリ> | <概要> |

---
🤖 *Reviewed by Claude Code*
```

##### インラインコメントの記述ルール

- 各コメントの先頭に優先度バッジを付ける: `**[Critical]**`, `**[High]**`, `**[Medium]**`, `**[Low]**`
- 背景（なぜ問題なのか）を必ず含める
- 修正案がある場合はコード例を含める
- `commit_id` には `gh pr view <pr-number> --json headRefOid --jq .headRefOid` で取得した最新コミット SHA を指定すること（行番号のズレを防ぐため）

##### API エラー時のフォールバック

API 呼び出しが失敗した場合は、ローカルファイルにフォールバック出力する。

- 出力先: `.claude/outputs/reviews/REVIEW-PR-<pr-number>.md`
- ユーザーに API エラーが発生した旨を報告する

#### モード B: コードベース全体レビュー — ローカルファイルとして出力

レビュー結果を `.claude/outputs/reviews/` ディレクトリにファイルとして出力する。

- ディレクトリが存在しない場合は作成すること
- ファイル名: `REVIEW-main-YYYY-MM-DD.md`

以下のフォーマットに従ってレビュー結果を生成する。**選択されたカテゴリのセクションのみ出力する。**

````markdown
# Code Review: main

> レビュー日: YYYY-MM-DD
> レビューカテゴリ: <選択されたカテゴリ一覧>

## 総合評価

| 観点 | 評価 |
|------|------|
| <カテゴリ名> | ⭐⭐⭐⭐☆ |

---

## <カテゴリ名>レビュー

### 🔴 Critical

#### C-1: <問題のタイトル>

**問題点**

> 📍 [`path/to/file.java:42`](path/to/file.java#L42)
> ```java
> // 該当コードの引用
> ```

**背景**

<!-- なぜこれが問題なのか、技術的な文脈情報 -->

**修正案**

```java
// 修正後のコード例
```

---

### 🟠 High
### 🟡 Medium
### 🟢 Low
````

### セキュリティチェックリスト（security カテゴリ選択時のみ出力）

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

### テストカバレッジマトリクス（test カテゴリ選択時のみ出力）

| 対象クラス | メソッド/機能 | ユニットテスト | 統合テスト | 備考 |
|-----------|-------------|:-------------:|:---------:|------|
| `ClassName` | `methodName()` | ✅ | ✅ | |
| `ClassName` | `methodName2()` | ✅ | ❌ | テスト未作成 |

## 指摘の記述ルール

- 推測ではなく、実際にコードを読んで確認した事実のみを記載すること
- 良い点（Good practices）があれば、総括コメントで簡潔に言及すること
- セキュリティレビューでは、推測による脆弱性指摘は行わず、コード上で確認できる事実のみを記載すること
- ドキュメントレビューでは、ドキュメントの「有無」だけでなく「内容の正確性」も確認すること
- Resolved 済みのスレッドと同じ内容の指摘は行わないこと
