package com.example.kpmsadmin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryHolder> {

    Context context;
    List<History> historyList;

    public HistoryAdapter(Context context, List<History> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View historyLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list, parent, false);
        return new HistoryHolder(historyLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
        History history = historyList.get(position);
        holder.TrackingNumber.setText(history.getTrackingNumber());
        holder.CustName.setText(history.getCustName());
        holder.CustPhoneNum.setText(history.getCustPhoneNum());
        holder.CourierName.setText(history.getCourierName());
        holder.Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View editLayout = LayoutInflater.from(context).inflate(R.layout.edit_parcel, null);

                EditText TrackingNumber = editLayout.findViewById(R.id.edit_trackingNumber);
                EditText CustName = editLayout.findViewById(R.id.edit_custName);
                EditText CustPhoneNum = editLayout.findViewById(R.id.edit_custPhoneNum);
                Spinner CourierName = editLayout.findViewById(R.id.spinnerCourierName);

                // Populate the Spinner with courier names from resources
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                        R.array.courier_names_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                CourierName.setAdapter(adapter);

                TrackingNumber.setText(history.getTrackingNumber());
                CustName.setText(history.getCustName());
                CustPhoneNum.setText(history.getCustPhoneNum());
                CourierName.setSelection(adapter.getPosition(history.getCourierName()));

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Edit " + history.getTrackingNumber());
                builder.setView(editLayout);
                builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        final String trackingNumber = TrackingNumber.getText().toString();
                        final String custName = CustName.getText().toString();
                        final String custPhoneNum = CustPhoneNum.getText().toString();
                        final String courierName = CourierName.getSelectedItem().toString();
                        final String oldTrackingNumber = history.getTrackingNumber();


                        if(trackingNumber.isEmpty() || custName.isEmpty() || courierName.isEmpty()) {
                            Toast.makeText(context, "Fill in the fields", Toast.LENGTH_SHORT).show();
                        } else if (custPhoneNum.isEmpty() || isValidPhoneNumber(custPhoneNum)) {
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.EDITPARCEL_URL,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    HashMap<String, String> params = new HashMap<>();
                                    params.put("trackingNumber", trackingNumber);
                                    params.put("custName", custName);
                                    params.put("custPhoneNum", custPhoneNum);
                                    params.put("courierName", courierName);
                                    params.put("oldTrackingNumber", oldTrackingNumber);
                                    return params;
                                }
                            };
                            RequestQueue queue = Volley.newRequestQueue(context);
                            queue.add(stringRequest);
                        } else if (!isValidPhoneNumber(custPhoneNum)) {
                            Toast.makeText(context, "Please enter a valid phone number starting with '01' and having 10-11 digits", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        holder.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("DELETE PARCEL");
                builder.setMessage("Confirm to Delete Parcel " + history.getTrackingNumber());
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.DELETEPARCEL_URL,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject object = new JSONObject(response);
                                            String check = object.getString("state");
                                            if (check.equals("delete")) {
                                                Delete(position);
                                                Toast.makeText(context, "Parcel Delete Successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Nullable
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                HashMap<String, String> deleteParams = new HashMap<>();
                                deleteParams.put("trackingNumber", history.getTrackingNumber());
                                return deleteParams;
                            }
                        };
                        RequestQueue requestQueue = Volley.newRequestQueue(context);
                        requestQueue.add(stringRequest);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private boolean isValidPhoneNumber(String custPhoneNum) {
        String regex = "^01[0-9]{8,9}$";
        return custPhoneNum.matches(regex);
    }


    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class HistoryHolder extends RecyclerView.ViewHolder {
        TextView TrackingNumber, CustName, CustPhoneNum, CourierName;
        Button Edit;
        ImageButton Delete;

        public HistoryHolder(@NonNull View itemView) {
            super(itemView);
            TrackingNumber = itemView.findViewById(R.id.rcy_trackingNumber);
            CustName = itemView.findViewById(R.id.rcy_custName);
            CustPhoneNum = itemView.findViewById(R.id.rcy_custPhoneNum);
            CourierName = itemView.findViewById(R.id.rcy_courierName);
            Edit = itemView.findViewById(R.id.rcy_edit);
            Delete = itemView.findViewById(R.id.rcy_delete);
        }
    }

    public void Delete(int item) {
        historyList.remove(item);
        notifyItemRemoved(item);
    }

    public void updateList(List<History> newList) {
        historyList = new ArrayList<>();
        historyList.addAll(newList);
        notifyDataSetChanged();
    }
}
