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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyFrag extends Fragment {

    DailyAdapterDisplay dailyAdapterDisplay;
    ArrayList<String> dailyId, dailyName, dailyTotalIteration, dailyRemainingIteration, dailyLatestIteratedDate, dailyDays, dailyHour;
    DailyDatabase dailyDatabase;
    RecyclerView cardViewer;
    ExecutorService executorService;
    Handler handler;

    boolean isToday;

    DailyEntity dailyEntity;
    List<DailyEntity> dailyEntities;

    String currentDay, today, tomorrow;

    RadioButton rbToday, rbTom;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_task, container, false);

        // initialize lists
        dailyId = new ArrayList<>();
        dailyName = new ArrayList<>();
        dailyTotalIteration = new ArrayList<>();
        dailyRemainingIteration = new ArrayList<>();
        dailyLatestIteratedDate = new ArrayList<>();
        dailyDays = new ArrayList<>();
        dailyHour = new ArrayList<>();

        dailyEntities = new ArrayList<>();

        initializeDatabase();
        currentDay = DayUtil.getTodayAbbreviation(); //default

        cardViewer = view.findViewById(R.id.rcv_dailyTask);
        rbToday = view.findViewById(R.id.rbToday);
        rbTom = view.findViewById(R.id.rbTomorrow);

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
                dailyDays,
                currentDay
        );

        cardViewer.setLayoutManager(new LinearLayoutManager(getContext()));
        cardViewer.setAdapter(dailyAdapterDisplay);

        ItemTouchHelper itemTouchHelper = getItemTouchHelper(getContext());
        itemTouchHelper.attachToRecyclerView(cardViewer);

        // Initially show today's tasks
        today = DayUtil.getTodayAbbreviation();
        tomorrow = DayUtil.getTomorrowAbbreviation();

        loadTasksByDay(today); // default view

        rbToday.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                rbTom.setChecked(false); // ensure toggle behavior
                loadTasksByDay(today);
                currentDay = today;
            }
        });

        rbTom.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                rbToday.setChecked(false);
                loadTasksByDay(tomorrow);
                currentDay = tomorrow;
            }
        });

        return view;
    }

    private void loadTasksByDay(String day) {
        String currentDate = getCurrentDate();
        String today = DayUtil.getTodayAbbreviation();
        String tomorrow = DayUtil.getTomorrowAbbreviation();

        executorService.submit(() -> {
            List<DailyEntity> results = dailyDatabase.getDailyDao().getDailyByDay(day);

            // clear existing lists
            dailyId.clear();
            dailyName.clear();
            dailyTotalIteration.clear();
            dailyRemainingIteration.clear();
            dailyLatestIteratedDate.clear();
            dailyDays.clear();
            dailyHour.clear();

            for (DailyEntity entity : results) {
                String id = String.valueOf(entity.getDailyId());
                String name = entity.getDailyName();
                String totalIteration = entity.dailyTotalIteration;
                String remainingIteration = entity.getDailyRemainingIteration();
                String latestIteratedDate = entity.getDailyLatestIteratedDate();
                String days = entity.dailyDays;
                String hour = entity.dailyHour;

                if (day.equals(today)) {
                    // 💡 Skip if remaining iteration is 0 or less (today only)
                    if (Integer.parseInt(remainingIteration) <= 0) {
                        continue;
                    }

                    // update latest iterated date if needed
                    if (!currentDate.equals(latestIteratedDate)) {
                        DailyEntity dailyEntity = new DailyEntity(name, totalIteration, remainingIteration, currentDate, hour, days);
                        dailyEntity.setDailyId(Integer.parseInt(id));
                        dailyDatabase.getDailyDao().updateDaily(dailyEntity);

                        latestIteratedDate = currentDate;
                    }

                } else if (day.equals(tomorrow)) {
                    // 📌 Tomorrow view: override remainingIteration with totalIteration
                    remainingIteration = totalIteration;
                }

                // add data to display lists
                dailyId.add(id);
                dailyName.add(name);
                dailyTotalIteration.add(totalIteration);
                dailyRemainingIteration.add(remainingIteration);
                dailyLatestIteratedDate.add(latestIteratedDate);
                dailyDays.add(days);
                dailyHour.add(hour);
            }

            handler.post(dailyAdapterDisplay::notifyDataSetChanged);
        });
    }

    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date now = new Date();
        return sdf.format(now);
    }

    public ItemTouchHelper getItemTouchHelper(Context context) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.RIGHT
        ) {
            @Override
            public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX > 0) {
                    View itemView = viewHolder.itemView;
                    float cornerRadius = 40f;
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setAntiAlias(true);

                    float top = itemView.getTop();
                    float bottom = itemView.getBottom();
                    float left = itemView.getLeft();
                    float right = itemView.getLeft() + dX;

                    RectF rect = new RectF(left, top, right, bottom);
                    Path path = new Path();

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

                    canvas.save();
                    canvas.clipPath(path);
                    canvas.drawRect(rect, paint);
                    canvas.restore();

                    // Draw swipe text
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(40);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

                    String swipeText = "Swiping to next phase";
                    float textWidth = paint.measureText(swipeText);
                    float textX = itemView.getLeft() + 40;
                    float textY = top + (itemView.getHeight() / 2f) + 15;

                    canvas.drawText(swipeText, textX, textY, paint);
                }

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }


            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (currentDay != null && currentDay.equals(tomorrow)) {
                    return 0;
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
                //update the daily in the database
                String id = dailyId.get(position);
                String name = dailyName.get(position);
                String totalIteration = dailyTotalIteration.get(position);
                String remainingIteration = dailyRemainingIteration.get(position);
                String latestIteratedDate = dailyLatestIteratedDate.get(position);
                String days = dailyDays.get(position);
                String hour = dailyHour.get(position);

                dailyEntity = new DailyEntity(name, totalIteration, String.valueOf(Integer.parseInt(remainingIteration) - 1), latestIteratedDate, hour, days);
                dailyEntity.setDailyId(Integer.parseInt(id));

                executorService.execute(() -> {
                    initializeDatabase();
                    dailyDatabase.getDailyDao().updateDaily(dailyEntity);

                    handler.post(() -> {
                        if (Integer.parseInt(remainingIteration) - 1 <= 0) {
                            // Remove from all lists
                            dailyId.remove(position);
                            dailyName.remove(position);
                            dailyTotalIteration.remove(position);
                            dailyRemainingIteration.remove(position);
                            dailyLatestIteratedDate.remove(position);
                            dailyHour.remove(position);
                            dailyDays.remove(position);

                            // Notify RecyclerView about item removal
                            dailyAdapterDisplay.notifyItemRemoved(position);
                            dailyAdapterDisplay.notifyItemChanged(position, dailyId.size() - 1);
                        } else {
                            // Update the data in list
                            dailyRemainingIteration.set(position, String.valueOf(Integer.parseInt(remainingIteration) - 1));

                            // Notify RecyclerView to redraw this item
                            dailyAdapterDisplay.notifyItemChanged(position);
                        }
                    });
                });
            }
        };

        return new ItemTouchHelper(simpleCallback);
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
}