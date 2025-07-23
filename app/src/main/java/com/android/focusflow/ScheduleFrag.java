package com.android.focusflow;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleFrag extends Fragment {

    ArrayList<String> scheduleId, scheduleName, scheduleDes, scheduleStartDate, scheduleEndDate, scheduleTaskProgress;
    List<ScheduleEntity> scheduleEntities;
    ScheduleDatabase scheduleDatabase;
    ScheduleAdapterDisplay adapter;
    RecyclerView taskView;
    RadioGroup rgScheduleProgress;
    RadioButton rbProgress;
    String selectedProgress;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scheduled_task, container, false);
        ImageButton imgVHelp = view.findViewById(R.id.imgVScheduleHelp);

        rgScheduleProgress = view.findViewById(R.id.rgScheduleProgress);
        taskView = view.findViewById(R.id.rcvScheduleCardDisp);

        //arraylist initialization
        scheduleId = new ArrayList<>();
        scheduleName = new ArrayList<>();
        scheduleDes = new ArrayList<>();
        scheduleStartDate = new ArrayList<>();
        scheduleEndDate = new ArrayList<>();
        scheduleTaskProgress = new ArrayList<>();

        scheduleEntities = new ArrayList<>();

        //setting the default value of the selected progress to display the schedule or the task that needed to be completed
        selectedProgress = "Do";

        // schedule database initialization
        initializeDatabase();

        executor.submit(() -> {
            getAllScheduledTaskAndStoreInList(selectedProgress);

            new Handler(Looper.getMainLooper()).post(() -> {
                setupTaskCardRecyclerView(view);
            });
        });

        ItemTouchHelper itemTouchHelper = getItemTouchHelper(getContext());
        itemTouchHelper.attachToRecyclerView(taskView);

        rgScheduleProgress.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rbDo){
                    selectedProgress = "Do";

                    executor.submit(() -> {
                        getAllScheduledTaskAndStoreInList(selectedProgress);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            setupTaskCardRecyclerView(view);
                        });
                    });


                }
                else if (checkedId == R.id.rbDoing){
                    selectedProgress = "Doing";

                    executor.submit(() -> {
                        getAllScheduledTaskAndStoreInList(selectedProgress);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            setupTaskCardRecyclerView(view);
                        });
                    });


                }
                else if (checkedId == R.id.rbDone){
                    selectedProgress = "Done";

                    executor.submit(() -> {
                        getAllScheduledTaskAndStoreInList(selectedProgress);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            setupTaskCardRecyclerView(view);
                        });
                    });

                }
            }
        });

        imgVHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "scheduled help is clicked!", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @NonNull
    private ItemTouchHelper getItemTouchHelper(Context context) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                switch (selectedProgress) {
                    case "Do":
                        if (direction == ItemTouchHelper.LEFT) {
                            //disable this move
                        } else if (direction == ItemTouchHelper.RIGHT) {
                            Toast.makeText(context, "This task is now on Doing", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "Doing":
                        if (direction == ItemTouchHelper.LEFT) {
                            Toast.makeText(context, "This task is now on Do", Toast.LENGTH_SHORT).show();
                        } else if (direction == ItemTouchHelper.RIGHT) {
                            Toast.makeText(context, "This task is now on Done", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "Done":
                        if (direction == ItemTouchHelper.LEFT) {
                            Toast.makeText(context, "This task is now on Doing", Toast.LENGTH_SHORT).show();
                        } else if (direction == ItemTouchHelper.RIGHT) {
                            //disable this move
                        }
                        break;
                }

            }
        };

        return new ItemTouchHelper(simpleCallback);
    }


    private void initializeDatabase(){
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
                ScheduleDatabase.class, "ScheduleDatabase")
                .addCallback(scheduleCallback)
                .fallbackToDestructiveMigration()// need to remove when the development phase is done
                .build();
    }

    private void setupTaskCardRecyclerView(@NonNull View view) {
        taskView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScheduleAdapterDisplay(getContext(), scheduleId, scheduleName, scheduleDes, scheduleStartDate, scheduleEndDate, scheduleTaskProgress);
        taskView.setAdapter(adapter);
    }

    private void getAllScheduledTaskAndStoreInList(@NonNull String activeProgress){

        switch (activeProgress) {
            case "Do": {
                Long scheduleSize = scheduleDatabase.getScheduleDAO().getScheduleSize();

                if (scheduleSize == null) {
                    Toast.makeText(getContext(), "No task!", Toast.LENGTH_SHORT).show();
                } else {
                    scheduleId.clear();
                    scheduleName.clear();
                    scheduleDes.clear();
                    scheduleStartDate.clear();
                    scheduleEndDate.clear();
                    scheduleTaskProgress.clear();

                    for (ScheduleEntity scheduleEntity : scheduleDatabase.getScheduleDAO().getSchedulesByProgress("Do")) {
                        scheduleId.add(String.valueOf(scheduleEntity.taskId));
                        scheduleName.add(scheduleEntity.taskName);
                        scheduleDes.add(scheduleEntity.taskDescription);
                        scheduleStartDate.add(scheduleEntity.taskStartDate);
                        scheduleEndDate.add(scheduleEntity.taskEndDate);
                        scheduleTaskProgress.add(scheduleEntity.taskProgress);
                    }
                }
                break;
            }
            case "Doing": {
                Long scheduleSize = scheduleDatabase.getScheduleDAO().getScheduleSize();

                if (scheduleSize == null) {
                    Toast.makeText(getContext(), "No task!", Toast.LENGTH_SHORT).show();
                } else {
                    scheduleId.clear();
                    scheduleName.clear();
                    scheduleDes.clear();
                    scheduleStartDate.clear();
                    scheduleEndDate.clear();
                    scheduleTaskProgress.clear();

                    for (ScheduleEntity scheduleEntity : scheduleDatabase.getScheduleDAO().getSchedulesByProgress("Doing")) {
                        scheduleId.add(String.valueOf(scheduleEntity.taskId));
                        scheduleName.add(scheduleEntity.taskName);
                        scheduleDes.add(scheduleEntity.taskDescription);
                        scheduleStartDate.add(scheduleEntity.taskStartDate);
                        scheduleEndDate.add(scheduleEntity.taskEndDate);
                        scheduleTaskProgress.add(scheduleEntity.taskProgress);
                    }
                }
                break;
            }
            case "Done": {
                Long scheduleSize = scheduleDatabase.getScheduleDAO().getScheduleSize();

                if (scheduleSize == null) {
                    Toast.makeText(getContext(), "No task!", Toast.LENGTH_SHORT).show();
                } else {
                    scheduleId.clear();
                    scheduleName.clear();
                    scheduleDes.clear();
                    scheduleStartDate.clear();
                    scheduleEndDate.clear();
                    scheduleTaskProgress.clear();

                    for (ScheduleEntity scheduleEntity : scheduleDatabase.getScheduleDAO().getSchedulesByProgress("Done")) {
                        scheduleId.add(String.valueOf(scheduleEntity.taskId));
                        scheduleName.add(scheduleEntity.taskName);
                        scheduleDes.add(scheduleEntity.taskDescription);
                        scheduleStartDate.add(scheduleEntity.taskStartDate);
                        scheduleEndDate.add(scheduleEntity.taskEndDate);
                        scheduleTaskProgress.add(scheduleEntity.taskProgress);
                    }
                }
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + activeProgress);
        }
    }
}