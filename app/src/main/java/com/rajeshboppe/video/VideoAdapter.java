package com.rajeshboppe.video;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyHolder> {
    private List<Video> videoList;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.videoview, parent, false);
        return new MyHolder(view);
    }

    public void setData(List<Video> videos) {
        videoList = videos;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.textView.setText(videoList.get(position).getTitle());
        if (videoList.get(position).isCurrent()) {
            holder.textView.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            holder.textView.setTypeface(Typeface.DEFAULT);
        }
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }

    }
}



