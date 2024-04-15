package com.example.kpmsadmin;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TrackingNumber extends AppCompatActivity {

    //declare
    public static final String url = Urls.INSERTPARCEL_URL;
    EditText CustName, CustPhoneNum, TrackingNumber;
    Spinner CourierName;
    TextView Feedback;
    Button Submit, Reset;

    private MaterialButton inputImagebtn;
    private MaterialButton recognizeTextBtn;
    private ShapeableImageView imageIv;

    //TAG
    private static final String TAG = "MAIN_TAG";
    //uri of image we take from camera/gallery
    private Uri imageUri = null;

    private ImageView userpic;
    private final int STORAGE_REQUEST = 200;
    private String[] storagePermission;
    //private Uri imageUri;

    //array of permission to pick image
    private String[] cameraPermissions;
    private String[] storagePermissions;

    //progress dialog
    private ProgressDialog progressDialog;

    //text recognizer
    private TextRecognizer textRecognizer;

    private DrawerLayout drawerLayout;
    private ImageView ivMenu;
    private RecyclerView rvMenu;
    //array utk simpan gmbr
    static ArrayList<String> arrayList = new ArrayList<>();
    static ArrayList<Integer> image = new ArrayList<>();
    private MainDrawerAdapter adapter;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_number);

        inputImagebtn = findViewById(R.id.inputImagebtn);
        recognizeTextBtn = findViewById(R.id.recognizeTextBtn);
        imageIv = findViewById(R.id.imageIv);
        TrackingNumber = (EditText) findViewById(R.id.etTrackingNumber);

        CustName = (EditText) findViewById(R.id.etName);
        CustPhoneNum = (EditText) findViewById(R.id.etPhoneNumber);
        CourierName = (Spinner) findViewById(R.id.spinnerCourierName);

        Submit = (Button) findViewById(R.id.btnSubmit);
        Reset = (Button) findViewById(R.id.btnReset);

        drawerLayout = findViewById(R.id.drawer_layout);
        ivMenu = findViewById(R.id.ivMenu);
        rvMenu = findViewById(R.id.rvMenu);

        arrayList.clear();

        arrayList.add("ADD NEW PARCEL");
        arrayList.add("PARCEL HISTORY");
        arrayList.add("LOG OUT");

        image.add(R.drawable.baseline_move_to_inbox_24);
        image.add(R.drawable.baseline_history_24);
        image.add(R.drawable.baseline_login_24);

        adapter = new MainDrawerAdapter(this, arrayList, image);

        rvMenu.setLayoutManager(new LinearLayoutManager(this));

        rvMenu.setAdapter(adapter);

        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputData();
            }
        });

        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields();
            }
        });


        storagePermission = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //textRecognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //handle click, show input image dialog
        inputImagebtn.setOnClickListener(v -> showImagePicDialog());

        recognizeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null) {
                    Toast.makeText(TrackingNumber.this, "Pick Image First...", Toast.LENGTH_SHORT).show();
                } else {
                    recognizeTextFromImage();
                }
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.courier_names_array, // Assuming you have defined an array resource with courier names
                android.R.layout.simple_spinner_item // Use a default spinner layout
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        CourierName.setAdapter(adapter);


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("CROPPED_IMAGE_URI")) {
            String croppedImageUriString = intent.getStringExtra("CROPPED_IMAGE_URI");
            if (croppedImageUriString != null) {
                imageUri = Uri.parse(croppedImageUriString);
                imageIv.setImageURI(imageUri);
            }
        }
    }

    private void showImagePicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Pick Image From")
                .setPositiveButton("Gallery/Camera", (dialog, which) -> {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    private void pickFromGallery() {
        CropImage.activity().start(TrackingNumber.this);
    }

    private void requestStoragePermission() {
        requestPermissions(storagePermission, STORAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST) {
            if (grantResults.length > 0) {
                boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (writeStorageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, "PLEASE ENABLE STORAGE PERMISSION", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Intent intent = new Intent(this, TrackingNumber.class);
                intent.putExtra("CROPPED_IMAGE_URI", resultUri.toString());
                startActivity(intent);
            }
        }
    }

    private void recognizeTextFromImage() {
        Log.d(TAG, "recognizeTextFromImage: ");
        progressDialog.setMessage("Preparing Image...");
        progressDialog.show();

        try {
            InputImage inputImage = InputImage.fromFilePath(this, imageUri);

            progressDialog.setMessage("Recognizing Text..");

            Task<Text> textTaskResult = textRecognizer.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {

                            progressDialog.dismiss();
                            String recognizedText = text.getText();
                            Log.d(TAG, "onSuccess: recognizedText: " + recognizedText);
                            TrackingNumber.setText(recognizedText);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Log.e(TAG, "onFailure: ", e);
                            Toast.makeText(TrackingNumber.this, "Failed recognizing text due to" + e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "recognizeTextFromImage ", e);
            Toast.makeText(this, "Failed preparing image due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void inputData() {
        // Retrieve data from fields
        String custName = CustName.getText().toString().trim();
        String custPhoneNum = CustPhoneNum.getText().toString().trim();
        String trackingNumber = TrackingNumber.getText().toString().trim();
        String courierName = CourierName.getSelectedItem().toString();

        // Validate if all fields are filled
        if (custName.isEmpty() || trackingNumber.isEmpty() || courierName.isEmpty()) {
            // Display error message if any field is empty
            Toast.makeText(TrackingNumber.this, "All fields must be filled in", Toast.LENGTH_SHORT).show();
        }
        else if (custPhoneNum.isEmpty() || (isValidPhoneNumber(custPhoneNum) && isValidName(custName))) {

            submitForm(custName, custPhoneNum, trackingNumber, courierName);

        }
        else if (!isValidName(custName) || custPhoneNum.isEmpty() ) {
            Toast.makeText(TrackingNumber.this, "Customer name must contain only alphabetic characters", Toast.LENGTH_SHORT).show();
        }
        else if(!isValidPhoneNumber(custPhoneNum)) {
            Toast.makeText(TrackingNumber.this, "Please enter a valid phone number starting with '01' and having 10-11 digits", Toast.LENGTH_SHORT).show();



        }

    }

    private boolean isValidName(String custName) {
        String regex = "^[a-zA-Z]+$";
        return custName.matches(regex);
    }


    private void submitForm(String custName, String custPhoneNum, String trackingNumber, String courierName) {
        String url = Urls.INSERTPARCEL_URL;

        // Assuming you're using Volley, prepare the request.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // This block is executed upon a successful response.
                    if (response.contains("Error: Tracking number already exists!")) {
                        // Show AlertDialog for existing tracking number
                        new AlertDialog.Builder(TrackingNumber.this)
                                .setTitle("Duplicate Tracking Number")
                                .setMessage("The tracking number already exists. Please use a different tracking number.")
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                    } else {
                        // Handle successful form submission (tracking number is unique)
                        Toast.makeText(TrackingNumber.this, response, Toast.LENGTH_LONG).show();
                        // Optionally, redirect the user or clear the form fields
                    }
                },
                error -> {
                    // Handle network error here
                    Toast.makeText(TrackingNumber.this, "Failed to communicate with the server!", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("custName", custName);
                params.put("custPhoneNum", custPhoneNum);
                params.put("trackingNumber", trackingNumber);
                params.put("courierName", courierName);
                return params;
            }
        };

        // Add the request to your RequestQueue.
        Volley.newRequestQueue(this).add(stringRequest);
    }


    private boolean isValidPhoneNumber(String custPhoneNum) {
        String regex = "^01[0-9]{8,9}$";
        return custPhoneNum.matches(regex);
    }

    private void resetFields() {
        CustName.setText("");
        CustPhoneNum.setText("");
        TrackingNumber.setText("");
        CourierName.setSelection(0);

        imageIv.setImageResource(R.drawable.baseline_image_24);

    }


    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

}
