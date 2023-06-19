package com.example.waste_collector.activity;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.waste_collector.R;
import com.example.waste_collector.adapter.CustomAdapter;
import com.example.waste_collector.model.authentication.Auth;
import com.example.waste_collector.model.collections.Collection;
import com.example.waste_collector.model.profile.DetailsResponse;
import com.example.waste_collector.rest_client.RecyclerViewInterface;
import com.example.waste_collector.service.ApiService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestsActivity extends AppCompatActivity implements RecyclerViewInterface {

    private RecyclerView recyclerView;
    private CustomAdapter customAdapter;
    private ArrayList<String> address, request_date, desired_date, assigned_date, is_collected, longitude, latitude;
    private ArrayList<Integer> task_id, collection_id;
    private ImageView imgBack;
    private String auth_user_id, profID, collectionId;
    private Auth auth;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        recyclerView = findViewById(R.id.recyclerView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        imgBack = findViewById(R.id.img_back_his);
        task_id = new ArrayList<>();
        collection_id = new ArrayList<>();
        address = new ArrayList<>();
        request_date = new ArrayList<>();
        desired_date = new ArrayList<>();
        assigned_date = new ArrayList<>();
        is_collected = new ArrayList<>();
        longitude = new ArrayList<>();
        latitude = new ArrayList<>();



        getRequests();

        auth = new Auth(getApplicationContext());
        String token = auth.getToken();
        profID = auth.getProfileID();
        auth.startRunnable();

        getProfile();

        if (token.isEmpty()) {
            Log.e("TAG", "No auth token found in shared preferences");
            return;
        }

        try {
            DecodedJWT jwt = JWT.decode(token);
            Map<String, Claim> claims = jwt.getClaims();
            String auth_id = String.valueOf(claims.get("user_id"));
            Log.d("TAG", "Auth ID: " + auth_id);
            String IdWithoutQuotes = auth_id.replace("\"", "");
            auth_user_id = IdWithoutQuotes;
        } catch (JWTDecodeException exception) {
            Log.e("TAG", "Invalid JWT token: " + exception.getMessage());
        }

        imgBack.setOnClickListener(view -> {
            goBack();
        });

        imgBack.setOnClickListener(v -> goBack());
    }

    private void goBack(){
        Intent i = new Intent(RequestsActivity.this, MainActivity.class);
        String username = getIntent().getStringExtra("username");
        i.putExtra("username", username);
        startActivity(i);
        finish();
    }

    private void getProfile(){
        Call<DetailsResponse> detailsResponseCall = ApiService.getWasteApiService().getProfile(Integer.parseInt(profID));
        detailsResponseCall.enqueue(new Callback<DetailsResponse>() {
            @Override
            public void onResponse(Call<DetailsResponse> call, Response<DetailsResponse> response) {
                if(response.isSuccessful()){
                    DetailsResponse detailsResponse = response.body();
                    if (detailsResponse != null) {
                        String work = detailsResponse.getWork_area();
                        String work_vehicle = detailsResponse.getVehicle();
                        Toast.makeText(RequestsActivity.this, "Work Area: "+work+" Vehicle :"+work_vehicle, Toast.LENGTH_SHORT).show();
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(RequestsActivity.this);
                        builder.setTitle("No Profile Details");
                        builder.setMessage("Provide Details in the Profile Section!");
                        builder.setPositiveButton("Okay", (dialog, which) -> {
                            dialog.dismiss();
                            Intent intent = new Intent(RequestsActivity.this, ProfileActivity.class);
                            startActivity(intent);
                        });
                        builder.show();
                    }
                }
            }

            @Override
            public void onFailure(Call<DetailsResponse> call, Throwable t) {
                Toast.makeText(RequestsActivity.this, "Error Returning User Profile" +t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                goBack();
            }
        });
    }

    private void getRequests() {
        auth = new Auth(getApplicationContext());
        collectionId = auth.getCollectorID();
        try {
            int prof_id = Integer.parseInt(collectionId);
            Toast.makeText(RequestsActivity.this, "Collector ID: " + prof_id, Toast.LENGTH_SHORT).show();
            Call<List<Collection>> call = ApiService.getWasteApiService().getRequests(prof_id);
            call.enqueue(new Callback<List<Collection>>() {
                @Override
                public void onResponse(Call<List<Collection>> call, Response<List<Collection>> response) {
                    if(response.isSuccessful()){
                        List<Collection> collectionList = response.body();
                        // Create a SimpleDateFormat instance for parsing the original date format
                        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
                        // Create a SimpleDateFormat instance for formatting the desired date format
                        SimpleDateFormat desiredFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

                        for (Collection collection : collectionList) {

                            String requestDate = formatDateString(collection.getRequest_date());
                            String desiredDate = formatDateString(collection.getRequest_date());
                            is_collected.add("Collected: False");
                            task_id.add(collection.getTask_id());
                            address.add(collection.getAddress());
                            latitude.add(collection.getLatitude());
                            longitude.add(collection.getLongitude());
                            collection_id.add(collection.getCollection_id());
                            request_date.add(requestDate);
                            desired_date.add(desiredDate);
                            assigned_date.add(formatDate(collection.getAssigned_date(), originalFormat, desiredFormat));


                            Log.d("Response", "ID: " + collection.getTask_id() + ", Address: " + collection.getAddress() + ", R Date: " + collection.getRequest_date() + ", C Date: " + collection.getUser_collect_date());
                        }

                        runOnUiThread(() -> {
                            recyclerView.setLayoutManager(new LinearLayoutManager(RequestsActivity.this));
                            customAdapter = new CustomAdapter(RequestsActivity.this, task_id, collection_id, address, assigned_date, desired_date, is_collected, RequestsActivity.this);
                            recyclerView.setAdapter(customAdapter);
                            Toast.makeText(RequestsActivity.this, "Your Pending Tasks!", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<Collection>> call, Throwable t) {
                    Toast.makeText(RequestsActivity.this, "Error Fetching Data: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("Collection Data", "Error Msg: " + t.getLocalizedMessage());
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(RequestsActivity.this, "Invalid collector ID", Toast.LENGTH_SHORT).show();
            Log.d("Collection Data", "Error Msg: " + e.getMessage());
        }
    }

    public static String formatDateString(String inputDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSXXX");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        try {
            Date date = inputFormat.parse(inputDate);
            outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if there's an error
    }


    private String formatDate(String date, SimpleDateFormat originalFormat, SimpleDateFormat desiredFormat) {
        try {
            Date originalDate = originalFormat.parse(date);
            return desiredFormat.format(originalDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onItemClick(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(RequestsActivity.this);
        builder.setTitle("Collection Details");
        builder.setMessage(
                "Collection Address : "+address.get(position)+ "\n" +
                "Assigned Date : "+assigned_date.get(position)+ "\n" +
                "Collection By : "+desired_date.get(position)+ "\n" +
                "Longitude : "+longitude.get(position)+ "\n" +
                "Latitude : "+latitude.get(position)
                );
        builder.setPositiveButton("View Location", (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent(RequestsActivity.this, LocationActivity.class);
            intent.putExtra("task_id", task_id.get(position));
            intent.putExtra("collection_id", collection_id.get(position));
            intent.putExtra("address", address.get(position));
            intent.putExtra("desired_date", desired_date.get(position));
            intent.putExtra("assigned_date", assigned_date.get(position));
            intent.putExtra("is_collected", is_collected.get(position));
            intent.putExtra("longitude", longitude.get(position));
            intent.putExtra("latitude", latitude.get(position));
            startActivity(intent);
        });
        builder.show();


    }
}
