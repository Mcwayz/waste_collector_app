package com.example.waste_collector.model.profile;

public class ProfileRequest {

    private String vehicle;
    private String work_area;
    private int auth;

    public ProfileRequest() {

    }

    public ProfileRequest(String vehicle, String work_area, int auth) {
        this.vehicle = vehicle;
        this.work_area = work_area;
        this.auth = auth;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getWork_area() {
        return work_area;
    }

    public void setWork_area(String work_area) {
        this.work_area = work_area;
    }

    public int getAuth() {
        return auth;
    }

    public void setAuth(int auth) {
        this.auth = auth;
    }

    @Override
    public String toString() {
        return "ProfileRequest{" +
                "vehicle='" + vehicle + '\'' +
                ", work_area='" + work_area + '\'' +
                ", auth=" + auth +
                '}';
    }
}
