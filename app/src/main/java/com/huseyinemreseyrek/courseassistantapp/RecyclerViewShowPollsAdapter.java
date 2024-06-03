package com.huseyinemreseyrek.courseassistantapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

public class RecyclerViewShowPollsAdapter extends RecyclerView.Adapter<RecyclerViewShowPollsAdapter.MyViewHolder> {

    Context context;
    ArrayList<Poll> polls;
    public RecyclerViewShowPollsAdapter(Context context, ArrayList<Poll> polls)
    {
        this.context = context;
        this.polls = polls;
    }

    @NonNull
    @Override
    public RecyclerViewShowPollsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_recycler_show_polls, parent, false);
        return new RecyclerViewShowPollsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewShowPollsAdapter.MyViewHolder holder, int position) {
        holder.name.setText(polls.get(holder.getAdapterPosition()).getName());
        holder.courseID.setText(polls.get(holder.getAdapterPosition()).getCourseID());
        holder.status.setText(polls.get(holder.getAdapterPosition()).getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SelectedPoll.class);
                intent.putExtra("pollName", polls.get(holder.getAdapterPosition()).getName());
                intent.putExtra("courseID", polls.get(holder.getAdapterPosition()).getCourseID());
                intent.putExtra("status", polls.get(holder.getAdapterPosition()).getStatus());

                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return polls.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        TextView courseID;
        TextView status;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.pollName);
            courseID = itemView.findViewById(R.id.courseID);
            status = itemView.findViewById(R.id.status);
        }
    }
}
