package com.activitytest.weather.gson;


import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("city")
    public String city;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
