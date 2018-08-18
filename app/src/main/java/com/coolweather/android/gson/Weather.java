package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/***
 * now :实况天气
 */
public class Weather {
    private Basic basic;
    private Now now;
    private String status;
    private Update update;


    public static class Now{
        private String tmp;
        @SerializedName("cond_txt")
        private String describe;

        public String getTmp() {
            return tmp;
        }

        public void setTmp(String tmp) {
            this.tmp = tmp;
        }

        public String getDescribe() {
            return describe;
        }

        public void setDescribe(String describe) {
            this.describe = describe;
        }
    }

    public Basic getBasic() {
        return basic;
    }

    public void setBasic(Basic basic) {
        this.basic = basic;
    }

    public Now getNow() {
        return now;
    }

    public void setNow(Now now) {
        this.now = now;
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
