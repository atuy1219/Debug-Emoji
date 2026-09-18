# Debug-Emoji

Minimal Android framework probe for isolating emoji rendering differences on OEM Android builds.

The app intentionally has **no AndroidX, Jetpack Compose, Google Play services, EmojiCompat, WebView, or network code**.

## Probe rows

- TextView, default/hardware rendering
- TextView, forced software layer
- EditText, default/hardware rendering
- EditText, forced software layer
- StaticLayout.draw(Canvas)
- Canvas.drawText()
- Canvas.drawTextRun()
- TW-only TextView
- Unicode 17 TextView
- Unicode 17 Canvas

The target sequences include Taiwan, Japan, United States, China, Hong Kong, an invalid ZZ regional-indicator pair, and Unicode 17 emoji.

## Build

GitHub Actions builds a signed debug APK on every push to `main` and exposes it as the `Debug-Emoji-apk` artifact.

The build intentionally uses only Android SDK command-line tools rather than Gradle:

```sh
./build.sh
```

Requirements for local Linux builds:

- JDK 21
- Android SDK platform 36
- Android build-tools 36.0.0
- `zip`

## Package

`com.atuy1219.debugemoji`
