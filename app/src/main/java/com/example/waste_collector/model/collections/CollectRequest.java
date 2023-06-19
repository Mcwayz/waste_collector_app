package com.example.waste_collector.model.collections;

public class CollectRequest {
    private int collection_id;

    public CollectRequest() {
    }

    public CollectRequest(int collection_id) {
        this.collection_id = collection_id;
    }

    public int getCollection_id() {
        return collection_id;
    }

    public void setCollection_id(int collection_id) {
        this.collection_id = collection_id;
    }

    @Override
    public String toString() {
        return "CollectRequest{" +
                "collection_id=" + collection_id +
                '}';
    }
}
