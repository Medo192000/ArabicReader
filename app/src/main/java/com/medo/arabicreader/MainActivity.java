```java
package com.medo.arabicreader;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final int PICK_IMAGE = 101;
    private static final int PICK_PDF = 102;

    private TextView resultText;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 28, 24, 24);
        root.setBackgroundColor(Color.rgb(248, 249, 252));

        TextView title = new TextView(this);
        title.setText("قارئ العربي");
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.rgb(35, 65, 120));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("اقرأ النصوص العربية من الصور وملفات PDF");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 12, 0, 24);
        root.addView(subtitle);

        Button imageButton = new Button(this);
        imageButton.setText("📷  اختر صورة لقراءة النص");
        root.addView(imageButton);

        Button pdfButton = new Button(this);
        pdfButton.setText("📄  افتح ملف PDF");
        root.addView(pdfButton);

        TextView resultTitle = new TextView(this);
        resultTitle.setText("النص المستخرج");
        resultTitle.setTextSize(19);
        resultTitle.setTypeface(null, Typeface.BOLD);
        resultTitle.setTextColor(Color.BLACK);
        resultTitle.setPadding(0, 20, 0, 8);
        root.addView(resultTitle);

        ScrollView scrollView = new ScrollView(this);

        resultText = new TextView(this);
        resultText.setText(
            "النص الذي ستستخرجه من الصورة أو ملف PDF سيظهر هنا."
        );
        resultText.setTextSize(18);
        resultText.setTextColor(Color.rgb(35, 35, 35));
        resultText.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG_RTL);
        resultText.setGravity(Gravity.RIGHT);
        resultText.setPadding(16, 16, 16, 16);
        resultText.setBackgroundColor(Color.WHITE);

        scrollView.addView(resultText);

        LinearLayout.LayoutParams scrollParams =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1
            );

        scrollParams.setMargins(0, 8, 0, 12);
        root.addView(scrollView, scrollParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button copyButton = new Button(this);
        copyButton.setText("نسخ النص");

        Button speakButton = new Button(this);
        speakButton.setText("قراءة صوتية");

        actions.addView(copyButton,
            new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        actions.addView(speakButton,
            new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        root.addView(actions);

        setContentView(root);

        imageButton.setOnClickListener(v -> openPicker(PICK_IMAGE));
        pdfButton.setOnClickListener(v -> openPicker(PICK_PDF));

        copyButton.setOnClickListener(v -> copyText());
        speakButton.setOnClickListener(v -> speakText());

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("ar"));

                if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(
                        this,
                        "الصوت العربي غير متاح حاليًا على جهازك",
                        Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
    }

    private void openPicker(int type) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (type == PICK_IMAGE) {
            intent.setType("image/*");
        } else {
            intent.setType("application/pdf");
        }

        startActivityForResult(intent, type);
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        Uri selectedFile = data.getData();

        if (selectedFile == null) {
            return;
        }

        if (requestCode == PICK_IMAGE) {
            resultText.setText(
                "تم اختيار الصورة بنجاح.\n\n"
                + "الخطوة التالية هي تشغيل محرك OCR "
                + "لاستخراج النص العربي منها."
            );
        } else if (requestCode == PICK_PDF) {
            resultText.setText(
                "تم اختيار ملف PDF بنجاح.\n\n"
                + "الخطوة التالية هي قراءة صفحات الملف "
                + "واستخراج النصوص منها."
            );
        }
    }

    private void copyText() {
        String text = resultText.getText().toString();

        if (TextUtils.isEmpty(text)) {
            Toast.makeText(
                this, "لا يوجد نص لنسخه", Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ClipboardManager clipboard =
            (ClipboardManager) getSystemService(
                Context.CLIPBOARD_SERVICE
            );

        if (clipboard != null) {
            clipboard.setPrimaryClip(
                ClipData.newPlainText("النص المستخرج", text)
            );

            Toast.makeText(
                this, "تم نسخ النص", Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void speakText() {
        String text = resultText.getText().toString();

        if (textToSpeech == null || TextUtils.isEmpty(text)) {
            Toast.makeText(
                this, "لا يوجد نص لقراءته", Toast.LENGTH_SHORT
            ).show();
            return;
        }

        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "arabic_reader"
        );
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
    }
}
```
