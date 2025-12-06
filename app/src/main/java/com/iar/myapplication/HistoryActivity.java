package com.iar.myapplication;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private LineChart historyFlowChart;
    private RecyclerView rvHiredPlumbers;
    private PlumberAdapter plumberAdapter;
    private List<Plumber> hiredPlumbers = new ArrayList<>();
    private List<Entry> flowHistory = new ArrayList<>();
    private DatabaseReference mHistoryDb, mRequestDb, mUserDb;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyFlowChart = findViewById(R.id.history_flow_chart);
        rvHiredPlumbers = findViewById(R.id.rv_hired_plumbers);
        rvHiredPlumbers.setLayoutManager(new LinearLayoutManager(this));

        plumberAdapter = new PlumberAdapter(hiredPlumbers, plumber -> {
            // Can add click listener to view plumber profile
        });
        rvHiredPlumbers.setAdapter(plumberAdapter);

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mHistoryDb = FirebaseDatabase.getInstance().getReference("sensor_history");
        mRequestDb = FirebaseDatabase.getInstance().getReference("requests");
        mUserDb = FirebaseDatabase.getInstance().getReference("users");

        setupChart();
        fetchFlowHistory();
        fetchHiredPlumbers(userId);
    }

    private void setupChart(){
        historyFlowChart.getDescription().setEnabled(false);
        historyFlowChart.getLegend().setEnabled(true);
        historyFlowChart.getAxisRight().setEnabled(false);
        historyFlowChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        historyFlowChart.getXAxis().setGranularity(1f);
        historyFlowChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                long millis = (long) value;
                return new SimpleDateFormat("MM/dd", Locale.getDefault()).format(new Date(millis));
            }
        });
    }

    private void fetchFlowHistory() {
        mHistoryDb.limitToLast(30).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                flowHistory.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    SensorHistoryEntry entry = snapshot.getValue(SensorHistoryEntry.class);
                    if (entry != null) {
                        flowHistory.add(new Entry(entry.timestamp, (float) entry.flowRate));
                    }
                }
                LineDataSet dataSet = new LineDataSet(flowHistory, "Flow Rate History");
                LineData lineData = new LineData(dataSet);
                historyFlowChart.setData(lineData);
                historyFlowChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HistoryActivity.this, "Failed to load flow history.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchHiredPlumbers(String userId) {
        mRequestDb.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hiredPlumbers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Request request = snapshot.getValue(Request.class);
                    if (request != null && request.getStatus().equals("COMPLETED")) {
                        mUserDb.child(request.getPlumberId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot plumberSnapshot) {
                                Plumber plumber = plumberSnapshot.getValue(Plumber.class);
                                if (plumber != null) {
                                    hiredPlumbers.add(plumber);
                                    plumberAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HistoryActivity.this, "Failed to load hired plumbers.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}