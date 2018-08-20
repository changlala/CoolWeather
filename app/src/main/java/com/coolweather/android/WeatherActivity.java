package com.coolweather.android;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.gson.Weather2;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private TextView textView;
    private ScrollView scrollView;
    private LinearLayout forecastLayout;
    private TextView titleCity;
    private TextView titleUpdate;
    private TextView nowDegree;
    private TextView nowDescribe;
    private ProgressDialog progressDialog;
    private ImageView bingImg;
    private static final String TAG = "WeatherActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();

        scrollView = findViewById(R.id.weatherLayout);
        //scrollView.setVisibility(View.INVISIBLE);
        forecastLayout = findViewById(R.id.forecastLayout);
        titleCity =  findViewById(R.id.title_city);
        titleUpdate = findViewById(R.id.title_updateTime);

        nowDegree = findViewById(R.id.degree);
        nowDescribe = findViewById(R.id.weatherInfo);

        bingImg = findViewById(R.id.bingPic);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingUrl = prefs.getString("bingPic",null);
        if(bingUrl != null){
            Glide.with(this).load(bingUrl).into(bingImg);
        }else{
            loadBingPic();
        }

        String nowJson = prefs.getString("now",null);
        String forecastJson = prefs.getString("forecast",null);
        Gson gson = new Gson();
        if(nowJson != null && forecastJson != null){
            Weather now = Utility.handleNowWeatherRequest(nowJson);
            showNowWeather(now);
            Weather2 forecast = Utility.handleForeCastWeatherRequest(forecastJson);
            showForecastWeather(forecast);
        }else{
            String weatherId = getIntent().getStringExtra("weatherId");
            /**
             * 两个线程，不知道何时调用closeProgressDialog();
             * 因为这两个线程是异步的没办法确定哪个线程先结束，不确定到底在哪个线程的onResponse里写closeProgressDialog();
             *
             * 想到的解决办法就是
             *  1. 干脆同步访问网络算了，线性顺序。
             *  2. 其他方法呢？？
             */

            //showProgressDialog();
            if(nowJson == null){
                requesWeather(weatherId,"now");

            }
            if(forecastJson == null){
                requesWeather(weatherId,"forecast");
            }
            loadBingPic();
        }
    }
    private void loadBingPic(){
        String url = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingUrl = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingUrl).into(bingImg);
                    }
                });

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString("bingPic",bingUrl);
                editor.apply();
            }
        });
    }
    private void requesWeather(String weatherId , String type){
        String srcNow = "https://free-api.heweather.com/s6/weather/now?";
        String srcForecast = "https://free-api.heweather.com/s6/weather/forecast?";
        String param="location="+weatherId+"&key=11bc161e04104be693fd733bc06332ae";

        switch (type){
            case "now":
                String url = srcNow+param;
                HttpUtil.sendOkhttpRequest(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(WeatherActivity.this,"实况天气获取失败",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String json = response.body().string();
                        if(json != null){
                            final Weather now = Utility.handleNowWeatherRequest(json);
                            if(now != null && now.getStatus().equals("ok")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showNowWeather(now);
                                        }});
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                                editor.putString("now",json);
                                editor.apply();

                            }else{
                                Toast.makeText(WeatherActivity.this,"实况天气获取失败(1)",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(WeatherActivity.this,"now josn is null",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case "forecast":
                String urll = srcForecast+param;
                HttpUtil.sendOkhttpRequest(urll, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(WeatherActivity.this,"预报天气获取失败",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String json = response.body().string();
                        if(json != null){
                            final Weather2 forecast = Utility.handleForeCastWeatherRequest(json);
                            if(forecast != null && forecast.getStatus().equals("ok")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showForecastWeather(forecast);
                                    }});
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                                editor.putString("forecast",json);
                                editor.apply();

                            }else{
                                Toast.makeText(WeatherActivity.this,"预报天气获取失败(1)",Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(WeatherActivity.this,"forecast josn is null",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
        /**
        try{
            /**
             * 这里起初是把这个url给encode结果错误提示
             *  Caused by: java.lang.IllegalArgumentException: Expected URL scheme 'http' or 'https' but no colon was found
             *  后尝试值encodeparam字段 ,结果 返回的json 的status 提示invalid param？？？？？
             *  最后一点也不encode了，成功
             *  ？？？
             *  ？？？？？？、、
             *
            //param = URLEncoder.encode(param,"GBK");
            HttpUtil.sendOkhttpRequest(src+param, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    final Weather now = Utility.handleNowWeatherRequest(responseText);
                    if(now != null){
                        /**
                         * 看下面的警告,最好不要使用string作为参数，推荐@string/这种，那咋弄？
                         *
                        if(now.getStatus().equals("ok")){
                            showNowWeather(now);
                        }else{
                            Log.d(TAG, "onResponse: status "+now.getStatus());
                        }


                    }else{
                        Log.d(TAG, "onResponse: now is null");
                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        */
    }


    private void showNowWeather(Weather now){
        titleCity.setText(now.getBasic().getcName());
        titleUpdate.setText(now.getUpdate().getLoc().split(" ")[1]);
        nowDescribe.setText(now.getNow().getDescribe());
        nowDegree.setText(now.getNow().getTmp());
    }

    private void showForecastWeather(Weather2 forecast){
        List<Weather2.ForeCast> forecastList = forecast.getForecasts();

        forecastLayout.removeAllViews();
        for(Weather2.ForeCast f : forecastList){
            View v = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView forecastDate = v.findViewById(R.id.date);
            TextView forecastTmpMax = v.findViewById(R.id.tmpMax);
            TextView forecastTmpMin = v.findViewById(R.id.tmpMin);
            TextView forecastCondD = v.findViewById(R.id.condD);
            TextView forecastCondN = v.findViewById(R.id.condN);

            forecastDate.setText(f.getDate());
            forecastCondD.setText(f.getDescribeD());
            forecastCondN.setText(f.getDescribeN());
            forecastTmpMin.setText(f.getTmpMin());
            forecastTmpMax.setText(f.getTmpMax());
            forecastLayout.addView(v);
        }
    }

    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载。。。");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
