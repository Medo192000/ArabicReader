package com.medo.arabicreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OcrHelper {

    public interface Callback {
        void onSuccess(String text);
        void onError(String message);
    }

    private static final String MODEL_URL =
        "https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/main/ara.traineddata";

    private final Context context;
    private final ExecutorService executor =
        Executors.newSingleThreadExecutor();
    private final Handler mainHandler =
        new Handler(Looper.getMainLooper());

    public OcrHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public void recognize(Bitmap bitmap, Callback callback) {
        executor.execute(() -> {
            try {
                File tessData = new File(
                    context.getFilesDir(),
                    "tesseract/tessdata"
                );

                if (!tessData.exists() && !tessData.mkdirs()) {
                    throw new Exception(
                        "تعذر إنشاء مجلد نموذج اللغة."
                    );
                }

                File model = new File(
                    tessData,
                    "ara.traineddata"
                );

                if (!model.exists() || model.length() == 0) {
                    downloadModel(model);
                }

                TessBaseAPI tess = new TessBaseAPI();

                try {
                    String dataPath = new File(
                        context.getFilesDir(),
                        "tesseract"
                    ).getAbsolutePath();

                    if (!tess.init(dataPath, "ara")) {
                        throw new Exception(
                            "تعذر تشغيل نموذج اللغة العربية."
                        );
                    }

                    tess.setPageSegMode(
                        TessBaseAPI.PageSegMode.PSM_AUTO
                    );

                    tess.setImage(bitmap);

                    String text = tess.getUTF8Text();

                    if (text == null) {
                        text = "";
                    }

                    String result = text.trim();

                    mainHandler.post(() ->
                        callback.onSuccess(result)
                    );

                } finally {
                    tess.end();
                }

            } catch (Exception e) {
                String message = e.getMessage();

                if (message == null || message.isEmpty()) {
                    message = "حدث خطأ أثناء قراءة الصورة.";
                }

                final String error = message;

                mainHandler.post(() ->
                    callback.onError(error)
                );
            }
        });
    }

    private void downloadModel(File destination)
            throws Exception {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(MODEL_URL);

            connection = (HttpURLConnection)
                url.openConnection();

            connection.setConnectTimeout(20000);
            connection.setReadTimeout(60000);
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            int status = connection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
                throw new Exception(
                    "فشل تنزيل نموذج العربية. "
                    + "رمز الاستجابة: " + status
                );
            }

            File temporary = new File(
                destination.getParentFile(),
                "ara.traineddata.tmp"
            );

            try (
                InputStream input =
                    connection.getInputStream();

                FileOutputStream output =
                    new FileOutputStream(temporary)
            ) {
                byte[] buffer = new byte[8192];
                int count;

                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }

                output.flush();
            }

            if (temporary.length() == 0) {
                temporary.delete();

                throw new Exception(
                    "ملف نموذج اللغة الذي تم تنزيله فارغ."
                );
            }

            if (destination.exists()) {
                destination.delete();
            }

            if (!temporary.renameTo(destination)) {
                temporary.delete();

                throw new Exception(
                    "تعذر حفظ نموذج اللغة العربية."
                );
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
