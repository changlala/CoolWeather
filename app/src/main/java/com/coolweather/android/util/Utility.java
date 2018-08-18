package com.coolweather.android.util;

import android.util.Log;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.gson.Weather2;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Utility {
    private static final String TAG = "Utility";
    public static boolean handleProvinceResponse(String response){
        //解析response
        try{
            /**  这里不能用gson解析，因为类变量和json数据不是严密对应的
            Gson gson = new Gson();
            List<Province> provinces = gson.fromJson(response,new TypeToken<List<Province>>(){}.getType());
            //这个为什么要在（）后面加{}呢？应该是因为原生的构造函数是protected的，只有其子类才可访问。
            // 而加上{}就代表所实例化的匿名对像是原对象的的子类。可以给一个普通对象的构造函数加 上{}试试，例Gson
            //例 Gson gson = new Gson(){};编译器提示Gson为final类无法被继承

            */
            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0; i < jsonArray.length();i++){//jsonArray没有使用collection接口不能foreach ？
                JSONObject o = jsonArray.getJSONObject(i);
                Province province = new Province();
                province.setProvinceId(o.getInt("id"));
                province.setName(o.getString("name"));
                province.save();
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean handleCityRequest(String response,int provinceId){
        try{
            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0; i < jsonArray.length();i++){
                JSONObject o = jsonArray.getJSONObject(i);
                City city = new City();
                city.setCityId(o.getInt("id"));
                city.setName(o.getString("name"));
                city.setpId(provinceId);
                Log.d(TAG, "handleCityRequest: name"+o.getString("name")+"id"+o.getInt("id"));
                city.save();
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean handleCountyRequest(String response,int cityId){
        try{
            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0; i < jsonArray.length();i++){
                JSONObject o = jsonArray.getJSONObject(i);
                County county = new County();
                county.setCountyId(o.getInt("id"));
                county.setName(o.getString("name"));
                county.setWeatherId(o.getString("weather_id") );
                county.setcId(cityId);
                county.save();
            }

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /***
     *
     * @param response
     * @return 实况天气对象
     */
    public static Weather handleNowWeatherRequest(String response){
        try{
            JSONObject obj = new JSONObject(response);
            JSONArray array = obj.getJSONArray("HeWeather6");
            String weatherContent = array.getJSONObject(0).toString();
            Gson gson = new Gson();
            return gson.fromJson(weatherContent,Weather.class);
        }catch (JSONException e){
            e.printStackTrace();

        }
        return null;

    }

    /**
     *
     * @param response
     * @return 未来三天天气对象
     */
    public static Weather2 handleForeCastWeatherRequest(String response){
        try{
            JSONObject obj = new JSONObject(response);
            JSONArray array = obj.getJSONArray("HeWeather6");
            String weatherContent = array.getJSONObject(0).toString();
            Gson gson = new Gson();
            return gson.fromJson(weatherContent,Weather2.class);
        }catch (JSONException e){
            e.printStackTrace();

        }
        return null;

    }
}
