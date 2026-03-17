# 旧 POKKE アプリ 機能仕様書

> 元リポジトリ: [kumano-dormitory/pokke-app-android](https://github.com/kumano-dormitory/pokke-app-android)
> 技術: Java / Android SDK 30 / SQLite / OkHttp

---

## 1. アプリ概要

熊野寮事務室で使用するタブレット用荷物管理アプリ。
事務当番が荷物の受け取り・引き渡し・棚卸しを行い、データをLAN内PCサーバーと同期する。

---

## 2. 画面構成・遷移図

```
┌─────────────────────────────────┐
│         MainActivity            │
│         （メイン画面）            │
│                                 │
│  [事務当交代] [受け取り] [引き渡し] │
│  [泊まり事務当] [QRスキャン]      │
│  [更新] [旧型ノート] [管理用画面]  │
│                                 │
│  ┌─────────────────────────┐    │
│  │   イベントログ (直近100件)  │    │
│  └─────────────────────────┘    │
└──────────┬──────────────────────┘
           │
     ┌─────┼──────┬──────────┬──────────┬────────────┐
     ▼     ▼      ▼          ▼          ▼            ▼
  事務当  受取り   引渡し    泊まり事務当  旧型ノート  管理用画面
  交代    登録    Release   NightDuty   OldNote    Others
                    │                              (PW制)
              ┌─────┼─────┐
              ▼     ▼     ▼
           通常   代理   QR引渡
```

---

## 3. データベース設計

### 3.1 parcels テーブル（荷物）

| カラム | 型 | 説明 |
|---|---|---|
| `uid` | TEXT PK | UUID |
| `owner_uid` | TEXT | 所有者（寮生）UID |
| `owner_room_name` | TEXT | 所有者の部屋名 |
| `owner_ryosei_name` | TEXT | 所有者の名前 |
| `register_datetime` | TEXT | 登録日時 (yyyy-MM-dd HH:mm:ss) |
| `register_staff_uid` | TEXT | 登録した事務当番UID |
| `register_staff_room_name` | TEXT | 登録した事務当番の部屋 |
| `register_staff_ryosei_name` | TEXT | 登録した事務当番の名前 |
| `placement` | INTEGER | 荷物種別（後述） |
| `fragile` | INTEGER | 壊れ物フラグ (0/1) |
| `is_released` | INTEGER | 引渡済 (0/1) |
| `release_agent_uid` | TEXT | 代理受取人UID |
| `release_datetime` | TEXT | 引渡日時 |
| `release_staff_uid` | TEXT | 引渡した事務当番UID |
| `release_staff_room_name` | TEXT | 引渡した事務当番の部屋 |
| `release_staff_ryosei_name` | TEXT | 引渡した事務当番の名前 |
| `checked_count` | INTEGER | 泊まり事務当チェック回数 |
| `is_lost` | INTEGER | 紛失フラグ (0/1/10/11) |
| `lost_datetime` | TEXT | 最終確認日時 |
| `is_returned` | INTEGER | 返送フラグ |
| `returned_datetime` | TEXT | 返送日時 |
| `is_operation_error` | INTEGER | 操作ミスフラグ |
| `operation_error_type` | INTEGER | エラー種別コード |
| `note` | TEXT | 備考（「その他」の説明 / 新入寮生の面接番号） |
| `is_deleted` | INTEGER | 論理削除フラグ |
| `sharing_status` | INTEGER | 同期状態 (10=未同期, 30=同期済) |
| `sharing_time` | TEXT | 最終同期日時 |

#### placement（荷物種別）

| 値 | 種別 | 備考 |
|---|---|---|
| 0 | 普通 | 通常の荷物 |
| 1 | 冷蔵 | 冷蔵庫保管 |
| 2 | 冷凍 | 冷凍庫保管 |
| 3 | 大型 | 大型荷物 |
| 4 | 不在票 | 不在票のみ |
| 5 | その他 | noteに説明（200字以内） |
| 6 | 新入寮生 | noteに面接番号（1-3桁） |

### 3.2 ryosei テーブル（寮生）

| カラム | 型 | 説明 |
|---|---|---|
| `uid` | TEXT PK | UUID |
| `room_name` | TEXT | 部屋番号 |
| `ryosei_name` | TEXT | 名前（漢字） |
| `ryosei_name_kana` | TEXT | 名前（かな） |
| `ryosei_name_alphabet` | TEXT | 名前（ローマ字） |
| `block_id` | INTEGER | ブロックID（後述） |
| `slack_id` | TEXT | Slack ユーザーID |
| `status` | INTEGER | 状態（後述） |
| `parcels_current_count` | INTEGER | 現在の未引渡荷物数 |
| `parcels_total_count` | INTEGER | 累計受取荷物数 |
| `parcels_total_waittime` | TEXT | 累計待機時間 |
| `last_event_id` | INTEGER | 最終イベントID |
| `last_event_datetime` | TEXT | 最終イベント日時 |
| `created_at` | TEXT | 作成日時 |
| `updated_at` | TEXT | 更新日時 |
| `sharing_status` | INTEGER | 同期状態 |
| `sharing_time` | TEXT | 最終同期日時 |

#### block_id（ブロック）

| 値 | ブロック |
|---|---|
| 1 | A1棟 |
| 2 | A2棟 |
| 3 | A3棟 |
| 4 | A4棟 |
| 5 | B12棟 |
| 6 | B3棟 |
| 7 | B4棟 |
| 8 | C12棟 |
| 9 | C34棟 |
| 10 | 臨時キャパ |

#### status（寮生状態）

| 値 | 状態 |
|---|---|
| 1 | 通常（本人確認済） |
| 5 | 本人未確認 |
| 10 | 退寮済 |

### 3.3 parcel_event テーブル（イベントログ）

| カラム | 型 | 説明 |
|---|---|---|
| `uid` | TEXT PK | UUID |
| `created_at` | TEXT | イベント発生日時 |
| `event_type` | INTEGER | イベント種別（後述） |
| `parcel_uid` | TEXT | 関連荷物UID |
| `ryosei_uid` | TEXT | 関連寮生UID |
| `room_name` | TEXT | 発生時の部屋名 |
| `ryosei_name` | TEXT | 発生時の寮生名 |
| `target_event_uid` | TEXT | 関連イベントUID（引渡→登録の参照等） |
| `note` | TEXT | 備考 |
| `is_after_fixed_time` | INTEGER | 確定時間後フラグ |
| `is_finished` | INTEGER | 確定済 (0=取消可, 1=確定済) |
| `is_deleted` | INTEGER | 論理削除フラグ |
| `sharing_status` | INTEGER | 同期状態 |
| `sharing_time` | TEXT | 最終同期日時 |

#### event_type（イベント種別）

| 値 | 種別 | 説明 |
|---|---|---|
| 1 | 登録 | 荷物受け取り登録 |
| 2 | 引渡 | 荷物引き渡し |
| 3 | 削除 | 操作取り消し |
| 4 | 発見 | 紛失荷物の発見 |
| 5 | 荷物チェック | 泊まり事務当での確認 |
| 10 | 事務当交代 | 事務当番の交代 |
| 11 | 泊まり入り | 泊まり事務当の開始 |
| 12 | 泊まり出 | 泊まり事務当の終了 |
| 20 | 本人確認 | QRによる本人確認完了 |

---

## 4. 機能詳細

### 4.1 事務当番交代 (JimutoChange)

**目的**: 事務当番に入る際に担当者を登録する

**フロー**:
1. ブロック一覧 → 部屋一覧 → 寮生一覧（3カラムレイアウト）
2. 寮生を選択すると即座に事務当番が交代
3. event_type=10 のイベントが記録される
4. メイン画面に現在の事務当番名が表示される

**制約**:
- 退寮済（status=10）の寮生は選択不可

### 4.2 荷物受け取り登録 (Register)

**目的**: 業者から届いた荷物を登録する

**前提条件**: 事務当番が設定済みであること

**フロー**:
1. ブロック → 部屋 → 寮生を選択（検索も可能）
2. 荷物種別を選択（普通/冷蔵/冷凍/大型/不在票/その他/新入寮生）
3. 「その他」→ 説明文入力（200字以内、CJK/英数字のみ）
4. 「新入寮生」→ 面接番号入力（1-3桁）+ 数量選択（1-8個）
5. 登録実行

**登録時の処理**:
- parcels テーブルに INSERT（sharing_status=10）
- ryosei の parcels_current_count, parcels_total_count を +1
- parcel_event に event_type=1 を INSERT

**検索機能**: 漢字・ひらがな・カタカナ・ローマ字・部屋番号で検索可能

### 4.3 荷物引き渡し (Release)

**目的**: 寮生に荷物を渡す

**前提条件**: 事務当番が設定済みであること

#### 4.3.1 通常引き渡し

**フロー**:
1. ブロック → 部屋 → 寮生を選択
2. 未引渡荷物があるか確認（なければエラー表示）
3. **本人確認ダイアログ**（学生証・免許証・橙食券等の確認）が表示される
4. 引渡す荷物をチェックボックスで選択（デフォルト: 全選択）
5. 「引き渡し」ボタンで実行

#### 4.3.2 代理引き渡し

**フロー**:
1. 「代理引き渡し」トグルをON（背景がピンクに変化）
2. 代理人を寮生一覧から選択
3. 荷物の所有者を選択
4. 引渡す荷物を選択（デフォルト: **全て未選択** — 明示的な選択が必要）
5. release_agent_uid に代理人UIDが記録される

**引渡時の処理**:
- parcels の is_released=1, release_datetime, release_staff_* を更新
- ryosei の parcels_current_count を -1
- parcel_event に event_type=2 を INSERT（target_event_uid で登録イベントと紐付け）
- 元の登録イベントの is_finished=1 に更新

### 4.4 QRコード引き渡し

**目的**: Slack通知のQRコードで簡易引き渡し

**フロー**:
1. メイン画面の QR ボタン → カメラ起動（ZXing）
2. QR コードをスキャン → UUID を取得
3. parcels テーブルで UUID を検索:
   - **1件ヒット & 未引渡**: 引渡ダイアログを表示
   - **1件ヒット & 引渡済**: エラー表示
   - **0件**: ryosei テーブルを検索 → 本人確認フローへ
   - **2件以上**: QR重複エラー

#### QRによる本人確認

- ryosei の status=5（未確認）の場合 → 本人確認ダイアログ表示
- 「本人確認済み」チェック後に確定 → status=1 に更新
- event_type=20 を記録

### 4.5 泊まり事務当 (NightDuty)

**目的**: 荷物棚の棚卸し（2フェーズ）

**前提条件**: 事務当番が設定済みであること。開始時に event_type=11 が記録される。

#### フェーズ1: 現物確認

1. 未引渡荷物を棟別にリスト表示（A棟/B棟/C棟/臨キャパ/新入寮生）
2. 各荷物について:
   - 棚に荷物があればチェックボックスをON
   - 見つからなければ「紛失」トグルをON → 確認ダイアログ
3. 全荷物をチェックしたら確認ダイアログ → フェーズ2へ

**紛失フラグ (is_lost)**:

| 値 | 状態 |
|---|---|
| 0 | あり（フェーズ1） |
| 1 | 紛失（フェーズ1） |
| 10 | あり（フェーズ2、変更不可） |
| 11 | 紛失（フェーズ2、変更不可） |

#### フェーズ2: 荷物札確認

1. 同じリストだが紛失トグルは無効
2. 新入寮生セクションは非表示
3. 全チェック完了で泊まり事務当終了

### 4.6 操作取り消し

**対象**: メイン画面のイベントログをタップ

**取消可能条件**: is_finished=0 のイベントのみ

#### 登録の取消 (event_type=1)
- parcels_current_count, parcels_total_count を -1
- 荷物を論理削除（is_deleted=1）
- イベントを論理削除
- event_type=3（削除）イベントを新規記録

#### 引渡の取消 (event_type=2)
- parcels_current_count を +1
- is_released=0 に戻し、release_* フィールドをクリア
- 元の登録イベントの is_finished=0 に戻す（is_after_fixed_time=0の場合のみ）
- event_type=3（削除）イベントを新規記録

### 4.7 履歴閲覧 (OldNote)

**目的**: 過去の荷物記録の閲覧

**フィルター**:
- 日付範囲（デフォルト: 直近3日間、MaterialDatePicker使用）
- 棟別（A棟/B棟/C棟/臨キャパ/全体）

**表示項目**: 登録日時, 所有者, 登録者, 荷物種別, 引渡日時, 受取人, 引渡者, 最終確認日時

### 4.8 管理用画面 (Others)

**パスワード**: `PassworD`

**機能**:
- 同期ステータスの強制変更（テーブル単位 or 全テーブル）
  - sharing_status → 10（再同期対象に設定）
  - sharing_status → 30（同期済みに設定）
- テスト寮生の追加（部屋「事務室」、名前「テスト 1」、block_id=10）

---

## 5. サーバー同期

### 接続先
- `http://192.168.0.100:8080` （LAN内PCサーバー、ハードコード）
- 対応サーバー: `luggage-manager-api`

### 同期プロトコル
1. sharing_status < 30 のレコードを最大50件取得
2. JSON にシリアライズして `POST /{table}/create` に送信
3. サーバーの応答:
   - 空文字: 完了 → ローカルを sharing_status=30 に更新
   - SQL文: ローカルDBで直接実行（サーバーからのデータプッシュ）
4. 50件未満になるまでループ

### 同期タイミング
- 画面遷移時
- 登録・引渡・取消などの操作後
- 更新ボタン押下時

---

## 6. UI/UX特記事項

### レイアウト
- **横画面固定**（タブレット前提）
- ブロック → 部屋 → 寮生の **3カラムナビゲーション**（登録・引渡・事務当交代で共通）

### サウンドエフェクト
- カーソル移動音（ブロック/部屋/寮生選択時、ピッチ変化あり）
- エラー音
- スキャン音（3種）
- 完了音
- 本人確認音（仮面ライダー555のSE）
- 画面遷移音
- 検索音

### 連打防止
- ボタン: 1000ms のデバウンス
- リストアイテム: 400ms のデバウンス

### その他
- 代理引渡時は背景色がピンク (RGB 255,200,180) に変化
- 論理削除方式（物理削除なし）
- バッテリー監視機能あり（充電切れ警告、現在無効化）

---

## 7. ビジネスルールまとめ

| # | ルール |
|---|---|
| 1 | 事務当番が未設定の状態では登録・引渡・QRスキャン・泊まり事務当は実行不可 |
| 2 | 退寮済（status=10）の寮生は選択対象外 |
| 3 | 未引渡荷物が0件の寮生には引渡操作不可 |
| 4 | 引渡時は必ず本人確認ダイアログを表示（スキップ不可） |
| 5 | 代理引渡では荷物チェックボックスがデフォルト未選択（明示的選択が必要） |
| 6 | is_finished=1 のイベントは取消不可 |
| 7 | 泊まり事務当は2フェーズ（現物確認→荷物札確認）を完了する必要がある |
| 8 | 全レコードは論理削除（is_deleted フラグ）で管理 |
| 9 | 同期は操作のたびに実行される |
| 10 | 新入寮生の荷物は面接番号で管理される |

---

## 8. 関連システム

| システム | 役割 |
|---|---|
| luggage-manager-api | サーバー側API（同期・バックアップ） |
| nimotsu-app-pc | PC版管理アプリ（庶務部用） |
| Slack POKKE ワークスペース | 荷物到着通知 + QRコード配信 |
