package com.example.waste_collector.model.profile;


public class DetailsResponse {


    private int collector_id;
    private String vehicle;
    private String work_area;
    private int auth;
    private String firstname;
    private String lastname;
    private String email;

    public DetailsResponse() {

    }


    public DetailsResponse(int collector_id, String vehicle, String work_area, int auth, String firstname, String lastname, String email) {
        this.collector_id = collector_id;
        this.vehicle = vehicle;
        this.work_area = work_area;
        this.auth = auth;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public int getCollector_id() {
        return collector_id;
    }

    public void setCollector_id(int collector_id) {
        this.collector_id = collector_id;
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

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "DetailsResponse{" +
                "collector_id=" + collector_id +
                ", vehicle='" + vehicle + '\'' +
                ", work_area='" + work_area + '\'' +
                ", auth=" + auth +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
