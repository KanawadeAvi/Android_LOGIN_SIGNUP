package com.example.crud;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayOutputStream;
import org.json.JSONObject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private ImageView photoView;
    private Bitmap photoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText username = findViewById(R.id.etUsername);
        EditText email = findViewById(R.id.etEmail);
        EditText password = findViewById(R.id.etPassword);
        Button uploadButton = findViewById(R.id.btnUpload);
        Button signupButton = findViewById(R.id.btnSignup);
        photoView = findViewById(R.id.imgPhoto);

        // Open the camera to capture the photo
        uploadButton.setOnClickListener(v -> startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 100));

        // Handle signup action
        signupButton.setOnClickListener(v -> {
            String user = username.getText().toString().trim();
            String mail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String photoString = photoBitmap != null ? encodeImage(photoBitmap) : "";

            // Log field values
            Log.d(TAG, "Username: " + user);
            Log.d(TAG, "Email: " + mail);
            Log.d(TAG, "Password: " + pass);
            Log.d(TAG, "Photo (length): " + (photoString.length()));

            if (user.isEmpty() || mail.isEmpty() || pass.isEmpty() || photoString.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill all fields, including uploading a photo.", Toast.LENGTH_SHORT).show();
            } else {
                signupUser(user, mail, pass, photoString);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            photoBitmap = (Bitmap) data.getExtras().get("data");
            photoView.setImageBitmap(photoBitmap);
        }
    }

    private String encodeImage(Bitmap bitmap) {
        if (bitmap == null) return ""; // Ensure bitmap is not null
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void signupUser(String username, String email, String password, String photoString) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.signupUser(username, email, password, photoString);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Server Response: " + responseBody);

                        JSONObject jsonResponse = new JSONObject(responseBody);

                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(SignupActivity.this, "Signup Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            String error = jsonResponse.optString("error", "Unknown error");
                            Toast.makeText(SignupActivity.this, "Signup Failed: " + error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                        Toast.makeText(SignupActivity.this, "Error parsing server response.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Server error: " + response.code());
                    Toast.makeText(SignupActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(SignupActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
