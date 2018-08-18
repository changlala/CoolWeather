package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/***
 * forecast: 未来3天天气
 */
public class Weather2 {
    private Basic basic;
    @SerializedName("daily_forecast")
    private List<ForeCast> forecasts;
    private String status;
    private Update update;


    public static class ForeCast{
        @SerializedName("cond_txt_d")
        private String describeD;
        @SerializedName("cond_txt_n")
        private String describeN;
        private String date;
        @SerializedName("tmp_max")
        private String tmpMax;
        @SerializedName("tmp_min")
        private String tmpMin;

    }


}
