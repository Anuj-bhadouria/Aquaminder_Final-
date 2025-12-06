package com.iar.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<Request> requestList;
    private OnItemClickListener listener;
    private OnRequestActionListener actionListener;

    public interface OnItemClickListener {
        void onItemClick(Request request);
    }

    public interface OnRequestActionListener {
        void onAccept(Request request);
        void onReject(Request request);
    }

    public RequestAdapter(List<Request> requestList, OnItemClickListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    public void setOnRequestActionListener(OnRequestActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requestList.get(position);
        holder.bind(request, listener, actionListener);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress, tvStatus, tvDesc;
        LinearLayout llPlumberActions;
        Button btnAccept, btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.tv_request_address);
            tvStatus = itemView.findViewById(R.id.tv_request_status);
            tvDesc = itemView.findViewById(R.id.tv_request_desc);
            llPlumberActions = itemView.findViewById(R.id.ll_plumber_actions);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }

        public void bind(final Request request, final OnItemClickListener listener, final OnRequestActionListener actionListener) {
            tvAddress.setText(request.getAddress());
            tvStatus.setText("Status: " + request.getStatus());
            tvDesc.setText(request.getIssueDescription());

            itemView.setOnClickListener(v -> listener.onItemClick(request));

            if ("PENDING".equals(request.getStatus())) {
                llPlumberActions.setVisibility(View.VISIBLE);
                btnAccept.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onAccept(request);
                    }
                });
                btnReject.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onReject(request);
                    }
                });
            } else {
                llPlumberActions.setVisibility(View.GONE);
            }
        }
    }
}