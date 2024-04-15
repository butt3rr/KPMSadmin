package com.example.kpmsadmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainDrawerAdapter extends RecyclerView.Adapter<MainDrawerAdapter.ViewHolder> {

    Activity activity;
    ArrayList<String> arrayList;
    ArrayList<Integer> image;

    //constructor
    public MainDrawerAdapter(Activity activity, ArrayList<String> arrayList, ArrayList<Integer> image) {
        this.activity = activity;
        this.arrayList = arrayList;
        this.image = image;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drawer_main,parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.ivMenu.setImageResource(image.get(position));
        holder.tvMenu.setText(arrayList.get(position));
        holder.tvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();

                switch (pos){
                    case 0:
                        activity.startActivity(new Intent(activity, TrackingNumber.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case 1:
                        activity.startActivity(new Intent(activity, HistoryList.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case 2:
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle("Log Out");
                        builder.setMessage("Are you sure you want to log out?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Add code to handle logout process here

                                // Start MainActivity after logout
                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);
                                activity.finish(); // Finish the current activity
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                        break;

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMenu;
        private ImageView ivMenu;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tvMenu = itemView.findViewById(R.id.tvMenu);
            ivMenu = itemView.findViewById(R.id.ivMenu);

        }
    }
}
