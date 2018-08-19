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

        public String getDescribeD() {
            return describeD;
        }

        public void setDescribeD(String describeD) {
            this.describeD = describeD;
        }

        public String getDescribeN() {
            return describeN;
        }

        public void setDescribeN(String describeN) {
            this.describeN = describeN;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTmpMax() {
            return tmpMax;
        }

        public void setTmpMax(String tmpMax) {
            this.tmpMax = tmpMax;
        }

        public String getTmpMin() {
            return tmpMin;
        }

        public void setTmpMin(String tmpMin) {
            this.tmpMin = tmpMin;
        }
    }

    public Basic getBasic() {
        return basic;
    }

    public void setBasic(Basic basic) {
        this.basic = basic;
    }

    public List<ForeCast> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<ForeCast> forecasts) {
        this.forecasts = forecasts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }
}
