package com.android.focusflow;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyAdapterDisplay extends RecyclerView.Adapter<DailyAdapterDisplay.MyViewHolder> {

    Context context;
    ArrayList<String>
            dailyId,
            dailyName,
            dailyTotalIteration,
            dailyRemainingIteration,
            dailyLatestIteratedDate,
            dailyHour,
            dailyWeekDays;

    ExecutorService executorService = Executors.newCachedThreadPool();
    Handler handler = new Handler(Looper.getMainLooper());

    DailyEntity dailyEntity;
    List<DailyEntity> dailyEntities;

    String[] dailyActiveDays;
    String dailyDays;
    DailyDatabase dailyDatabase;

    public DailyAdapterDisplay(
            Context context,
            ArrayList<String> dailyId,
            ArrayList<String> dailyName,
            ArrayList<String> dailyTotalIteration,
            ArrayList<String> dailyRemainingIteration,
            ArrayList<String> dailyLatestIteratedDate,
            ArrayList<String> dailyHour,
            ArrayList<String> dailyWeekDays) {

        this.context = context;
        this.dailyId = dailyId;
        this.dailyName = dailyName;
        this.dailyTotalIteration = dailyTotalIteration;
        this.dailyRemainingIteration = dailyRemainingIteration;
        this.dailyLatestIteratedDate = dailyLatestIteratedDate;
        this.dailyHour = dailyHour;
        this.dailyWeekDays = dailyWeekDays;
    }

    @NonNull
    @Override
    public DailyAdapterDisplay.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.daily_card_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyAdapterDisplay.MyViewHolder holder, int position) {

        ArrayList<String> activeDaySunToSat = new ArrayList<>();

        holder.tvDailyId.setText(dailyId.get(position));
        holder.tvDailyName.setText(dailyName.get(position));
        holder.tvDailyIteration.setText(dailyRemainingIteration.get(position) + "/" + dailyTotalIteration.get(position));
        holder.tvDailyLatestIteratedDate.setText((dailyLatestIteratedDate.get(position)));
        holder.tvDailyHour.setText(dailyHour.get(position));
        holder.tvDailyDays.setText(dailyWeekDays.get(position));

        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, holder.btnMore);
                Menu menu = popupMenu.getMenu();

                popupMenu.getMenuInflater().inflate(R.menu.daily_card_menu, menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                       if (menuItem.getItemId() == R.id.mnuUpdate){
                           //show the display and set the data to the display

                           LayoutInflater inflater = LayoutInflater.from(context);
                           View view = inflater.inflate(R.layout.new_update_daily_task, null);
                           CheckBox cbDay;

                           ArrayList<Integer> cbIds = new ArrayList<>();

                           cbIds.add(R.id.cbSun);
                           cbIds.add(R.id.cbMon);
                           cbIds.add(R.id.cbTue);
                           cbIds.add(R.id.cbWed);
                           cbIds.add(R.id.cbThu);
                           cbIds.add(R.id.cbFri);
                           cbIds.add(R.id.cbSat);

                           Button btnCancel = view.findViewById(R.id.btnCancelNewDaily);
                           Button btnConfirm = view.findViewById(R.id.btnConfirmNewDaily);
                           EditText etDailyName = view.findViewById(R.id.etTaskName);
                           EditText etDailyTotalIteration = view.findViewById(R.id.etIteration);
                           TextView tvDailyHour = view.findViewById(R.id.tvTime);

                           //setting up data to the views
                           etDailyName.setText(dailyName.get(position));
                           etDailyTotalIteration.setText(dailyTotalIteration.get(position));
                           tvDailyHour.setText(dailyHour.get(position));

                           String daysActive = dailyWeekDays.get(position);
                           List<String> result = parseDays(daysActive);

                           for(String day : result){
                               for(int days : cbIds){
                                  cbDay = view.findViewById(days);
                                  if(cbDay.getText().toString().equals(day)){
                                      cbDay.setChecked(true);
                                  }
                               }
                           }
                           AlertDialog.Builder dailyDialog = new AlertDialog.Builder(context);
                           dailyDialog.setView(view);
                           AlertDialog dialog = dailyDialog.create();

                           btnConfirm.setOnClickListener(view1 -> {
                                dialog.dismiss();

                                String updateDailyName = etDailyName.getText().toString().trim();
                                String updateDailyTotalIter = etDailyTotalIteration.getText().toString();
                                String updateDailyHour = tvDailyHour.getText().toString();

                                AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
                                confirmDialog.setTitle("Updating...");
                                confirmDialog.setMessage("Proceed to update?");

                                confirmDialog.setPositiveButton("Confirm", (dialogInterface, i) -> {

                                    for (int id : cbIds) {
                                        CheckBox cbDays = dialog.findViewById(id);
                                        activeDays(cbDays, activeDaySunToSat);
                                    }
                                    dailyDays = "";

                                    for(String days : activeDaySunToSat){
                                        dailyDays += days;
                                    }

                                    dailyEntity = new DailyEntity(updateDailyName,updateDailyTotalIter,updateDailyTotalIter, "not iterated", updateDailyHour, dailyDays);
                                    dailyEntity.setDailyId(Integer.parseInt(dailyId.get(position)));

                                    executorService.execute(() -> {
                                        initializeDatabase();

                                        dailyDatabase.getDailyDao().updateDaily(dailyEntity);

                                        handler.post(() -> {
                                            dailyName.set(position, updateDailyName);
                                            dailyTotalIteration.set(position, updateDailyTotalIter);
                                            dailyRemainingIteration.set(position, updateDailyTotalIter);
                                            dailyLatestIteratedDate.set(position, "");
                                            dailyHour.set(position, updateDailyHour);
                                            dailyWeekDays.set(position, dailyDays);
                                            notifyItemChanged(position);

                                            Toast.makeText(context, "Updated successfully!", Toast.LENGTH_SHORT).show();

                                        });
                                    });
                                });

                                confirmDialog.show();

                           });
                           btnCancel.setOnClickListener(view2 -> dialog.dismiss());

                           dialog.show();
                           return true;
                       }
                       else if(menuItem.getItemId() == R.id.mnuDelete){
                           AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                           dialog.setTitle("Deleting...");
                           dialog.setMessage("Are you sure you wanted to proceed?");
                           dialog.setCancelable(true);

                           dialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                                   //store data to delete
                                   dialogInterface.dismiss();
                                   //init database
                                   initializeDatabase();

                                   dailyEntity = new DailyEntity(
                                           dailyName.get(position),
                                           dailyTotalIteration.get(position),
                                           dailyRemainingIteration.get(position),
                                           dailyLatestIteratedDate.get(position),
                                           dailyHour.get(position),
                                           dailyWeekDays.get(position));
                                   dailyEntity.setDailyId(Integer.parseInt(dailyId.get(position)));

                                   // initiate deletion
                                    executorService.execute(() -> {
                                        dailyDatabase.getDailyDao().deleteDaily(dailyEntity);

                                        handler.post(() -> {
                                            dailyId.remove(position);
                                            dailyName.remove(position);
                                            dailyTotalIteration.remove(position);
                                            dailyRemainingIteration.remove(position);
                                            dailyLatestIteratedDate.remove(position);
                                            dailyHour.remove(position);
                                            dailyWeekDays.remove(position);

                                            notifyItemRemoved(position);
                                        });
                                    });
                               }
                           });
                           dialog.show();
                           return true;
                       }

                       return true;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dailyId.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvDailyId, tvDailyName, tvDailyIteration, tvDailyLatestIteratedDate, tvDailyHour, tvDailyDays;
        ImageButton btnMore;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDailyId = itemView.findViewById(R.id.tvDailyId);
            tvDailyName = itemView.findViewById(R.id.txtDailyTitle);
            tvDailyIteration = itemView.findViewById(R.id.txtIteration);
            tvDailyLatestIteratedDate = itemView.findViewById(R.id.txtIterationDate);
            tvDailyHour = itemView.findViewById(R.id.txtDailyHour);
            tvDailyDays = itemView.findViewById(R.id.txtDailyDays);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }

    public List<String> parseDays(String daysActive) {
        List<String> daysList = new ArrayList<>();

        // Split by space
        String[] parts = daysActive.trim().split("\\s+");

        for (String part : parts) {
            // Handle combined letters like "TH"
            daysList.add(part);
        }

        return daysList;
    }

    private void initializeDatabase(){
        RoomDatabase.Callback dailyCallBack = new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                super.onDestructiveMigration(db);
            }
        };
        dailyDatabase = Room.databaseBuilder(context.getApplicationContext(),
                DailyDatabase.class, "DailyDatabase").addCallback(dailyCallBack).build();
    }

    private void activeDays(@NonNull CheckBox cbDay, ArrayList<String> activeDaySunToSat) {
        if (cbDay.isChecked()) {
            activeDaySunToSat.add(cbDay.getText().toString() + " ");
        }
    }
}
