# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build all modules
./gradlew build

# Run unit tests for a specific module
./gradlew :core:test
./gradlew :spring:webmvc:test

# Run integration tests for a specific module
./gradlew :spring:webmvc:integrationTest

# Run a single integration test class
./gradlew :spring:webmvc:integrationTest --tests "net.brightroom.endpointgate.spring.webmvc.EndpointGateInterceptorJsonResponseIntegrationTest"

# Run all checks (Spotless + unit tests + integration tests)
./gradlew check

# Apply Google Java Format
./gradlew spotlessApply
```

Code formatting uses Google Java Format via Spotless. Always run `spotlessApply` before committing, or the CI `check` task will fail.

## Architecture

Multi-module Gradle project (Java 25, Spring Boot 4.x) for endpoint gate support in Spring MVC / WebFlux. Published to Maven Central under group `net.bright-room.endpoint-gate`.

For full architecture details (modules, dependency graph, request flow, extension points, configuration reference), see @.claude/rules/architecture.md

## Coding Guidelines

コード実装時は @.claude/rules/coding.md を参照し、ガイドラインに準拠したコードを書くこと。

## Custom Skills

- `/plan <issue-number>` — GitHub Issue を参照して実装プランを作成する。ローカルではファイル出力、CI 環境では Issue コメントとして投稿
- `/implement <markdown-file-path> [--branch <branch-name>]` — 実装プランに基づいてコードを実装する。新規実装時は main からブランチを切り PR を作成、既存ブランチ指定時はそのブランチ上で修正・Push
- `/review [base-branch]` — 現在のブランチの変更に対するコードレビューを実施し、構造化された Markdown レポートとして出力

### Shared Skills

`.claude/skills/shared/` 配下のスキルは [claude-skills](https://github.com/bright-room/claude-skills) リポジトリからサブモジュール経由で提供される。利用可能なスキルの一覧は同リポジトリの README を参照。

## Contributing

PRs target `main`. PR titles should be prefixed with `Close #<IssueNumber>` when resolving an issue. See @.github/CONTRIBUTING.md for the full workflow.
