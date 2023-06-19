package com.example.waste_collector.activity;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.waste_collector.R;
import com.example.waste_collector.model.authentication.Auth;
import com.example.waste_collector.model.profile.DetailsResponse;
import com.example.waste_collector.model.profile.ProfileRequest;
import com.example.waste_collector.model.profile.ProfileResponse;
import com.example.waste_collector.service.ApiService;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Map;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private Dialog dialog;
    private String auth_id;
    private Button update;
    private ImageView imgBack;
    private TextInputEditText work_area, vehicle, auth_t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth_t = findViewById(R.id.tf_auth);
        update = findViewById(R.id.btn_update);
        vehicle = findViewById(R.id.tf_vehicle);
        imgBack = findViewById(R.id.img_back_his);
        work_area = findViewById(R.id.tf_work_area);

        Auth auth = new Auth(getApplicationContext());
        auth.startRunnable();
        String token = auth.getToken();

        if (token.isEmpty()) {
            Log.e("TAG", "No auth token found in shared preferences");
            return;
        }
        try {
            DecodedJWT jwt = JWT.decode(token);
            Map<String, Claim> claims = jwt.getClaims();
            auth_id = String.valueOf(claims.get("user_id"));
            Log.d("TAG", "Auth ID: " + auth_id);
            String IdWithoutQuotes = auth_id.replace("\"", "");
            auth_id = IdWithoutQuotes;
            auth_t.setText(auth_id);
            getProfile();
        } catch (JWTDecodeException exception){
            Log.e("TAG", "Invalid JWT token: " + exception.getMessage());
        }

        update.setOnClickListener(v -> validate());

        imgBack.setOnClickListener(v -> goBack());
    }

    // Function that validates user entries
    private void validate(){
        String vehicle_details, work;
        vehicle_details = Objects.requireNonNull(vehicle.getText()).toString();
        work = Objects.requireNonNull(work_area.getText()).toString();
        if (TextUtils.isEmpty(vehicle_details)) {
            Toast.makeText(this, "Please Enter Vehicle Details (Model & Plate)", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(work)) {
            Toast.makeText(this, "Please Enter Collection Area (e.g.. Kabwata - Libala)", Toast.LENGTH_SHORT).show();
        }
        else {
            updateProfile(getDetails());
            dialog = new Dialog(ProfileActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_wait2);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private ProfileRequest getDetails(){
        ProfileRequest profileRequest = new ProfileRequest();
        if(Objects.requireNonNull(vehicle.getText()).toString().equals("")) {
            Toast.makeText(this, "Please Enter Vehicle Details (Model & Plate)", Toast.LENGTH_SHORT).show();
        }else if(Objects.requireNonNull(auth_t.getText()).toString().equals("")){
            Toast.makeText(this, "Please Enter User ID", Toast.LENGTH_SHORT).show();
        }else if(Objects.requireNonNull(work_area.getText()).toString().equals("")){
            Toast.makeText(this, "Please Enter Collection Area (e.g.. Kabwata - Libala)", Toast.LENGTH_SHORT).show();
        }
        else{
            profileRequest.setAuth(Integer.parseInt(auth_id));
            profileRequest.setVehicle(vehicle.getText().toString());
            profileRequest.setWork_area(work_area.getText().toString());
        }
        return profileRequest;
    }

    // Function that posts user profile details
    private void getProfile(){
        Call<DetailsResponse> detailsResponseCall = ApiService.getWasteApiService().getProfile(Integer.parseInt(auth_id));
        detailsResponseCall.enqueue(new Callback<DetailsResponse>() {
            @Override
            public void onResponse(Call<DetailsResponse> call, Response<DetailsResponse> response) {
                if(response.isSuccessful()){
                    DetailsResponse detailsResponse = response.body();
                    if (detailsResponse != null) {
                        String w_vehicle = detailsResponse.getVehicle();
                        String w_area = detailsResponse.getWork_area();
                        int user_id = detailsResponse.getAuth();
                        auth_t.setText(String.valueOf(user_id));
                        vehicle.setText(w_vehicle);
                        work_area.setText(w_area);
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                        builder.setTitle("No Collector Details");
                        builder.setMessage("Provide Collector Details");
                        builder.setPositiveButton("Okay", (dialog, which) -> {
                            dialog.dismiss();
                        });
                        builder.show();
                    }
                }
            }

            @Override
            public void onFailure(Call<DetailsResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error Returning Collector Profile" +t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                goBack();
            }
        });
    }

    // Function that updates profile details
    private void updateProfile(ProfileRequest profileRequest)
    {
        Call<ProfileResponse> call = ApiService.getWasteApiService().updateProfile(profileRequest);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                String success; int auth;
                if(response.isSuccessful()){
                    ProfileResponse profileResponse = response.body();
                    auth = profileResponse.getAuth();
                    success = profileResponse.getSuccess();
                    if(success.equals("true")){
                        Log.d(TAG, "Auth ID: "+auth);
                        Toast.makeText(ProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        goBack();
                    }else{
                        Toast.makeText(ProfileActivity.this, "Profile Update Failed", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        goBack();
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Failed to Reach the Server", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private void goBack(){
        Intent i = new Intent(ProfileActivity.this, MainActivity.class);
        String username = getIntent().getStringExtra("username");
        i.putExtra("username", username);
        startActivity(i);
        finish();
    }
}