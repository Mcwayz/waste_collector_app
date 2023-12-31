package com.example.waste_collector.model.collections;

public class CollectionRequest {

    private int user;
    private String request_date;
    private boolean is_collected;
    private String collection_date;

    public CollectionRequest() {
    }

    public CollectionRequest(int user, String request_date, boolean is_collected, String collection_date) {
        this.user = user;
        this.request_date = request_date;
        this.is_collected = is_collected;
        this.collection_date = collection_date;
    }

    public int getUser_id() {
        return user;
    }

    public void setUser_id(int user) {
        this.user = user;
    }

    public String getRequest_date() {
        return request_date;
    }

    public void setRequest_date(String request_date) {
        this.request_date = request_date;
    }

    public boolean isIs_collected() {
        return is_collected;
    }

    public void setIs_collected(boolean is_collected) {
        this.is_collected = is_collected;
    }

    public String getCollection_date() {
        return collection_date;
    }

    public void setCollection_date(String collection_date) {
        this.collection_date = collection_date;
    }

    @Override
    public String toString() {
        return "CollectionRequest{" +
                "user=" + user +
                ", request_date='" + request_date + '\'' +
                ", is_collected=" + is_collected +
                ", collection_date='" + collection_date + '\'' +
                '}';
    }
}
