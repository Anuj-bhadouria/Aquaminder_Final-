package com.iar.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Dashboard extends AppCompatActivity {

    private List<Float> flowRates = new ArrayList<>();
    private List<Float> totalVolumes = new ArrayList<>();
    private List<Long> timestamps = new ArrayList<>();

    private DatabaseReference mDatabase, mHistoryDb, mRequestDb;
    private FirebaseAuth mAuth;
    private TextView tvCurrentFlow, tvDailyUsage, tvGoalProgress, tvActiveSensors;
    private MaterialCardView cardWarning, cardPlumberRequest;
    private TextView tvSystemHealthy, tvSystemMessage, tvPlumberName, tvRequestStatus;
    private ImageView ivWarning, ivProfile;
    private Button btnCancelRequest;
    private String activeRequestId;

    private LineChart lineChart;
    private BarChart barChart;
    private PieChart pieChart;

    private MaterialButtonToggleGroup toggleWaterFlow;



    private static final String CHANNEL_ID = "leak_warning_channel";
    private static final int NOTIFICATION_ID = 101;


    private Handler historyHandler = new Handler(Looper.getMainLooper());
    private Runnable historyRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        tvCurrentFlow = findViewById(R.id.tv_current_flow);
        tvDailyUsage = findViewById(R.id.tv_daily_usage);
        tvGoalProgress = findViewById(R.id.tv_goal_progress);
        tvActiveSensors = findViewById(R.id.tv_active_sensors);
        cardWarning = findViewById(R.id.card_warning);
        tvSystemHealthy = findViewById(R.id.tv_system_healthy);
        tvSystemMessage = findViewById(R.id.tv_system_message);
        ivWarning = findViewById(R.id.iv_warning);
        ivProfile = findViewById(R.id.iv_profile);
        cardPlumberRequest = findViewById(R.id.card_plumber_request);
        tvPlumberName = findViewById(R.id.tv_plumber_name);
        tvRequestStatus = findViewById(R.id.tv_request_status);
        btnCancelRequest = findViewById(R.id.btn_cancel_request);
        toggleWaterFlow = findViewById(R.id.toggle_water_flow);

        lineChart = findViewById(R.id.graph_water_flow);
        barChart = findViewById(R.id.graph_daily_consumption);
        pieChart = findViewById(R.id.graph_usage_breakdown);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    if (!"user".equals(role)) {
                        Toast.makeText(Dashboard.this, "Unauthorized access", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Dashboard.this, LoginActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference("sensor");
        mHistoryDb = FirebaseDatabase.getInstance().getReference("sensor_history");
        mRequestDb = FirebaseDatabase.getInstance().getReference("requests");

        // Initialize
        createNotificationChannel();
        requestNotificationPermission();
        setupCharts();
        setupRecentAlerts();
        setupBottomNavigation();
        setupProfileButton();
        fetchPlumberRequest();

        btnCancelRequest.setOnClickListener(v -> {
            if (activeRequestId != null) {
                mRequestDb.child(activeRequestId).child("status").setValue("CANCELLED");
            }
        });


        setupFirebaseListener();
        setupGraphToggles();


        startHourlyHistorySave();
    }

    private void setupProfileButton() {
        ivProfile.setOnClickListener(v -> startActivity(new Intent(Dashboard.this, ProfileActivity.class)));
    }

    /*** Notification setup ***/
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Leak Warnings";
            String description = "Alerts for potential water leaks";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void sendLeakWarningNotification(String status) {
        Intent intent = new Intent(this, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("LEAK DETECTED!")
                .setContentText("Status: " + status.toUpperCase())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Enable notifications to receive alerts", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /*** Hourly History Save ***/
    private void startHourlyHistorySave() {
        historyRunnable = new Runnable() {
            @Override
            public void run() {
                saveCurrentSensorHistory();
                historyHandler.postDelayed(this, 3600000);
            }
        };
        historyHandler.post(historyRunnable);
    }

    private void saveCurrentSensorHistory() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double flowRate = snapshot.child("flowRate").getValue(Double.class);
                    Double totalVolume = snapshot.child("totalVolume").getValue(Double.class);
                    String status = snapshot.child("status").getValue(String.class);

                    if (flowRate == null) flowRate = 0.0;
                    if (totalVolume == null) totalVolume = 0.0;
                    if (status == null) status = "SAFE";

                    long currenTime= System.currentTimeMillis();
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
                    String formattedDate= sdf.format(new Date(currenTime));


                    mHistoryDb.push().setValue(new SensorHistoryEntry(
                            flowRate,
                            totalVolume,
                            status,
                            currenTime,
                            formattedDate
                    ));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public static class SensorHistoryEntry {

        public double flowRate;
        public double totalVolume;
        public String status;
        public long timestamp;
        public String readableTime;

        public SensorHistoryEntry() {}


        public SensorHistoryEntry(double flowRate, double totalVolume, String status, long timestamp, String readableTime) {
            this.flowRate = flowRate;
            this.totalVolume = totalVolume;
            this.status = status;
            this.timestamp = timestamp;
            this.readableTime = readableTime;
        }
    }


    private void setupGraphToggles() {
        toggleWaterFlow.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_6h) {
                    fetchHistory(6);
                } else if (checkedId == R.id.btn_24h) {
                    fetchHistory(24);
                } else if (checkedId == R.id.btn_7d) {
                    fetchHistory(24 * 7);
                }
            }
        });
    }

    private void fetchHistory(int hours) {
        long cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(hours);
        mHistoryDb.orderByChild("timestamp").startAt(cutoff).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Map<String, Double> dailyMinMap = new LinkedHashMap<>();
                Map<String, Double> dailyMaxMap = new LinkedHashMap<>();
                ArrayList<String> dayLabels = new ArrayList<>();

                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

                for (DataSnapshot child : snapshot.getChildren()) {
                    Double flowRate = child.child("flowRate").getValue(Double.class);
                    Long ts = child.child("timestamp").getValue(Long.class);
                    Double volume = child.child("totalVolume").getValue(Double.class);

                    if (ts != null && volume != null) {

                        String dayKey = dayFormat.format(new Date(ts));


                        if (!dailyMinMap.containsKey(dayKey)) {
                            dailyMinMap.put(dayKey, volume);
                            dayLabels.add(dayKey);
                        }

                        dailyMaxMap.put(dayKey, volume);
                    }


                    if (flowRate != null) flowRates.add(flowRate.floatValue());
                    if (ts != null) timestamps.add(ts);
                }


                ArrayList<BarEntry> dailyEntries = new ArrayList<>();
                ArrayList<String> finalLabels = new ArrayList<>();

                int index = 0;

                for (String day : dayLabels) {
                    double min = dailyMinMap.get(day);
                    double max = dailyMaxMap.get(day);
                    double consumed = max - min;


                    if(consumed < 0) consumed = 0;

                    dailyEntries.add(new BarEntry(index, (float) consumed));
                    finalLabels.add(day);
                    index++;
                }


                updateWaterFlowChart();
                updateDailyConsumptionChart(dailyEntries, finalLabels);
                updateUsageBreakdownChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    /*** Firebase Listener for History + Current ***/
    private void setupFirebaseListener() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot currentSnapshot) {
                Double flowRateCurrent = currentSnapshot.child("flowRate").getValue(Double.class);
                Double totalVolumeCurrent = currentSnapshot.child("totalVolume").getValue(Double.class);
                String statusCurrent = currentSnapshot.child("status").getValue(String.class);


                tvCurrentFlow.setText(flowRateCurrent != null ? flowRateCurrent + " L/Min" : "0 L/Min");
                tvDailyUsage.setText(totalVolumeCurrent != null ? totalVolumeCurrent + " L" : "0 L");
                int progress = totalVolumeCurrent != null ? (int)((totalVolumeCurrent / 10.0)*100) : 0;
                tvGoalProgress.setText(progress + " %");
                tvActiveSensors.setText("Active sensors: 3");


                updateSystemStatus(statusCurrent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        fetchHistory(24 * 7);
    }

    /*** Charts Setup ***/
    private void setupCharts() {
        // WaterFlow
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getXAxis().setEnabled(true);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                long millis = (long) value;
                return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
            }
        });
        lineChart.animateX(1000);

        // DailyConsumption
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.animateY(1000);

        // Usage Breakdown Pie
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setDrawEntryLabels(false);
        pieChart.animateY(1400);

    }

    /*** Update Charts ***/
    private void updateWaterFlowChart() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < flowRates.size(); i++) {
            float x = (timestamps.size() > i) ? timestamps.get(i) : i;
            entries.add(new Entry(x, flowRates.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Flow (L/min)");
        dataSet.setColor(ContextCompat.getColor(this, R.color.accent_blue));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(this, R.color.accent_blue));
        dataSet.setDrawCircles(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineData.setDrawValues(false);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void updateDailyConsumptionChart(ArrayList<BarEntry> entries, ArrayList<String> labels) {
        if (barChart == null) return;

        BarDataSet set;
        if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
            set = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            set.setValues(entries);
            barChart.getData().notifyDataChanged();
            barChart.notifyDataSetChanged();
        } else {
            set = new BarDataSet(entries, "Daily Consumption");
            set.setColor(getResources().getColor(R.color.purple_500));
            set.setDrawValues(false);

            BarData data = new BarData(set);
            data.setBarWidth(0.5f);
            barChart.setData(data);
        }


        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        barChart.setFitBars(true);

        barChart.invalidate();
    }


    private void updateUsageBreakdownChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();


        float overheadUsage = 0f;
        if (totalVolumes != null && !totalVolumes.isEmpty()) {
            float lastVolume = totalVolumes.get(totalVolumes.size() - 1);
            float firstVolume = totalVolumes.get(0);
            overheadUsage = lastVolume - firstVolume;
            if (overheadUsage < 0) overheadUsage = 0;
        }

        if (overheadUsage == 0) overheadUsage = 5f;
        long threeHourBlock = System.currentTimeMillis() / (1000 * 60 * 60 * 3);
        java.util.Random random = new java.util.Random(threeHourBlock);
        float undergroundVal = 20f + random.nextInt(30);
        float mainLineVal = 10f + random.nextInt(20);




        entries.add(new PieEntry(overheadUsage, "Overhead"));
        entries.add(new PieEntry(undergroundVal, "Underground"));
        entries.add(new PieEntry(mainLineVal, "Main Line"));


        PieDataSet dataSet = new PieDataSet(entries, "Usage Breakdown");

        dataSet.setColors(new int[]{
                ContextCompat.getColor(this, R.color.blue_primary),
                ContextCompat.getColor(this, R.color.teal_200),
                Color.LTGRAY
        });

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);




        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setValueLineColor(Color.TRANSPARENT);
        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.BLACK);
        pieChart.setData(pieData);
        pieChart.setExtraOffsets(5f, 5f, 5f, 5f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.invalidate();
    }




    /*** Leak Detection ***/
    private void updateSystemStatus(String status) {
        if (status == null || "SAFE".equalsIgnoreCase(status)) {
            cardWarning.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_blue));
            tvSystemHealthy.setText("NO LEAK DETECTED");
            tvSystemHealthy.setTextColor(ContextCompat.getColor(this, R.color.accent_blue));
            tvSystemMessage.setText("System is operating normally");
            tvSystemMessage.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            ivWarning.setColorFilter(ContextCompat.getColor(this, R.color.accent_blue));
        } else {
            cardWarning.setCardBackgroundColor(ContextCompat.getColor(this, R.color.red_light));
            tvSystemHealthy.setText("LEAK DETECTED!");
            tvSystemHealthy.setTextColor(ContextCompat.getColor(this, R.color.red_dark));
            tvSystemMessage.setText("Status: " + status.toUpperCase());
            tvSystemMessage.setTextColor(ContextCompat.getColor(this, R.color.red_dark));
            ivWarning.setColorFilter(ContextCompat.getColor(this, R.color.red_dark));
            sendLeakWarningNotification(status);
        }
    }

    /*** Alerts and Bottom Nav ***/
    private void setupRecentAlerts() {
        RecyclerView rvAlerts = findViewById(R.id.rv_alerts);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        List<Alert> alerts = new ArrayList<>();
        alerts.add(new Alert(R.drawable.ic_minor_drip, "Minor Drip - Overhead Tank", "Resolved • 2h ago"));
        alerts.add(new Alert(R.drawable.ic_flow_spike, "Underground Tank", "Investigate • Yesterday"));
        alerts.add(new Alert(R.drawable.ic_valve_auto_shutoff, "Valve Auto-Shutoff - Main Line", "Preventive • 2 days ago"));
        rvAlerts.setAdapter(new AlertAdapter(alerts));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
             int itemId = item.getItemId();
            if (itemId == R.id.nav_book) {
                startActivity(new Intent(Dashboard.this, PlumbersListActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(Dashboard.this, ProfileActivity.class));
                return true;
            }
            return true;
        });
    }

    private void fetchPlumberRequest() {
        String userId = mAuth.getCurrentUser().getUid();
        mRequestDb.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Request request = snapshot.getValue(Request.class);
                    if (request != null && !request.getStatus().equals("COMPLETED") && !request.getStatus().equals("REJECTED") && !request.getStatus().equals("CANCELLED")) {
                        activeRequestId = snapshot.getKey();
                        cardPlumberRequest.setVisibility(View.VISIBLE);
                        tvRequestStatus.setText("Status: " + request.getStatus());

                        DatabaseReference plumberRef = FirebaseDatabase.getInstance().getReference("users").child(request.getPlumberId());
                        plumberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot plumberSnapshot) {
                                Plumber plumber = plumberSnapshot.getValue(Plumber.class);
                                if (plumber != null) {
                                    tvPlumberName.setText("Plumber: " + plumber.getName());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        return;
                    }
                }
                cardPlumberRequest.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}