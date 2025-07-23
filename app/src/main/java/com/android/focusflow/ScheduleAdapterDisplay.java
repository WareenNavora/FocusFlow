package com.android.focusflow;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleAdapterDisplay extends RecyclerView.Adapter<ScheduleAdapterDisplay.MyViewHolder> {
    Context context;
    ArrayList<String> taskId, taskName, taskDes, taskStart, taskEnd, taskProgress;
    String scheduledName, scheduleDescription, scheduledStartDate, scheduledEndDate, scheduledProgress;
    String progress;
    List<ScheduleEntity> scheduleEntities;
    ScheduleDatabase scheduleDatabase;
    ExecutorService executorService = Executors.newCachedThreadPool();
    Handler handler = new Handler(Looper.getMainLooper());


    ScheduleAdapterDisplay(Context context, ArrayList<String> taskId,ArrayList<String> taskName, ArrayList<String> taskDes, ArrayList<String> taskStart, ArrayList<String> taskEnd, ArrayList<String> taskProgress){
        this.context = context;
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskDes = taskDes;
        this.taskStart = taskStart;
        this.taskEnd = taskEnd;
        this.taskProgress = taskProgress;
    }

    @NonNull
    @Override
    public ScheduleAdapterDisplay.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.scheduled_task_card, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleAdapterDisplay.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (taskId.isEmpty() || taskName.isEmpty() || taskDes.isEmpty() || taskStart.isEmpty() || taskEnd.isEmpty() || taskProgress.isEmpty() || position >= taskId.size()) {
            return;
        }

        holder.tv_taskId.setText(String.valueOf(taskId.get(position)));
        holder.tv_taskName.setText(String.valueOf(taskName.get(position)));
        holder.tv_taskDes.setText(String.valueOf(taskDes.get(position)));
        holder.tv_taskStart.setText(String.valueOf(taskStart.get(position)));
        holder.tv_taskEnd.setText(String.valueOf(taskEnd.get(position)));
        holder.tv_taskProgress.setText(String.valueOf(taskProgress.get(position)));

        holder.imgv_taskMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.imgv_taskMenu);
                Menu menu = popupMenu.getMenu();

                popupMenu.getMenuInflater().inflate(R.menu.schedule_card_menu, menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        if (menuItem.getItemId() == R.id.mnuUpdate){

                            // inflate the design to update the current task selected
                            Toast.makeText(context, "Update the data of " + holder.tv_taskId.getText().toString(), Toast.LENGTH_SHORT).show();
                            LayoutInflater inflater = LayoutInflater.from(context);
                            View view = inflater.inflate(R.layout.new_update_scheduled_task, null);

                            // referencing the views
                            Button btnCancel = view.findViewById(R.id.btnCancelNewScheduled);
                            Button btnConfirm = view.findViewById(R.id.btnConfirmNewScheduled);

                            TextView tvStartDate = view.findViewById(R.id.tvStartDate);
                            TextView tvEndDate = view.findViewById(R.id.tvEndDate);
                            TextView tvScheduledName = view.findViewById(R.id.etTaskName);
                            TextView tvScheduledDescription = view.findViewById(R.id.etTaskDescription);

                            // Set data from holder to the views
                            tvScheduledName.setText(holder.tv_taskName.getText().toString());
                            tvScheduledDescription.setText(holder.tv_taskDes.getText().toString());
                            tvStartDate.setText(holder.tv_taskStart.getText().toString());
                            tvEndDate.setText(holder.tv_taskEnd.getText().toString());

                            // Create the AlertDialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setView(view);
                            builder.setCancelable(true);

                            // Show the dialog
                            AlertDialog dialog = builder.create();

                            btnConfirm.setOnClickListener(view5 -> {
                                AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(context);
                                confirmBuilder.setCancelable(true);
                                confirmBuilder.setTitle("Updating...");
                                confirmBuilder.setMessage("Do you want to proceed?");

                                AlertDialog confirmDialog = confirmBuilder.create();

                                confirmBuilder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                                    if (!tvScheduledName.getText().toString().isEmpty()
                                            && !tvScheduledDescription.getText().toString().isEmpty()
                                            && !tvStartDate.getText().toString().isEmpty()
                                            && !tvEndDate.getText().toString().isEmpty()) {

                                        scheduledName = tvScheduledName.getText().toString();
                                        scheduleDescription = tvScheduledDescription.getText().toString();
                                        scheduledStartDate = tvStartDate.getText().toString();
                                        scheduledEndDate = tvEndDate.getText().toString();

                                        initializeDatabase();

                                        ScheduleEntity scheduleEntity = new ScheduleEntity(scheduledName, scheduleDescription, scheduledStartDate, scheduledEndDate, "Do");
                                        scheduleEntity.setTaskId(Integer.parseInt(taskId.get(position)));

                                        executorService.execute(() -> {
                                            try {
                                                scheduleDatabase.getScheduleDAO().editSchedule(scheduleEntity);

                                                handler.post(() -> {
                                                    taskName.set(position, scheduledName);
                                                    taskDes.set(position, scheduleDescription);
                                                    taskStart.set(position, scheduledStartDate);
                                                    taskEnd.set(position, scheduledEndDate);
                                                    notifyItemChanged(position);

                                                    Toast.makeText(context, "Schedule is successfully updated!", Toast.LENGTH_SHORT).show();
                                                    confirmDialog.dismiss();
                                                    dialog.dismiss();
                                                });
                                            } catch (Exception e) {
                                                handler.post(() -> {
                                                    Toast.makeText(context, "Failed to update schedule: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    confirmDialog.dismiss();
                                                });
                                            }
                                        });

                                        executorService.shutdown();
                                    } else {
                                        Toast.makeText(context, "Missing or invalid input", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                confirmBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

                                confirmBuilder.show();
                            });


                            btnCancel.setOnClickListener(view3 -> {
                                Toast.makeText(context, "Canceled!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                            tvStartDate.setOnClickListener(view2 -> openDatePickerAndSetToTextView(tvStartDate));
                            tvEndDate.setOnClickListener(view1 -> openDatePickerAndSetToTextView(tvEndDate));

                            dialog.show();
                            return true;
                        }

                        else if(menuItem.getItemId() == R.id.mnuDelete){
                            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                            dialog.setTitle("Deleting...");
                            dialog.setMessage("Do you want to proceed?");

                            dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    initializeDatabase();

                                    ScheduleEntity scheduleEntity = new ScheduleEntity(taskName.get(position), taskDes.get(position), taskStart.get(position), taskEnd.get(position), taskProgress.get(position));
                                    scheduleEntity.setTaskId(Integer.parseInt(taskId.get(position)));

                                    executorService.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            scheduleDatabase.getScheduleDAO().deleteSchedule(scheduleEntity);

                                            handler.post(() -> {
                                                taskId.remove(position);
                                                taskName.remove(position);
                                                taskDes.remove(position);
                                                taskStart.remove(position);
                                                taskEnd.remove(position);
                                                taskProgress.remove(position);
                                                notifyItemRemoved(position);
                                                Toast.makeText(context, "Deleted successfully!", Toast.LENGTH_SHORT).show();
                                            });

                                        }
                                    });
                                    //process the deleting process
                                }
                            });

                            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            dialog.show();

                            return true;
                        }

                        else if(menuItem.getItemId() == R.id.mnuNextPhase){
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Next phase!");
                            builder.setMessage("Go to the next phase?");

                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //update the taskProgress to doing
                                    initializeDatabase();

                                    if (holder.tv_taskProgress.getText().toString().equals("Do")){
                                        progress = "Doing";
                                    }
                                    else if (holder.tv_taskProgress.getText().toString().equals("Doing")){
                                        progress = "Done";
                                    }
                                    else if (holder.tv_taskProgress.getText().toString().equals("Done")){
                                        Toast.makeText(context, "You are now already reached the final phase!", Toast.LENGTH_SHORT).show();
                                    }

                                    ScheduleEntity scheduleEntity = new ScheduleEntity(taskName.get(position), taskDes.get(position), taskStart.get(position), taskEnd.get(position), progress);
                                    scheduleEntity.setTaskId(Integer.parseInt(taskId.get(position)));

                                    if (executorService == null || executorService.isShutdown()) {
                                        executorService = Executors.newCachedThreadPool();
                                    }

                                    executorService.execute(() -> {
                                        try {
                                            scheduleDatabase.getScheduleDAO().editSchedule(scheduleEntity);
                                            handler.post(() -> {
                                                taskProgress.set(position, progress);
                                                notifyItemChanged(position);
                                                Toast.makeText(context, "Updated to the next phase successfully!", Toast.LENGTH_SHORT).show();
                                            });

                                        } catch (Exception e) {
                                            handler.post(() ->
                                                    Toast.makeText(context, "Failed to update: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                            );
                                        }
                                    });
                                    executorService.shutdown();
                                }
                            });

                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Do nothing
                                }
                            });
                            builder.show();

                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskId.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView  tv_taskId, tv_taskName, tv_taskDes, tv_taskStart, tv_taskEnd, tv_taskProgress;
        ImageButton imgv_taskMenu;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_taskId = itemView.findViewById(R.id.tv_scheduleId);
            tv_taskName = itemView.findViewById(R.id.tvScheduledTaskName);
            tv_taskDes = itemView.findViewById(R.id.tvScheduledTaskDescription);
            tv_taskStart = itemView.findViewById(R.id.tvStartDate);
            tv_taskEnd = itemView.findViewById(R.id.tvEndDate);
            tv_taskProgress = itemView.findViewById(R.id.tv_taskProgress);
            imgv_taskMenu = itemView.findViewById(R.id.imgvMore);
        }
    }

    private void openDatePickerAndSetToTextView(TextView targetTextView) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
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
        scheduleDatabase = Room.databaseBuilder(context.getApplicationContext(),
                ScheduleDatabase.class, "ScheduleDatabase").addCallback(scheduleCallback).build();
    }
}
