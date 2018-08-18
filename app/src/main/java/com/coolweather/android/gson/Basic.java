package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
        private String cid;
        @SerializedName("location")
        private String cName;

        public String getCid() {
            return cid;
        }

        public void setCid(String cid) {
            this.cid = cid;
        }

        public String getcName() {
            return cName;
        }

        public void setcName(String cName) {
            this.cName = cName;
        }

}
