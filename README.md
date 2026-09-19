# OnePlus 13 Noto Emoji Mount Fix

OnePlus 13 / ColorOS 16 / Android 16 向けの KernelSU Next モジュールです。

このリポジトリは旧 Debug-Emoji 検証アプリの置き場から、絵文字フォント置換モジュール本体のソースコード置き場へ変更しました。旧 Android APK のソースは現行ツリーから削除しています。

## 目的

端末の stock `/system/fonts/NotoColorEmojiFlags.ttf` では `🇹🇼` の専用 ligature が存在する一方、その bitmap が完全透明になっていました。また stock の Noto Emoji は Unicode 17 の対象 glyph を持っていません。

このモジュールは Google Noto Emoji 2.051 の次の2ファイルを systemless に配置します。

- `/system/fonts/NotoColorEmoji.ttf`
- `/system/fonts/NotoColorEmojiFlags.ttf`

現在の module ID は既存インストールとの互換性のため `oneplus13_emoji_fix_2051_pua` を維持しています。

## 現在の実装

- KernelSU Next の metamodule に `system/fonts` の mount を任せます。
- 旧版で使っていた `post-fs-data.sh` / `service.sh` からの手動 `mount --bind` は使用しません。
- フォントはリポジトリへ直接コミットせず、build 時に Google 公式 `googlefonts/noto-emoji` の `v2.051` から取得します。
- SHA-256 を固定しているため、意図しない upstream 差し替えは build 時に失敗します。
- runtime self-updater は現在無効です。

## KernelSU Next での注意

KernelSU Next の App Profile / global setting にある **Umount modules（モジュールをアンマウント）** が有効な app では、通常 app の mount namespace から systemless mount が外され、stock font が見える場合があります。

実機では次の挙動を確認しています。

- root / module Action: replacement font が見える
- `Umount modules = ON` の通常 app: stock font が見える
- `Umount modules = OFF` の通常 app: replacement font が見える

そのため、絵文字置換を見せたい app では `Umount modules` を OFF にする必要があります。Hybrid Mount 側の `disable_umount=true` だけでは、この実機構成では回避できませんでした。

## ビルド

Linux/macOS で `curl`, `sha256sum`, `zip` が使える環境を想定しています。

```sh
chmod +x scripts/build-module.sh
./scripts/build-module.sh
```

生成物:

```text
build/OnePlus13_NotoEmoji_MountFix_v2.4.0.zip
```

GitHub Actions の **Build module** からも同じ ZIP を artifact として生成できます。

## 固定している upstream

- Noto Emoji tag: `v2.051`
- `NotoColorEmoji.ttf`
  - SHA-256: `72a635cb3d2f3524c51620cdde406b217204e8a6a06c6a096ff8ed4b5fd6e27b`
- `NotoColorEmoji-flagsonly.ttf` → module 内では `NotoColorEmojiFlags.ttf`
  - SHA-256: `29779e6015811e747dfeafa418f4d3bd0824c5e045b5e489b359f7b69d0bb52c`

詳細は [UPSTREAM.md](UPSTREAM.md) を参照してください。

## ディレクトリ

```text
module/                     KernelSU module 本体
scripts/build-module.sh     reproducible build
.github/workflows/build.yml CI build
UPSTREAM.md                 upstream と hash
```

## インストール

1. KernelSU Next で compatible metamodule（例: Hybrid Mount）を有効化する。
2. GitHub Actions artifact またはローカル build の ZIP を KernelSU Next からインストールする。
3. 再起動する。
4. replacement font を見せたい app では KSUN の `Umount modules` を OFF にする。

実ファイルを `/system` へ直接書き換えるモジュールではありません。
