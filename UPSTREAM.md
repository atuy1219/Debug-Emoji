# Upstream fonts

このモジュールは Google Fonts の `googlefonts/noto-emoji` を upstream とします。

Repository:

- https://github.com/googlefonts/noto-emoji

Pinned tag:

- `v2.051`

Build 時に取得するファイル:

```text
https://raw.githubusercontent.com/googlefonts/noto-emoji/v2.051/fonts/NotoColorEmoji.ttf
https://raw.githubusercontent.com/googlefonts/noto-emoji/v2.051/fonts/NotoColorEmoji-flagsonly.ttf
```

Module 内の配置:

```text
NotoColorEmoji.ttf          -> system/fonts/NotoColorEmoji.ttf
NotoColorEmoji-flagsonly.ttf -> system/fonts/NotoColorEmojiFlags.ttf
```

Expected SHA-256:

```text
72a635cb3d2f3524c51620cdde406b217204e8a6a06c6a096ff8ed4b5fd6e27b  NotoColorEmoji.ttf
29779e6015811e747dfeafa418f4d3bd0824c5e045b5e489b359f7b69d0bb52c  NotoColorEmoji-flagsonly.ttf
```

フォント本体はこの repository には含めません。Google 公式の pinned tag から build 時に取得し、hash を照合します。
