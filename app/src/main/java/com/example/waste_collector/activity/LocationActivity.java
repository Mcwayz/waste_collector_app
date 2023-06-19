package com.example.waste_collector.activity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.waste_collector.R;
import com.example.waste_collector.adapter.CustomAdapter;
import com.example.waste_collector.model.authentication.Auth;
import com.example.waste_collector.model.collections.Collection;
import com.example.waste_collector.service.ApiService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private double latitude;
    private double longitude;
    private String collectionId;
    private ArrayList<String> request_date, desired_date, assigned_date, is_collected;
    private ArrayList<Integer> task_id, collection_id;

    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        task_id = new ArrayList<>();
        collection_id = new ArrayList<>();
        request_date = new ArrayList<>();
        desired_date = new ArrayList<>();
        assigned_date = new ArrayList<>();
        is_collected = new ArrayList<>();

        // Get the latitude and longitude values

        longitude = Double.parseDouble(getIntent().getStringExtra("longitude"));
        latitude = Double.parseDouble(getIntent().getStringExtra("latitude"));
        address = getIntent().getStringExtra("address");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        LatLng location = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(location).title(address));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
        googleMap.setOnMarkerClickListener(marker -> {
           showAlertDialog();
           return false;
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Collection");
        builder.setMessage("Have you collected the waste");
        builder.setPositiveButton("Yes", (dialog, which) -> collect());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void collect(){
        collectionId = getIntent().getStringExtra("collection_id");
        int collection_id = Integer.parseInt(collectionId);
        Toast.makeText(LocationActivity.this, "Collection ID: " + collection_id, Toast.LENGTH_SHORT).show();
        Call<List<Collection>> call = ApiService.getWasteApiService().updateCollection(collection_id);
        call.enqueue(new Callback<List<Collection>>() {
            @Override
            public void onResponse(Call<List<Collection>> call, Response<List<Collection>> response) {
                if(response.isSuccessful()){
                    List<Collection> collectionList = response.body();
                    SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
                    SimpleDateFormat desiredFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                    for (Collection collection : collectionList) {
                        String requestDate = formatDateString(collection.getRequest_date());
                        String desiredDate = formatDateString(collection.getRequest_date());
                        is_collected.add("Collected: True");
                        task_id.add(collection.getTask_id());
                        request_date.add(requestDate);
                        desired_date.add(desiredDate);
                        assigned_date.add(formatDate(collection.getAssigned_date(), originalFormat, desiredFormat));
                        Log.d("Response", "ID: " + collection.getTask_id()+", R Date: " + collection.getRequest_date() + ", C Date: " + collection.getUser_collect_date());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Collection>> call, Throwable t) {
                Toast.makeText(LocationActivity.this, "Error Updating Data: " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Updating Data", "Error Msg: " + t.getLocalizedMessage());
            }
        });

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
}