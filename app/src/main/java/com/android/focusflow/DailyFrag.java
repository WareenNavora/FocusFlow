package com.android.focusflow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyFrag extends Fragment {

    DailyAdapterDisplay dailyAdapterDisplay;
    ArrayList<String> dailyId, dailyName, dailyTotalIteration, dailyRemainingIteration, dailyLatestIteratedDate, dailyDays, dailyHour;
    DailyDatabase dailyDatabase;
    RecyclerView cardViewer;
    ExecutorService executorService;
    Handler handler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_task, container, false);

        // initialize everything
        dailyId = new ArrayList<>();
        dailyName = new ArrayList<>();
        dailyTotalIteration = new ArrayList<>();
        dailyRemainingIteration = new ArrayList<>();
        dailyLatestIteratedDate = new ArrayList<>();
        dailyDays = new ArrayList<>();
        dailyHour = new ArrayList<>();

        initializeDatabase();

        cardViewer = view.findViewById(R.id.rcv_dailyTask);

        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        dailyAdapterDisplay = new DailyAdapterDisplay(
                getContext(),
                dailyId,
                dailyName,
                dailyTotalIteration,
                dailyRemainingIteration,
                dailyLatestIteratedDate,
                dailyHour,
                dailyDays
        );

        cardViewer.setLayoutManager(new LinearLayoutManager(getContext()));
        cardViewer.setAdapter(dailyAdapterDisplay);

        executorService.submit(() -> {
            storeAllDataInDatabase(); // populates all lists

            handler.post(dailyAdapterDisplay::notifyDataSetChanged);
        });

        return view;
    }

    public void initializeDatabase(){
        RoomDatabase.Callback DailyCallBack = new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                super.onDestructiveMigration(db);
            }
        };

        dailyDatabase = Room.databaseBuilder(getContext(),
                DailyDatabase.class, "DailyDatabase")
                .addCallback(DailyCallBack)
                .fallbackToDestructiveMigration()
                .build();
    }

    public void storeAllDataInDatabase(){
        Long dailySize = dailyDatabase.getDailyDao().getDailySize();

        if(dailySize != 0) {
            // Clear and populate lists
            dailyId.clear();
            dailyName.clear();
            dailyTotalIteration.clear();
            dailyRemainingIteration.clear();
            dailyLatestIteratedDate.clear();
            dailyDays.clear();
            dailyHour.clear();

            for (DailyEntity dailyEntity1 : dailyDatabase.getDailyDao().getAllDaily()){
                dailyId.add(String.valueOf(dailyEntity1.getDailyId()));
                dailyName.add(dailyEntity1.getDailyName());
                dailyHour.add(dailyEntity1.getDailyHour());
                dailyTotalIteration.add(dailyEntity1.getDailyTotalIteration());
                dailyRemainingIteration.add(dailyEntity1.getDailyRemainingIteration());
                dailyLatestIteratedDate.add(dailyEntity1.getDailyLatestIteratedDate());
                dailyDays.add(dailyEntity1.getDailyDays());
            }

        } else {
            // Show toast on main thread
            if(getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "No data available", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

}