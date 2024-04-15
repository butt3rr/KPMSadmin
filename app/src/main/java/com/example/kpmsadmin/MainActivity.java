package com.example.kpmsadmin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText StaffID, Password;
    Button login;
    ProgressDialog progressDialog;
    UserManagement userManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StaffID = findViewById(R.id.log_username);
        Password = findViewById(R.id.log_password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        //userManagement = new UserManagement(this);

        login = findViewById(R.id.btnLogin);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserLoginProcess();
            }
        });

    }

    private void UserLoginProcess() {
        String staffID = StaffID.getText().toString().trim();
        String password = Password.getText().toString().trim();

        if (staffID.isEmpty() || password.isEmpty()) {
            message("Fill in all fields!");
        } else {
            progressDialog.show();
            StringRequest request = new StringRequest(Request.Method.POST, Urls.LOGIN_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String result = jsonObject.getString("status");

                                if (result.equals("success")) {
                                    progressDialog.dismiss();

                                    // Store username in SharedPreferences
                                    SharedPreferences.Editor editor = getSharedPreferences(UserManagement.PREF_NAME, MODE_PRIVATE).edit();
                                    editor.putString(UserManagement.staffID, staffID);
                                    editor.apply();

                                    Intent intent = new Intent(MainActivity.this, TrackingNumber.class);
                                    startActivity(intent);
                                    finish();
                                    message("Login Successful");
                                    //userManagement.checkLogin(); // Direct user to the appropriate activity


                                } else {
                                    progressDialog.dismiss();
                                    message("Staff ID or Password Incorrect!");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    progressDialog.dismiss();
                    message(error.getMessage());
                }
            }) {
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("staffID", staffID);
                    params.put("password", password);
                    return params;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            queue.add(request);
        }
    }


    public void message(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
