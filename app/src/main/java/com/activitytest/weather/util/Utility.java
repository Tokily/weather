package com.activitytest.weather.util;

import android.text.TextUtils;

import com.activitytest.weather.db.City;
import com.activitytest.weather.db.County;
import com.activitytest.weather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    public static boolean acquireProvince(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvince=new JSONArray(response);
                for(int i=0;i<allProvince.length();i++){
                    JSONObject jsProvince=allProvince.getJSONObject(i);
                    Province province=new Province();
                    province.setCode(jsProvince.getInt("id"));
                    province.setName(jsProvince.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean acquireCity(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCity=new JSONArray(response);
                for(int i=0;i<allCity.length();i++) {
                    JSONObject jsCity = allCity.getJSONObject(i);
                    City city=new City();
                    city.setCode(jsCity.getInt("id"));
                    city.setName(jsCity.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean acquireCounty(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounty=new JSONArray(response);
                for (int i=0;i<allCounty.length();i++){
                    JSONObject jsCounty=allCounty.getJSONObject(i);
                    County county=new County();
                    county.setCityId(cityId);
                    county.setName(jsCounty.getString("name"));
                    county.setWeatherId(jsCounty.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
