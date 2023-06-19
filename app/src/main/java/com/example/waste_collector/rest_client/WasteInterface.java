package com.example.waste_collector.rest_client;


import com.example.waste_collector.model.authentication.TokenRequest;
import com.example.waste_collector.model.authentication.TokenResponse;
import com.example.waste_collector.model.collections.Collection;
import com.example.waste_collector.model.collections.CollectionRequest;
import com.example.waste_collector.model.collections.CollectionResponse;
import com.example.waste_collector.model.profile.DetailsResponse;
import com.example.waste_collector.model.profile.ProfileRequest;
import com.example.waste_collector.model.profile.ProfileResponse;
import com.example.waste_collector.model.profile.UserRequest;
import com.example.waste_collector.model.profile.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface WasteInterface {

    String base_url = "http://192.168.8.107:8000";
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })

    // Get Waste Types
    @GET("/api/waste-types")
    Call<String> getWasteType();

    // Registration Endpoint
    @POST("/api/add-user/")
    Call<UserResponse> getUser(@Body UserRequest userRequest);

    // Login Endpoint
    @POST("/api/token/")
    Call<TokenResponse> getAuthToken(@Body TokenRequest tokenRequest);

    // Profile Endpoint
    @POST("/api/add-profile")
    Call<ProfileResponse> updateProfile(@Body ProfileRequest profileRequest);

    // Profile Details Endpoint
    @GET("/api/collector/{authId}/")
    Call<DetailsResponse> getProfile(@Path("authId") int authId);

    // My Subscription Endpoint
    @GET("/api/my-tasks/{authId}/")
    Call<List<Collection>>  getRequests(@Path("authId") int authId);

    @GET("/api/my-collections/{authId}/")
    Call<List<Collection>>  getCollections(@Path("authId") int authId);

    @PUT("/api/update-collection/{authId}/")
    Call<List<Collection>>  updateCollection(@Path("authId") int authId);



}
