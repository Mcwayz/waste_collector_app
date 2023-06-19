package com.example.waste_collector.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.waste_collector.R;
import com.example.waste_collector.model.authentication.Auth;
import com.example.waste_collector.model.profile.DetailsResponse;
import com.example.waste_collector.service.ApiService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private CardView cvCollection, cvHistory, cvProfile, cvOut;
    private String UserName;
    private int user_id, collector_id;
    private String auth_user_id;
    private ImageView imgProfile;
    private TextView cDate, Username;
    Dialog dialog;

    Auth auth;


    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d'" + getDayOfMonthSuffix(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) + "'", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private static String getDayOfMonthSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String currentDate = getCurrentDate();
        cDate = findViewById(R.id.tv_dash_date);
        Username = findViewById(R.id.tv_dash_profile_name);
        cvCollection = findViewById(R.id.cv_collection);
        cvHistory = findViewById(R.id.cv_history);
        cvProfile = findViewById(R.id.cv_profile);
        cvOut = findViewById(R.id.cv_logout);
        UserName = getIntent().getStringExtra("username");
        Username.setText("Hi "+UserName);
        cDate.setText(currentDate);
        imgProfile = findViewById(R.id.img_profile);

        cvOut.setOnClickListener(v -> {
            Paper.book().destroy();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cvProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("collector_id", collector_id);
            intent.putExtra("username", UserName);
            startActivity(intent);
        });

        cvHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CollectionsActivity.class);
            intent.putExtra("collector_id", collector_id);
            intent.putExtra("username", UserName);
            startActivity(intent);
        });

        cvCollection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,RequestsActivity.class);
            intent.putExtra("collector_id", collector_id);
            intent.putExtra("username", UserName);
            startActivity(intent);
        });


        auth = new Auth(getApplicationContext());

        String token = auth.getToken();
        auth.startRunnable();

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

        getProfile();

        imgProfile.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, imgProfile);
            popupMenu.getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());
            // Set click listener for menu items
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.menu_logout) {
                    // Handle logout action here
                    Paper.book().destroy();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });


    }

    private void getProfile(){
        Call<DetailsResponse> detailsResponseCall = ApiService.getWasteApiService().getProfile(Integer.parseInt(auth_user_id));
        detailsResponseCall.enqueue(new Callback<DetailsResponse>() {
            @Override
            public void onResponse(Call<DetailsResponse> call, Response<DetailsResponse> response) {
                if(response.isSuccessful()){
                    DetailsResponse detailsResponse = response.body();
                    if (detailsResponse != null) {
                        user_id = detailsResponse.getAuth();
                        collector_id = detailsResponse.getCollector_id();
                        SharedPreferences sharedPreferences = getSharedPreferences("my_app", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("profile_id", String.valueOf(user_id));
                        editor.putString("collector_id", String.valueOf(collector_id));
                        editor.apply();
                    }else{
                        Toast.makeText(MainActivity.this, "Error Returning Profile Info", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(Call<DetailsResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error Returning User Profile" +t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }


}