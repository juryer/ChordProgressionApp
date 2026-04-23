# Chord Progression Manager

作曲時のコード進行を管理するデスクトップアプリケーションです。  
コードネームまたはディグリーネームでコード進行を登録・管理し、楽曲単位でセクション構成を組み立て、MIDIファイルの出力も可能です。

---

## 概要

音楽制作の現場では「このコード進行をメモしておきたい」という場面が多くあります。  
そのニーズを解決するために開発した自作ツールです。  
AIを活用しながら、実用的な機能をゼロから設計・開発しました。  
初めて開発したアプリですが、テスト業務で培った品質を意識する視点を活かして取り組みました。

---

## 機能一覧

- コード進行の登録・管理
- コードネーム・ディグリーネームどちらでも入力可能
- 曲単位でコード進行の作成も可能
- MIDIファイル出力（BPM・拍子・移調対応）
- コード進行の並び替え・ソート・レーティング機能
- データ保存（JSON）

---

## 動作環境・必要ライブラリ

| 項目 | 内容 |
|------|------|
| Java | 21以上 |
| OS | Windows 10 / 11 |
| ライブラリ | Gson 2.10.1（`lib/gson-2.10.1.jar` に同梱） |

---

## 開発環境・使用技術

| 項目 | 内容 |
|------|------|
| 言語 | Java 21 |
| フレームワーク | Swing / Gson / javax.sound.midi |
| IDE | Eclipse（Pleiades All in One） |
| ビルド | jpackage（exe化） |
| バージョン管理 | GitHub |

---

## 起動方法

### Eclipseで実行する場合

1. このリポジトリをクローンまたはダウンロード
2. Eclipseで「既存プロジェクトをワークスペースへ」でインポート
3. `lib/gson-2.10.1.jar` がビルドパスに含まれていることを確認
4. `src/com/chordapp/Main.java` を右クリック →「実行」→「Javaアプリケーション」

### ビルドパスの確認方法

```
プロジェクトを右クリック
→「プロパティ」
→「Javaのビルド・パス」
→「ライブラリー」タブ
→ gson-2.10.1.jar が含まれているか確認
　含まれていなければ「JARの追加」→ lib/gson-2.10.1.jar を選択
```

---

## データ保存場所

アプリのデータは以下の場所に自動保存されます。

```
C:\Users\ユーザー名\AppData\Roaming\ChordProgressionApp\
　├── progressions.json　← コード進行データ
　└── songs.json　　　　← 楽曲データ
```

エクスプローラーのアドレスバーに `%APPDATA%\ChordProgressionApp` と入力するとすぐに開けます。

---

## クラス構成

| クラス名 | 役割 |
|---------|------|
| Main | エントリポイント |
| AppSettings | 設定シングルトン（バージョン・テーマ・表示モード） |
| AppTheme | カラーテーマ管理（ダークパープル / ダークグリーン / ライト） |
| ChordProgression | コード進行モデル |
| ChordProgressionRepository | コード進行の永続化・CRUD |
| ChordSelectorPanel | コード選択UI（ダイアトニック / タブ / 2段階） |
| ChordTransposer | 移調・構成音・機能（T/SD/D）分析ユーティリティ |
| DegreeConverter | コードネーム → ディグリーネーム変換 |
| DataManager | JSON永続化（Gson使用） |
| MainMenuWindow | メインメニュー |
| RegisterWindow | コード進行登録画面 |
| ListWindow | コード進行一覧・検索・ソート |
| ManageWindow | コード進行管理・編集・エクスポート |
| MidiExporter | MIDIファイル出力 |
| SettingsDialog | 設定ダイアログ |
| Song / SongSection / SongRepository | 楽曲モデル・永続化 |
| SongEditorWindow | 楽曲エディタ（キー設定・歌詞入力） |<img width="1020" height="702" alt="image_2026_4_23" src="https://github.com/user-attachments/assets/2e7ba6b6-6dca-43d9-8b50-bfb401745108" />

| SongListWindow | 楽曲一覧・MIDI出力 |
| ExportUtil | PNG・HTML出力ユーティリティ |

---

## スクリーンショット

<img src="https://github.com/user-attachments/assets/0b25a2f5-e17e-4f92-8a23-fc8595710320" width="40%">
<img src="https://github.com/user-attachments/assets/02e4a217-6e96-461c-896d-928e00fc660f" width="20%">

## スクリーンショット（楽曲エディタ、MIDI出力など）

<img src="https://github.com/user-attachments/assets/c8192589-1e2b-49b1-8d13-d50ea55c7db7" width="40%">
<img src="https://github.com/user-attachments/assets/08eebecc-2994-4425-9272-ab3c3edc2dfa" width="30%">


