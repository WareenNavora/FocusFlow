package com.android.focusflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
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
import android.os.Message;
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

        ItemTouchHelper itemTouchHelper = getItemTouchHelper(getContext());
        itemTouchHelper.attachToRecyclerView(taskView);

        executor.submit(() -> {
            getAllScheduledTaskAndStoreInList(selectedProgress);

            new Handler(Looper.getMainLooper()).post(() -> {
                setupTaskCardRecyclerView(view);
            });
        });

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
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float cornerRadius = 40f;
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setAntiAlias(true);

                    float top = itemView.getTop();
                    float bottom = itemView.getBottom();
                    float left, right;

                    RectF rect;
                    Path path = new Path();

                    if (dX > 0) {
                        // Swiping to the right
                        left = itemView.getLeft();
                        right = itemView.getLeft() + dX;
                        rect = new RectF(left, top, right, bottom);

                        path.addRoundRect(
                                rect,
                                new float[]{
                                        cornerRadius, cornerRadius, // top-left
                                        0f, 0f,                     // top-right
                                        0f, 0f,                     // bottom-right
                                        cornerRadius, cornerRadius  // bottom-left
                                },
                                Path.Direction.CW
                        );

                    } else {
                        // Swiping to the left
                        left = itemView.getRight() + dX;
                        right = itemView.getRight();
                        rect = new RectF(left, top, right, bottom);

                        path.addRoundRect(
                                rect,
                                new float[]{
                                        0f, 0f,                     // top-left
                                        cornerRadius, cornerRadius, // top-right
                                        cornerRadius, cornerRadius, // bottom-right
                                        0f, 0f                      // bottom-left
                                },
                                Path.Direction.CW
                        );
                    }

                    canvas.save();
                    canvas.clipPath(path);
                    canvas.drawRect(rect, paint);
                    canvas.restore();

                    // Draw the "Swiping" text
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(40);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    String swipeText = "Swiping";

                    float textWidth = paint.measureText(swipeText);
                    float textX = (dX > 0)
                            ? itemView.getLeft() + 40
                            : itemView.getRight() - textWidth - 40;
                    float textY = top + (itemView.getHeight() / 2f) + 15;

                    canvas.drawText(swipeText, textX, textY, paint);
                }

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }


            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                String currentProgress = scheduleTaskProgress.get(position);

                if (currentProgress.equals("Do")) {
                    return ItemTouchHelper.RIGHT; // Allow only right swipe
                } else if (currentProgress.equals("Doing")) {
                    return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT; // Allow both
                } else if (currentProgress.equals("Done")) {
                    return ItemTouchHelper.LEFT; // Allow only left swipe
                }

                return super.getSwipeDirs(recyclerView, viewHolder);
            }


            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // Get actual progress for the swiped item
                String currentProgress = scheduleTaskProgress.get(position);

                String schedId = scheduleId.get(position);
                String schedName = scheduleName.get(position);
                String schedDes = scheduleDes.get(position);
                String schedStart = scheduleStartDate.get(position);
                String schedEnd = scheduleEndDate.get(position);

                String newProgress;

                if (currentProgress.equals("Do") && direction == ItemTouchHelper.RIGHT) {
                    newProgress = "Doing";
                } else if (currentProgress.equals("Doing")) {
                    if (direction == ItemTouchHelper.LEFT) {
                        newProgress = "Do";
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        newProgress = "Done";
                    } else {
                        newProgress = null;
                    }
                } else if (currentProgress.equals("Done") && direction == ItemTouchHelper.LEFT) {
                    newProgress = "Doing";
                } else {
                    newProgress = null;
                }

                if (newProgress == null) {
                    // Invalid swipe, reset the item visually
                    adapter.notifyItemChanged(position);
                    return;
                }

                ScheduleEntity scheduleEntity = new ScheduleEntity(schedName, schedDes, schedStart, schedEnd, newProgress);
                scheduleEntity.setTaskId(Integer.parseInt(schedId));

                executor.execute(() -> {
                    scheduleDatabase.getScheduleDAO().editSchedule(scheduleEntity);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        // Remove item from in-memory lists
                        scheduleId.remove(position);
                        scheduleName.remove(position);
                        scheduleDes.remove(position);
                        scheduleStartDate.remove(position);
                        scheduleEndDate.remove(position);
                        scheduleTaskProgress.remove(position);

                        // Notify adapter so the item disappears
                        adapter.notifyItemRemoved(position);

                    });
                });
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