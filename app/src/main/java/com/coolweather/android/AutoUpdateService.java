package com.coolweather.android;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.gson.Weather2;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    private static final String TAG = "AutoUpdateService";
    private String cName;
    private String degree;
    private String cond;
    private static boolean isFirst = true;
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CountDownLatch countDownLatch = new CountDownLatch(3);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String nowJson = prefs.getString("now",null);
        String weatherId = Utility.handleNowWeatherRequest(nowJson).getBasic().getCid();

        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int tenMinutes = 1*60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime()+tenMinutes;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);//不在状态栏显示？？？？
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        if(nowJson != null && !isFirst ){
            requesWeather(countDownLatch,weatherId,"now");
            requesWeather(countDownLatch,weatherId,"forecast");
            loadBingPic(countDownLatch);
            try{
                countDownLatch.await();
                NotificationManager manager1 = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new NotificationCompat.Builder(this)
                        .setContentTitle("天气已更新")
                        .setContentText(cName+"地区 "+cond+" 气温 "+degree)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX).build();
                manager1.notify(0,notification);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        isFirst = false;
        return super.onStartCommand(intent, flags, startId);

    }

    private void loadBingPic(final CountDownLatch countDownLatch){
        String url = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String bingUrl = response.body().string();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this)
                        .edit();
                editor.putString("bingPic",bingUrl);
                editor.apply();
                countDownLatch.countDown();
            }
        });

    }
    private void requesWeather(final CountDownLatch countDownLatch,String weatherId , String type) {
        String srcNow = "https://free-api.heweather.com/s6/weather/now?";
        String srcForecast = "https://free-api.heweather.com/s6/weather/forecast?";
        String param = "location=" + weatherId + "&key=11bc161e04104be693fd733bc06332ae";

        switch (type) {
            case "now":
                String url = srcNow + param;
                HttpUtil.sendOkhttpRequest(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String json = response.body().string();
                        if (json != null) {
                            final Weather now = Utility.handleNowWeatherRequest(json);
                            if (now != null && now.getStatus().equals("ok")) {
                                cName = now.getBasic().getcName();
                                degree = now.getNow().getTmp();
                                cond = now.getNow().getDescribe();
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                                editor.putString("now", json);
                                editor.apply();

                            }
                        } else {

                        }
                        countDownLatch.countDown();
                        Log.d(TAG, "onResponse: now wether is done");
                    }
                });
                break;
            case "forecast":
                String urll = srcForecast + param;
                HttpUtil.sendOkhttpRequest(urll, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String json = response.body().string();
                        if (json != null) {
                            final Weather2 forecast = Utility.handleForeCastWeatherRequest(json);
                            if (forecast != null && forecast.getStatus().equals("ok")) {
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                                editor.putString("forecast", json);
                                editor.apply();

                            }
                        }
                        countDownLatch.countDown();
                        Log.d(TAG, "onResponse: forecast weather is done");
                    }
                });
        }
    }

}
