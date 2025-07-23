package com.android.focusflow;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class main extends AppCompatActivity {

    BottomNavigationView bnv_main;
    ImageButton btnAddTask;
    String scheduledName, scheduleDescription, scheduledStartDate, scheduledEndDate;
    final String scheduleProgress = "Do";
    String dailyName, dailyIteration, dailyHour;
    String[] dailyActiveDays;
    String activeFragment;

    DailyDatabase dailyDatabase;
    DailyDao dailyDao;
    DailyEntity dailyEntity;

    ScheduleDatabase scheduleDatabase;
    ScheduleAdapterDisplay adapterDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeScheduleDatabase();
        initializeDailyDatabase();

        dailyActiveDays = new String[7];

        bnv_main = findViewById(R.id.bnv_main_menu);
        btnAddTask = findViewById(R.id.btnAddTask);

        //set default fragment
        setFragment(new ScheduleFrag());
        activeFragment = "ScheduleFrag";

        bnv_main.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId() == R.id.bnv_schedList){
                    setFragment(new ScheduleFrag());
                    activeFragment = "ScheduleFrag";
                }else if (item.getItemId() == R.id.bnv_dailyList){
                    setFragment(new DailyFrag());
                    activeFragment = "DailyFrag";
                }else if (item.getItemId() == R.id.bnv_stat){
                    setFragment(new StatisticsFrag());
                    activeFragment = "StatisticsFrag";
                }else if (item.getItemId() == R.id.bnv_more){
                    setFragment(new MoreFrag());
                    activeFragment = "MoreFrag";
                }
                return true;
            }
        });
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activeFragment.equals("ScheduleFrag")){
                    inflateScheduledTaskLayout();
                } else if(activeFragment.equals("DailyFrag")){
                    inflateDailyTaskLayout();
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(
                getWindow().getDecorView(),
                (view, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    View main = findViewById(R.id.main);
                    main.setPadding(
                            main.getPaddingLeft(),
                            main.getPaddingTop(),
                            main.getPaddingRight(),
                            systemBars.bottom
                    );
                    return WindowInsetsCompat.CONSUMED;
                }
        );
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
        scheduleDatabase = Room.databaseBuilder(getApplicationContext(),
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
        dailyDatabase = Room.databaseBuilder(getApplicationContext(),
                DailyDatabase.class, "DailyDatabase").addCallback(dailyCallback).build();
    }

    private void setFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fcv_main_fragment_container, fragment, null );
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commit();
    }

    private void activeDays(@NonNull CheckBox cbDay, ArrayList<String> activeDaySunToSat) {
        if (cbDay.isChecked()) {
            activeDaySunToSat.add(cbDay.getText().toString() + " ");
        }
    }

    private void inflateDailyTaskLayout(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.new_update_daily_task, null);

        String dailyName, dailyIteration, dailyHour;
        ArrayList<String> activeDaySunToSat = new ArrayList<>();
        ArrayList<Integer> dayIds = new ArrayList<>();

        //component
        Button btnCancel = dialogView.findViewById(R.id.btnCancelNewDaily);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmNewDaily);
        EditText etDailyName = dialogView.findViewById(R.id.etTaskName);
        EditText etDailyIteration = dialogView.findViewById(R.id.etIteration);
        TextView tvDailyHour = dialogView.findViewById(R.id.tvTime);

        dayIds.add(R.id.cbSun);
        dayIds.add(R.id.cbMon);
        dayIds.add(R.id.cbTue);
        dayIds.add(R.id.cbWed);
        dayIds.add(R.id.cbThu);
        dayIds.add(R.id.cbFri);
        dayIds.add(R.id.cbSat);

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();

        tvDailyHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTimePickerAndSetToTextView(tvDailyHour);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check and store every value of the checked radio button in list of strings
                for (int id : dayIds) {
                    CheckBox cbDays = dialogView.findViewById(id);
                    activeDays(cbDays, activeDaySunToSat);
                }
                //store in local variable
                String dailyName = etDailyName.getText().toString().trim();
                String dailyTotalIteration = etDailyIteration.getText().toString().trim();
                String dailyHour = tvDailyHour.getText().toString().trim();

                if(dailyHour.equals("")){
                    dailyHour = "Unscheduled";
                }

                String dailyDays = "";

                for(String days : activeDaySunToSat){
                    dailyDays += days;
                }

                dailyEntity = new DailyEntity(dailyName,dailyTotalIteration,dailyTotalIteration, "not iterated",dailyHour,dailyDays.trim());

                //adding the data to daily database
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                executorService.execute(() -> {
                    dailyDatabase.getDailyDao().addDaily(dailyEntity);

                    handler.post(dialog::dismiss);
                });

                activeDaySunToSat.clear();
            }
        });
        dialog.show();
    }

    private void inflateScheduledTaskLayout() {
        // Inflate the layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.new_update_scheduled_task, null);

        Button btnCancel = dialogView.findViewById(R.id.btnCancelNewScheduled);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmNewScheduled);
        TextView tvStartDate = dialogView.findViewById(R.id.tvStartDate);
        TextView tvEndDate = dialogView.findViewById(R.id.tvEndDate);
        TextView tvScheduledName = dialogView.findViewById(R.id.etTaskName);
        TextView tvScheduledDescription = dialogView.findViewById(R.id.etTaskDescription);

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);  // Set custom layout
        builder.setCancelable(true);  // Optional: allow dialog to be dismissed

        // Show the dialog
        AlertDialog dialog = builder.create();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get all the input data
                if (!tvScheduledName.getText().toString().isEmpty() && !tvScheduledDescription.getText().toString().isEmpty() && !tvStartDate.getText().toString().isEmpty() && !tvEndDate.getText().toString().isEmpty()) {
                    scheduledName = tvScheduledName.getText().toString();
                    scheduleDescription = tvScheduledDescription.getText().toString();
                    scheduledStartDate = tvStartDate.getText().toString();
                    scheduledEndDate = tvEndDate.getText().toString();

                    ScheduleEntity scheduleEntity = new ScheduleEntity(scheduledName, scheduleDescription, scheduledStartDate, scheduledEndDate, scheduleProgress);
                    addScheduleInBackground(scheduleEntity, new ScheduleCallback() {
                        @Override
                        public void onScheduleAdded(boolean success) {
                            if (success) {
                                Toast.makeText(main.this, "Added successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(main.this, "Error occured, please try again later!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }else{
                    Toast.makeText(main.this, "Missing or invalid input", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(main.this, "Canceled!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePickerAndSetToTextView(tvStartDate);
            }
        });
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePickerAndSetToTextView(tvEndDate);
            }
        });
        dialog.show();
    }

    private void addScheduleInBackground(ScheduleEntity scheduleEntity, ScheduleCallback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(() -> {
            try {
                scheduleDatabase.getScheduleDAO().addSchedule(scheduleEntity);

                handler.post(() -> callback.onScheduleAdded(true));

            } catch (Exception e) {

                handler.post(() -> callback.onScheduleAdded(false));

            }
        });
    }

    public interface ScheduleCallback {
        void onScheduleAdded(boolean success);
    }

    private void openDatePickerAndSetToTextView(TextView targetTextView) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                        String formattedDate = sdf.format(selectedDate.getTime());

                        targetTextView.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void openTimePickerAndSetToTextView(TextView targetTextView){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        // Do something with the selected time
                        String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        targetTextView.setText(time);
                    }
                }, hour, minute, true); // true for 24-hour format

        timePickerDialog.show();
    }

}
