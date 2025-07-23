package com.android.focusflow;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ScheduleEntity.class}, version = 2)
public abstract class ScheduleDatabase extends RoomDatabase {

    public abstract ScheduleDAO getScheduleDAO();
}
