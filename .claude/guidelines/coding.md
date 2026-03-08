# Coding Guidelines

このドキュメントはコード実装時およびコードレビュー時に参照されるコーディングガイドラインである。

---

## CG-1: 三項演算子の禁止

三項演算子（`condition ? a : b`）の使用を禁止する。可読性を優先し、`if-else` 文を使用すること。

### NG

```java
String result = isActive ? "active" : "inactive";
```

### OK

```java
String result;
if (isActive) {
  result = "active";
} else {
  result = "inactive";
}
```

### 理由

- 三項演算子はネストすると著しく可読性が低下する
- コードベース全体の一貫性を保つため、一律で `if-else` を使用する

---

## CG-2: Javadoc の記述

`public` および `protected` なクラス・メソッド・フィールドには Javadoc を記述すること。Javadoc コンパイル時に警告が出ない状態を維持する。

### チェック項目

- クラスレベルの `@author` タグは不要（Git で追跡可能なため）
- メソッドの `@param` タグ: すべてのパラメータに対して記述すること
- メソッドの `@return` タグ: `void` 以外の戻り値がある場合は記述すること
- メソッドの `@throws` / `@exception` タグ: チェック例外を throws する場合は記述すること
- ジェネリクスの `@param <T>` タグ: 型パラメータがある場合は記述すること
- Javadoc の最初の文（summary sentence）はピリオドまたは日本語の句点で終わること

### NG

```java
/**
 * Evaluates the gate.
 */
public GateResult evaluate(String gateName, RequestContext context) {
  // @param, @return が欠落 → javadoc 警告
}
```

### OK

```java
/**
 * Evaluates the gate for the given request context.
 *
 * @param gateName the name of the gate to evaluate
 * @param context the current request context
 * @return the evaluation result
 */
public GateResult evaluate(String gateName, RequestContext context) {
}
```

### 理由

- `javadoc` タスクの警告ゼロを維持することで、ドキュメントの品質を担保する
- 公開 API の利用者にとって、パラメータや戻り値の説明は不可欠である

---

## CG-3: ネストの深さの制限

条件分岐、ループ、ラムダ式、無名クラス等のネストは **3 階層以内** に抑えること。3 階層を超える場合はメソッド抽出やアーリーリターン等で構造を平坦化する。

### NG

```java
public void process(List<Item> items) {
  for (Item item : items) {                        // depth 1
    if (item.isActive()) {                          // depth 2
      item.getSubItems().forEach(sub -> {           // depth 3
        if (sub.isValid()) {                        // depth 4 — 超過
          execute(sub);
        }
      });
    }
  }
}
```

### OK (アーリーリターン + メソッド抽出)

```java
public void process(List<Item> items) {
  for (Item item : items) {
    if (!item.isActive()) {
      continue;
    }
    processSubItems(item.getSubItems());
  }
}

private void processSubItems(List<SubItem> subItems) {
  subItems.stream()
      .filter(SubItem::isValid)
      .forEach(this::execute);
}
```

### 理由

- ネストが深いコードは認知負荷が高く、バグの温床になる
- メソッド抽出により、各メソッドの責務が明確になりテスタビリティも向上する

---

## CG-4: 早期リターンの活用

メソッドの先頭でガード節（guard clause）を用いて異常系・境界条件を早期に `return` / `throw` し、正常系のネストを浅く保つこと。

### NG

```java
public Response handle(Request request) {
  if (request != null) {
    if (request.isAuthorized()) {
      if (request.hasPayload()) {
        return process(request);
      } else {
        throw new InvalidRequestException("No payload");
      }
    } else {
      throw new UnauthorizedException("Not authorized");
    }
  } else {
    throw new IllegalArgumentException("Request must not be null");
  }
}
```

### OK

```java
public Response handle(Request request) {
  if (request == null) {
    throw new IllegalArgumentException("Request must not be null");
  }
  if (!request.isAuthorized()) {
    throw new UnauthorizedException("Not authorized");
  }
  if (!request.hasPayload()) {
    throw new InvalidRequestException("No payload");
  }
  return process(request);
}
```

### 適用パターン

- **null チェック / バリデーション**: メソッド先頭で不正な引数を弾く
- **空コレクション**: 空の場合に早期リターンし、後続のループ処理を不要にする
- **状態チェック**: 前提条件を満たさない場合に即座に返す
- **ループ内**: `continue` で不要な反復をスキップする（CG-3 の例も参照）

### 理由

- 正常系のコードがインデントされずに読めるため、可読性が大幅に向上する
- 異常系が先頭にまとまることで、前提条件が一目で把握できる
- CG-3（ネスト制限）の達成手段として最も効果的である
