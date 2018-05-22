package com.activitytest.weather;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activitytest.weather.db.City;
import com.activitytest.weather.db.County;
import com.activitytest.weather.db.Province;
import com.activitytest.weather.util.HttpUtil;
import com.activitytest.weather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment{
    public static final int LIVE_PROVINCE=0;
    public static final int LIVE_CITY=1;
    public static final int LIVE_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView textView;
    private Button button;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province currentProvince;
    private City currentCity;
    private int currentLevel;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose,container,false);
        textView=view.findViewById(R.id.title);
        button=view.findViewById(R.id.back);
        listView=view.findViewById(R.id.list);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentLevel==LIVE_PROVINCE){
                    currentProvince=provinceList.get(i);
                    queryCities();
                }else if(currentLevel==LIVE_CITY){
                    currentCity=cityList.get(i);
                    queryCounties();
                }else if(currentLevel==LIVE_COUNTY){
                    String weatherId=countyList.get(i).getWeatherId();
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel==LIVE_COUNTY){
                    queryCities();
                }else if(currentLevel==LIVE_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    private void queryProvinces(){
        textView.setText("中国");
        button.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LIVE_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromService(address,"province");
        }
    }
    private void queryCities(){
        textView.setText(currentProvince.getName());
        button.setVisibility(View.VISIBLE);
        cityList= DataSupport.where("provinceid=?",String.valueOf(currentProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LIVE_CITY;
        }else {
            int provinceCode=currentProvince.getCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromService(address,"city");
        }
    }
    public void queryCounties(){
        textView.setText(currentCity.getName());
        button.setVisibility(View.VISIBLE);
        countyList= DataSupport.where("cityid=?",String.valueOf(currentCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LIVE_COUNTY;
        }else {
            int provinceCode=currentProvince.getCode();
            int cityCode=currentCity.getCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromService(address,"county");
        }
    }
    private void queryFromService(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkhttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.acquireProvince(responseText);
                }else if("city".equals(type)){
                    result=Utility.acquireCity(responseText,currentProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.acquireCounty(responseText,currentCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }
    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载……");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
