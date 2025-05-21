package com.example.smsforward;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SmsProcessingService extends JobIntentService {

    // یک شناسهٔ یکتا برای JobIntentService
    private static final int JOB_ID = 1000;

    // متد استاتیک برای enqueue کردن کار
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, SmsProcessingService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String sender = intent.getStringExtra("sender");
        String message = intent.getStringExtra("message");
        if (message == null) return;

        // بررسی وضعیت شبکه
        if (!isNetworkAvailable()) {
            // در صورت آفلاین بودن، می‌توانید پیام را ذخیره کرده و بعداً ارسال کنید.
            return;
        }

        try {
            // ساخت JSON برای ارسال به سرور واسطه
            JSONObject payload = new JSONObject();
            payload.put("message", "From: " + sender + "\n" + message);

            // آدرس ریلیوی شما
            String urlString = "https://web-production-2ea9.up.railway.app/";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // نوشتن بدنهٔ JSON
            OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            os.write(payload.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            // بررسی کد پاسخ HTTP
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK
                    || responseCode == HttpURLConnection.HTTP_CREATED) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // متدی برای بررسی وضعیت شبکه
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }
}
