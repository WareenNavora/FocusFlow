package com.android.focusflow;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsFrag extends Fragment {

    DailyDatabase dailyDatabase;
    ScheduleDatabase scheduleDatabase;
    Long schedTotal, dailyTotal, schedDo, schedDoing, schedDone;
    ExecutorService executorService;
    Handler handler;
    TextView txtTotalTask, txtDailyTask, txtScheduleTask, txtDo, txtDoing, txtDone;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        txtDo = view.findViewById(R.id.txtDoTotalCount);
        txtDoing = view.findViewById(R.id.txtDoingTotalCount);
        txtDone = view.findViewById(R.id.txtDoneTotalCount);
        txtScheduleTask = view.findViewById(R.id.txtTotalScheduleCount);
        txtDailyTask = view.findViewById(R.id.txtTotalDailyCount);
        txtTotalTask = view.findViewById(R.id.txtTotalTaskCount);

        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        initializeDatabase();

        executorService.execute(() -> {
            schedTotal = scheduleDatabase.getScheduleDAO().getScheduleSize();
            dailyTotal = dailyDatabase.getDailyDao().getDailySize();
            schedDo = scheduleDatabase.getScheduleDAO().getScheduleDoSize();
            schedDoing = scheduleDatabase.getScheduleDAO().getScheduleDoingSize();
            schedDone = scheduleDatabase.getScheduleDAO().getScheduleDoneSize();

            handler.post(() -> {
                txtDo.setText(String.valueOf(Math.toIntExact(schedDo)));
                txtDoing.setText(String.valueOf(Math.toIntExact(schedDoing)));
                txtDone.setText(String.valueOf(Math.toIntExact(schedDone)));
                txtScheduleTask.setText(String.valueOf(Math.toIntExact(schedTotal)));
                txtDailyTask.setText(String.valueOf(Math.toIntExact(dailyTotal)));
                txtTotalTask.setText(String.valueOf(Math.toIntExact(dailyTotal + schedTotal)));

            });
        });

        return view;
    }

    public void initializeDatabase(){
        RoomDatabase.Callback CallBack = new RoomDatabase.Callback() {
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
                .addCallback(CallBack)
                .fallbackToDestructiveMigration()
                .build();

        scheduleDatabase = Room.databaseBuilder(getContext(),
                ScheduleDatabase.class, "ScheduleDatabase")
                .addCallback(CallBack)
                .fallbackToDestructiveMigration()
                .build();
    }
}