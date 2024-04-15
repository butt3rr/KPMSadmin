package com.example.kpmsadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private List<History> historyList;
    private List<History> filteredList = new ArrayList<>();

    private SearchView searchView;

    private DrawerLayout drawerLayout;
    private ImageView ivMenu;
    private RecyclerView rvMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_list);

        drawerLayout = findViewById(R.id.drawer_layout);
        ivMenu = findViewById(R.id.ivMenu);
        rvMenu = findViewById(R.id.rvMenu);

        rvMenu.setLayoutManager(new LinearLayoutManager(this));
        rvMenu.setAdapter(new MainDrawerAdapter(this, com.example.kpmsadmin.TrackingNumber.arrayList, com.example.kpmsadmin.TrackingNumber.image));

        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        recyclerView = findViewById(R.id.recyclerList);
        searchView = findViewById(R.id.searchView);
        
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        
        LoadHistory();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    

}

    private void filter(String text) {
        filteredList.clear();
        for (History item : historyList) {
            // Check if the query text is contained in any of the item fields (case-insensitive)
            if (item.getTrackingNumber().toLowerCase().contains(text.toLowerCase()) ||
                    item.getCustName().toLowerCase().contains(text.toLowerCase()) ||
                    item.getCustPhoneNum().toLowerCase().contains(text.toLowerCase()) ||
                    item.getCourierName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        // Update the adapter's dataset
        if (text.isEmpty()) {
            historyAdapter.updateList(historyList);
        } else {
            historyAdapter.updateList(filteredList);
        }
    }


    private void LoadHistory() {
        JsonArrayRequest request = new JsonArrayRequest(Urls.HISTORY_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray array) {
                for(int i=0; i< array.length(); i++){
                    try {
                        JSONObject object = array.getJSONObject(i);
                        String trackingNumber = object.getString("trackingNumber").trim();
                        String custName = object.getString("custName").trim();
                        String custPhoneNum = object.getString("custPhoneNum").trim();
                        String courierName = object.getString("courierName").trim();

                        History history = new History();
                        history.setTrackingNumber(trackingNumber);
                        history.setCustName(custName);
                        history.setCustPhoneNum(custPhoneNum);
                        history.setCourierName(courierName);
                        historyList.add(history);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                historyAdapter = new HistoryAdapter(HistoryList.this,historyList);
                recyclerView.setAdapter(historyAdapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HistoryList.this, error.toString(), Toast.LENGTH_SHORT).show();

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(HistoryList.this);
        requestQueue.add(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        com.example.kpmsadmin.TrackingNumber.closeDrawer(drawerLayout);
    }

}