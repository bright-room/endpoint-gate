---
name: implement
description: 指定された Markdown（実装プラン等）を読み込み、内容に基づいてコードを実装する。ファイルパスで実装ソースを指定する。
argument-hint: "[markdown-file-path] [--branch <branch-name>]"
---

# Implement Skill

指定された Markdown ファイルを読み込み、その内容に基づいてコードを実装する。

## 前提条件

- `gh` CLI が認証済みであること（PR 作成時）
- コーディングガイドライン `.claude/rules/coding.md` に準拠すること

## 引数

```
$ARGUMENTS = <markdown-file-path> [--branch <branch-name>]
```

- `<markdown-file-path>`: 実装の元となる Markdown ファイルのパス（必須）
- `--branch <branch-name>`: 作業ブランチの指定（任意）

引数なしの場合はエラーとする。

### ブランチの動作

| 引数 | 動作 |
|------|------|
| `--branch` なし | Markdown の内容からブランチ名を自動生成し、`main` から新規ブランチを作成。実装完了後に PR を作成する |
| `--branch <existing-branch>` | 指定されたブランチにチェックアウトし、そのブランチ上で修正を実施。実装完了後に Push する（PR は作成しない） |

## 手順

### 1. 引数の解析

- `--branch` オプションがあればブランチ名を取得する
- 残りの引数から Markdown ファイルパスを取得する
- ファイルが存在しない場合はエラーメッセージを出力して終了する

### 2. 実装ソースの理解

指定された Markdown ファイルを読み込み、内容を深く理解する。

- **実装プランの場合**: Phase / Step の構成、対象ファイル、変更内容を把握する
- **レビュー指摘の場合**: 指摘事項、修正案、対象ファイル・行番号を把握する
- **その他の Markdown**: 記述された要件・仕様を把握する

### 3. プロジェクト構成とガイドラインの読み込み

以下を読み込み、実装の前提知識を把握する:

1. `.claude/rules/coding.md` — コーディングガイドライン
2. `settings.gradle.kts` — モジュール構成
3. 対象モジュールの `build.gradle.kts` — 適用されている convention plugin、依存関係、テスト構成（ソースセット）を把握する
4. `.claude/rules/architecture.md` が存在する場合 — アーキテクチャ上の制約

### 4. ブランチの準備

#### `--branch` なしの場合（新規実装）

1. `main` ブランチの最新を取得し、そこから新規ブランチを作成する
2. ブランチ名はソースの内容から自動生成する:
   - 実装プランの場合: `feat/<issue-number>-<概要のケバブケース>`（例: `feat/42-add-webflux-support`）
   - レビュー指摘修正の場合: `fix/<issue-number>-<概要のケバブケース>`
   - その他: `feat/<概要のケバブケース>`

#### `--branch` ありの場合（既存ブランチでの修正）

指定されたブランチにチェックアウトし、最新を pull する。

### 5. コードの実装

Markdown の内容に基づいてコードを実装する。

#### 実装時の注意事項

- `.claude/rules/coding.md` のコーディングガイドラインに厳密に準拠すること
- 既存のコードベースのパターン・命名規則に従うこと
- 変更対象モジュールに最も近い既存実装をリファレンスとし、ディレクトリ構成・クラス設計・テスト構成を踏襲すること
- 実装プランがある場合は Phase / Step の順序に従って段階的に実装すること
- 各ステップの実装後、対象モジュールのビルドが通ることを確認すること

#### Bean 登録時の確認事項

- ユーザー定義 Bean で上書き可能にすべきか検討し、必要に応じて `@ConditionalOnMissingBean` を適用する（既存の Auto-configuration クラスのパターンを参照）
- Auto-configuration クラスを追加した場合、`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` への登録を行う

#### 実装の進め方

1. **プロダクトコードの実装**: 新規クラスの作成、既存クラスの修正
2. **テストコードの実装**:
   - 対象モジュールの `build.gradle.kts` で `integration-test` plugin が適用されている場合、統合テストは `src/integrationTest/java` に配置する
   - 統合テストのインフラ（テスト用 Controller、Router、テスト用 AutoConfiguration 等）は同ソースセット内に既存のものがあればそのパターンに従う
   - ユニットテストは `src/test/java` に配置する
3. **設定ファイルの更新**: Auto-configuration 登録、プロパティメタデータなど
4. **ドキュメントの更新**: Javadoc、README、CLAUDE.md など（Markdown に記載がある場合）

### 6. ビルドとフォーマットの確認

実装完了後、以下を実行する:

```bash
# Google Java Format の適用
./gradlew spotlessApply

# 対象モジュールの部分ビルド（高速なフィードバックループ）
./gradlew :<module>:test
./gradlew :<module>:integrationTest  # integration-test plugin 適用モジュールのみ

# 全体ビルド（最終確認）
./gradlew check
```

- `spotlessApply` は必ずコミット前に実行すること
- 部分ビルドで失敗した場合は原因を特定し修正してから全体ビルドに進むこと
- `check` はユニットテストと統合テストの両方を含む

### 7. コミット

変更内容をコミットする。

- コミットメッセージは変更内容を適切に要約すること
- 実装プランの場合は Issue 番号をコミットメッセージに含めること
  - 例: `feat: add WebFlux support (#42)`
- レビュー指摘修正の場合は修正内容を簡潔に記載すること
  - 例: `fix: improve error handling and add missing tests`
- 複数の論理的なまとまりがある場合は、適切にコミットを分割すること
- Co-Authored-By には実行時のモデル情報を使用すること

```bash
git add <files>
git commit -m "$(cat <<'EOF'
<commit message>

Co-Authored-By: <実行中のモデル名> <noreply@anthropic.com>
EOF
)"
```

### 8. Push と PR 作成

#### `--branch` なしの場合（新規実装）

1. リモートに Push する

```bash
git push -u origin <branch-name>
```

2. PR を作成する。Issue を紐づける場合は **PR 本文** に `Closes #<issue-number>` を記載する。

```bash
gh pr create --title "<PR title>" --body "$(cat <<'EOF'
## Summary
<変更内容の箇条書き>

Closes #<issue-number>

## Test plan
<テスト方針のチェックリスト>

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

- PR タイトルは 70 文字以内に収めること
- PR の URL をユーザーに返すこと

#### `--branch` ありの場合（既存ブランチでの修正）

1. リモートに Push する
2. Push が完了した旨をユーザーに報告すること

## 注意事項

- Markdown ファイルの内容を正確に理解し、過不足のない実装を行うこと
- 推測ではなく、実際のコードを読んで確認した事実に基づいて実装すること
- 実装中に不明点や判断が必要な事項があればユーザーに確認すること
- ビルドが通らない状態でコミット・Push しないこと
- `spotlessApply` を忘れずに実行すること
- コーディングガイドライン違反がないことを実装中に常に確認すること
