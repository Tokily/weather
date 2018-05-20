package com.activitytest.weather.db;


import org.litepal.crud.DataSupport;

public class County extends DataSupport {
    private int id;
    private String name;
    private int WeatherId;
    private int CityId;

    public int getCityId() {
        return CityId;
    }

    public void setCityId(int cityId) {
        CityId = cityId;
    }

    public int getWeatherId() {
        return WeatherId;
    }

    public void setWeatherId(int weatherId) {
        WeatherId = weatherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
