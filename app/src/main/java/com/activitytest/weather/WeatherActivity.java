package com.activitytest.weather;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.activitytest.weather.gson.Forecast;
import com.activitytest.weather.gson.Weather;
import com.activitytest.weather.util.HttpUtil;
import com.activitytest.weather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView updateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqi;
    private TextView pm25;
    private TextView comfort;
    private TextView carWash;
    private TextView sport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        weatherLayout=findViewById(R.id.weather_layout);
        titleCity=findViewById(R.id.title);
        updateTime=findViewById(R.id.updateTime);
        degreeText=findViewById(R.id.degree);
        weatherInfoText=findViewById(R.id.weather);
        forecastLayout=findViewById(R.id.forecast_List);
        aqi=findViewById(R.id.aqi);
        pm25=findViewById(R.id.pm25);
        comfort=findViewById(R.id.comfort);
        carWash=findViewById(R.id.wash);
        sport=findViewById(R.id.sport);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=preferences.getString("weather",null);
        if(weatherString!=null){
            Weather weather= Utility.acquireWeather(weatherString);
            showWeatherInfo(weather);
        }else {
            String weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(weatherId);
        }
    }

    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=001a1551664541c593b5850deaa2aa9d";
        HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"acquire weather error!",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather=Utility.acquireWeather(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"acquire weather error!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    private void showWeatherInfo(Weather weather) {
        titleCity.setText(weather.basic.city);
        updateTime.setText(weather.basic.update.updateTime.split(" ")[1]);
        degreeText.setText(weather.now.temperature+"℃");
        weatherInfoText.setText(weather.now.more.info);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecasts){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView date=view.findViewById(R.id.dateFuture);
            TextView info=view.findViewById(R.id.info);
            TextView max=view.findViewById(R.id.max);
            TextView min=view.findViewById(R.id.min);
            date.setText(forecast.date);
            info.setText(forecast.more.info);
            max.setText(forecast.temperature.max);
            min.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqi.setText(weather.aqi.city.aqi);
            pm25.setText(weather.aqi.city.pm25);
        }
        comfort.setText("舒适度："+weather.suggestion.comfort.info);
        carWash.setText("洗车指数："+weather.suggestion.carWash.info);
        sport.setText("运动建议："+weather.suggestion.sport.info);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}
