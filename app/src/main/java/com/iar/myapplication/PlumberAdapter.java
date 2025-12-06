package com.iar.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlumberAdapter extends RecyclerView.Adapter<PlumberAdapter.PlumberViewHolder> {

    private List<Plumber> plumberList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Plumber plumber);
    }

    public PlumberAdapter(List<Plumber> plumberList, OnItemClickListener listener) {
        this.plumberList = plumberList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plumber, parent, false);
        return new PlumberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlumberViewHolder holder, int position) {
        Plumber plumber = plumberList.get(position);
        holder.bind(plumber, listener);
    }

    @Override
    public int getItemCount() {
        return plumberList.size();
    }

    public static class PlumberViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlumberImage;
        TextView tvName, tvDetails, tvRate;

        public PlumberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlumberImage = itemView.findViewById(R.id.iv_plumber_image);
            tvName = itemView.findViewById(R.id.tv_plumber_name);
            tvDetails = itemView.findViewById(R.id.tv_plumber_details);
            tvRate = itemView.findViewById(R.id.tv_plumber_rate);
        }

        public void bind(final Plumber plumber, final OnItemClickListener listener) {
            tvName.setText(plumber.getName());

            String details = String.format("%.1f • %d jobs", plumber.getRating(), plumber.getNumRatings());
            tvDetails.setText(details);

            if (plumber.getHourlyRate() != null && !plumber.getHourlyRate().isEmpty()) {
                tvRate.setText(String.format("$%s/hr", plumber.getHourlyRate()));
                tvRate.setVisibility(View.VISIBLE);
            } else {
                tvRate.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(plumber));
        }
    }
}