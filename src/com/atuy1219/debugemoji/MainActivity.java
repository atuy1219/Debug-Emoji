package com.atuy1219.debugemoji;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String FLAGS = "JP=🇯🇵  US=🇺🇸  CN=🇨🇳  HK=🇭🇰  TW=🇹🇼";
    private static final String U17 = "U17=🫪 🫯 🫈 🛘 🫍 🪊 🪎";
    private static final String[] TEST_TEXTS = {
            "🇹🇼", "🇯🇵", "🇺🇸", "🇨🇳", "🇭🇰", "🇿🇿",
            "🫪", "🫯", "🫈", "🛘", "🫍", "🪊", "🪎"
    };
    private static final String[] TEST_NAMES = {
            "TW", "JP", "US", "CN", "HK", "ZZ",
            "U17_1FAEA", "U17_1FAEF", "U17_1FAC8", "U17_1F6D8",
            "U17_1FACD", "U17_1FA8A", "U17_1FA8E"
    };

    private LinearLayout root;
    private TextView status;
    private List<String> mappedEmojiFonts;

    private static int dp(Context c, int n) {
        return (int)(n * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView label(String s) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(Color.rgb(180, 180, 180));
        v.setTextSize(13);
        v.setPadding(dp(this, 12), dp(this, 12), dp(this, 12), dp(this, 4));
        return v;
    }

    private TextView plainTextView(String text, boolean software) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextColor(Color.WHITE);
        v.setTextSize(30);
        v.setPadding(dp(this, 12), dp(this, 8), dp(this, 12), dp(this, 8));
        v.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        if (software) v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        return v;
    }

    private TextView directTextView(String text, Typeface tf) {
        TextView v = plainTextView(text, true);
        v.setTypeface(tf);
        return v;
    }

    private EditText editText(String text, boolean software) {
        EditText v = new EditText(this);
        v.setText(text);
        v.setTextColor(Color.WHITE);
        v.setTextSize(30);
        v.setSingleLine(false);
        v.setPadding(dp(this, 12), dp(this, 8), dp(this, 12), dp(this, 8));
        if (software) v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        return v;
    }

    private void addProbe(String name, View v) {
        root.addView(label(name));
        root.addView(v, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mappedEmojiFonts = findMappedEmojiFonts();

        ScrollView scroller = new ScrollView(this);
        scroller.setBackgroundColor(Color.rgb(18, 18, 18));
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, dp(this, 8), 0, dp(this, 32));
        scroller.addView(root);
        setContentView(scroller);

        TextView title = plainTextView("Debug Emoji — Font Path Probe v2", false);
        title.setTextSize(22);
        root.addView(title);

        TextView info = plainTextView(
                "Pure Android framework probe — no AndroidX, Compose, GMS library, EmojiCompat, WebView, or network.\n" +
                "Build=" + Build.FINGERPRINT + "\nAPI=" + Build.VERSION.SDK_INT, false);
        info.setTextSize(12);
        root.addView(info);

        addProbe("1. TextView — hardware/default", plainTextView(FLAGS, false));
        addProbe("2. TextView — forced software layer", plainTextView(FLAGS, true));
        addProbe("3. EditText — hardware/default", editText(FLAGS, false));
        addProbe("4. EditText — forced software layer", editText(FLAGS, true));
        addProbe("5. StaticLayout.draw(Canvas)", new StaticLayoutProbeView(this, FLAGS));
        addProbe("6. Canvas.drawText()", new CanvasProbeView(this, FLAGS, false, null));
        addProbe("7. Canvas.drawTextRun()", new CanvasProbeView(this, FLAGS, true, null));
        addProbe("8. TextView — TW only", plainTextView("TW=[🇹🇼] <- brackets must contain a visible flag", false));
        addProbe("9. Unicode 17 — TextView", plainTextView(U17, false));
        addProbe("10. Unicode 17 — Canvas.drawText()", new CanvasProbeView(this, U17, false, null));

        addDirectFontSection("/system/fonts/NotoColorEmoji.ttf");
        addDirectFontSection("/system/fonts/NotoColorEmojiFlags.ttf");

        for (String path : mappedEmojiFonts) {
            if (!path.equals("/system/fonts/NotoColorEmoji.ttf") &&
                !path.equals("/system/fonts/NotoColorEmojiFlags.ttf")) {
                addDirectFontSection(path);
            }
        }

        TextView maps = plainTextView("Mapped emoji/font files in this app process:\n" + joinLines(mappedEmojiFonts), false);
        maps.setTextSize(11);
        root.addView(maps);

        Button reportButton = new Button(this);
        reportButton.setText("COPY FULL REPORT TO CLIPBOARD");
        reportButton.setOnClickListener(v -> {
            String r = makeReport();
            ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("DebugEmoji", r));
            status.setText("Full report copied to clipboard.");
        });
        root.addView(reportButton);

        Button shotButton = new Button(this);
        shotButton.setText("SAVE PIXELCOPY SCREENSHOT");
        shotButton.setOnClickListener(v -> savePixelCopy());
        root.addView(shotButton);

        status = plainTextView(
                "Key comparison: DEFAULT vs direct /system font vs mapped /data/fonts font.", false);
        status.setTextSize(13);
        root.addView(status);
    }

    private void addDirectFontSection(String path) {
        try {
            File f = new File(path);
            if (!f.isFile() || !f.canRead()) return;
            Typeface tf = Typeface.createFromFile(f);
            addProbe("DIRECT TextView: " + path, directTextView("TW=🇹🇼 JP=🇯🇵 U17=🫪", tf));
            addProbe("DIRECT Canvas: " + path, new CanvasProbeView(this, "TW=🇹🇼 JP=🇯🇵 U17=🫪", false, tf));
        } catch (Throwable t) {
            TextView v = plainTextView("Failed: " + t.getClass().getSimpleName() + ": " + t.getMessage(), false);
            v.setTextSize(12);
            addProbe("DIRECT load failed: " + path, v);
        }
    }

    private List<String> findMappedEmojiFonts() {
        Set<String> out = new LinkedHashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/self/maps"))) {
            String line;
            while ((line = br.readLine()) != null) {
                int slash = line.indexOf('/');
                if (slash < 0) continue;
                String path = line.substring(slash).trim();
                String lower = path.toLowerCase(Locale.US);
                if ((lower.contains("emoji") || lower.contains("/fonts/")) &&
                    (lower.endsWith(".ttf") || lower.endsWith(".otf"))) {
                    out.add(path);
                }
            }
        } catch (Throwable ignored) {}
        return new ArrayList<>(out);
    }

    private static String joinLines(List<String> paths) {
        if (paths.isEmpty()) return "(none found)";
        StringBuilder b = new StringBuilder();
        for (String p : paths) b.append(p).append('\n');
        return b.toString();
    }

    private String makeReport() {
        StringBuilder b = new StringBuilder();
        b.append("Debug-Emoji Font Path Probe v2\n");
        b.append("fingerprint=").append(Build.FINGERPRINT).append('\n');
        b.append("sdk=").append(Build.VERSION.SDK_INT).append('\n');
        b.append("release=").append(Build.VERSION.RELEASE).append('\n');

        b.append("\n[MAPPED_FONT_PATHS]\n");
        for (String p : mappedEmojiFonts) b.append(p).append('\n');

        appendPaintReport(b, "PaintDefault", new Paint(Paint.ANTI_ALIAS_FLAG));
        appendPaintReport(b, "DEFAULT", paintWith(Typeface.DEFAULT));
        appendPaintReport(b, "SANS_SERIF", paintWith(Typeface.SANS_SERIF));
        appendPaintReport(b, "MONOSPACE", paintWith(Typeface.MONOSPACE));
        appendPaintReport(b, "SERIF", paintWith(Typeface.SERIF));

        appendDirectFileReport(b, "/system/fonts/NotoColorEmoji.ttf");
        appendDirectFileReport(b, "/system/fonts/NotoColorEmojiFlags.ttf");
        for (String p : mappedEmojiFonts) {
            if (!p.equals("/system/fonts/NotoColorEmoji.ttf") &&
                !p.equals("/system/fonts/NotoColorEmojiFlags.ttf")) {
                appendDirectFileReport(b, p);
            }
        }

        b.append("\nInterpretation:\n");
        b.append("hasGlyph=true + pixels=0 => accepted glyph/run with advance but no painted pixels in this app process.\n");
        b.append("DEFAULT pixels=0 but DIRECT /system pixels>0 => app-visible font-map/fallback differs from raw system font.\n");
        b.append("A mapped /data/fonts file reproducing pixels=0 directly identifies that font as the blank-glyph source.\n");
        return b.toString();
    }

    private void appendPaintReport(StringBuilder b, String name, Paint p) {
        p.setTextSize(64f);
        b.append("\n[").append(name).append("]\n");
        for (int j = 0; j < TEST_TEXTS.length; j++) {
            boolean hg;
            try { hg = p.hasGlyph(TEST_TEXTS[j]); }
            catch (Throwable t) { hg = false; }
            int pixels = countPixels(p.getTypeface(), TEST_TEXTS[j]);
            b.append(TEST_NAMES[j])
             .append(" hasGlyph=").append(hg)
             .append(" width=").append(p.measureText(TEST_TEXTS[j]))
             .append(" pixels=").append(pixels)
             .append('\n');
        }
    }

    private void appendDirectFileReport(StringBuilder b, String path) {
        b.append("\n[DIRECT:").append(path).append("]\n");
        try {
            File f = new File(path);
            b.append("exists=").append(f.exists())
             .append(" readable=").append(f.canRead())
             .append(" size=").append(f.exists() ? f.length() : -1)
             .append('\n');
            if (!f.isFile() || !f.canRead()) return;
            Typeface tf = Typeface.createFromFile(f);
            Paint p = paintWith(tf);
            p.setTextSize(64f);
            for (int j = 0; j < TEST_TEXTS.length; j++) {
                boolean hg;
                try { hg = p.hasGlyph(TEST_TEXTS[j]); }
                catch (Throwable t) { hg = false; }
                b.append(TEST_NAMES[j])
                 .append(" hasGlyph=").append(hg)
                 .append(" width=").append(p.measureText(TEST_TEXTS[j]))
                 .append(" pixels=").append(countPixels(tf, TEST_TEXTS[j]))
                 .append('\n');
            }
        } catch (Throwable t) {
            b.append("ERROR=").append(t.getClass().getName()).append(": ")
             .append(t.getMessage()).append('\n');
        }
    }

    private Paint paintWith(Typeface tf) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(tf);
        return p;
    }

    private int countPixels(Typeface tf, String text) {
        Bitmap bm = Bitmap.createBitmap(256, 192, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        c.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.WHITE);
        p.setTextSize(128f);
        if (tf != null) p.setTypeface(tf);
        c.drawText(text, 24f, 144f, p);
        int[] px = new int[256 * 192];
        bm.getPixels(px, 0, 256, 0, 0, 256, 192);
        int n = 0;
        for (int v : px) if (((v >>> 24) & 0xff) != 0) n++;
        bm.recycle();
        return n;
    }

    private void savePixelCopy() {
        final Window w = getWindow();
        final View decor = w.getDecorView();
        final int width = decor.getWidth();
        final int height = decor.getHeight();
        if (width <= 0 || height <= 0) {
            status.setText("Window has no size yet.");
            return;
        }

        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        PixelCopy.request(w, bitmap, result -> {
            if (result != PixelCopy.SUCCESS) {
                status.setText("PixelCopy failed: " + result);
                return;
            }
            try {
                String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Images.Media.DISPLAY_NAME, "DebugEmoji_" + stamp + ".png");
                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                if (Build.VERSION.SDK_INT >= 29) {
                    cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Debug-Emoji");
                    cv.put(MediaStore.Images.Media.IS_PENDING, 1);
                }

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                if (uri == null) throw new RuntimeException("MediaStore insert returned null");
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    if (os == null) throw new RuntimeException("openOutputStream returned null");
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                }

                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues done = new ContentValues();
                    done.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, done, null, null);
                }

                status.setText("Saved PixelCopy screenshot: " + uri);
            } catch (Throwable t) {
                status.setText("Save failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }, new Handler(Looper.getMainLooper()));
    }

    public static class CanvasProbeView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String text;
        private final boolean run;

        public CanvasProbeView(Context context, String text, boolean run, Typeface tf) {
            super(context);
            this.text = text;
            this.run = run;
            paint.setColor(Color.WHITE);
            paint.setTextSize(64f);
            if (tf != null) paint.setTypeface(tf);
            setBackgroundColor(Color.rgb(18, 18, 18));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(getContext(), 100));
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float x = dp(getContext(), 12);
            float y = dp(getContext(), 70);
            if (run) {
                canvas.drawTextRun(text, 0, text.length(), 0, text.length(), x, y, false, paint);
            } else {
                canvas.drawText(text, x, y, paint);
            }
        }
    }

    public static class StaticLayoutProbeView extends View {
        private final StaticLayout layout;

        @SuppressWarnings("deprecation")
        public StaticLayoutProbeView(Context context, String text) {
            super(context);
            TextPaint p = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(Color.WHITE);
            p.setTextSize(64f);
            layout = new StaticLayout(text, p, 2000,
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            setBackgroundColor(Color.rgb(18, 18, 18));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(getContext(), 100));
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.save();
            canvas.translate(dp(getContext(), 12), dp(getContext(), 8));
            layout.draw(canvas);
            canvas.restore();
        }
    }
}
