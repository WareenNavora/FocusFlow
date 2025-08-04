package com.android.focusflow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MoreFrag extends Fragment {

    ListView lvTutorialChoices,lvDataChoices;
    DailyDatabase dailyDatabase;
    ScheduleDatabase scheduleDatabase;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        lvTutorialChoices = view.findViewById(R.id.lvTutorialChoices);
        lvDataChoices = view.findViewById(R.id.lvDataChoices);

        String[] tutorialChoices = {"Tutorial"};
        String[] dataChoices = {"Delete all data from scheduled task", "Delete all data from daily task"};

        CustomFontStyleLV customFontStyleLV = new CustomFontStyleLV(getContext(), tutorialChoices);
        CustomFontStyleLV customFontStyleLV1 = new CustomFontStyleLV(getContext(), dataChoices);

        lvTutorialChoices.setAdapter(customFontStyleLV);
        lvDataChoices.setAdapter(customFontStyleLV1);

        initializeScheduleDatabase();
        initializeDailyDatabase();

        lvTutorialChoices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    Toast.makeText(getContext(), "You've selected tutorial section!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Try again later!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvDataChoices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0: { // Delete all from scheduled task
                        AlertDialog.Builder dialog1 = new AlertDialog.Builder(requireContext());
                        dialog1.setTitle("Delete all data from scheduled task");
                        dialog1.setMessage("Are you sure you want to delete all data from scheduled task?");
                        dialog1.setPositiveButton("Confirm", (dialogInterface, i1) -> {
                            executorService.submit(() -> {
                                scheduleDatabase.getScheduleDAO().deleteAllSchedule();
                                handler.post(() ->
                                        Toast.makeText(requireContext(), "All data from scheduled task has been deleted!", Toast.LENGTH_SHORT).show()
                                );
                            });
                        });
                        dialog1.setNegativeButton("Cancel", (dialogInterface, i1) ->
                                Toast.makeText(requireContext(), "Try again later!", Toast.LENGTH_SHORT).show()
                        );
                        dialog1.show();
                        break;
                    }

                    case 1: { // Delete all from daily task
                        AlertDialog.Builder dialog2 = new AlertDialog.Builder(requireContext());
                        dialog2.setTitle("Delete all data from daily task");
                        dialog2.setMessage("Are you sure you want to delete all data from daily task?");
                        dialog2.setPositiveButton("Confirm", (dialogInterface, i2) -> {
                            executorService.submit(() -> {
                                dailyDatabase.getDailyDao().deleteAllDaily();
                                handler.post(() ->
                                        Toast.makeText(requireContext(), "All data from daily task has been deleted!", Toast.LENGTH_SHORT).show()
                                );
                            });
                        });
                        dialog2.setNegativeButton("Cancel", null); // no message needed
                        dialog2.show();
                        break;
                    }

                    default:
                        Toast.makeText(requireContext(), "Try again later!", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });

        return view;
    }

    private void initializeScheduleDatabase(){
        RoomDatabase.Callback scheduleCallback = new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                super.onDestructiveMigration(db);
            }
        };
        scheduleDatabase = Room.databaseBuilder(getContext(),
                ScheduleDatabase.class, "ScheduleDatabase").addCallback(scheduleCallback).build();
    }

    private void initializeDailyDatabase(){
        RoomDatabase.Callback dailyCallback = new RoomDatabase.Callback() {
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
                DailyDatabase.class, "DailyDatabase").addCallback(dailyCallback).build();
    }
}