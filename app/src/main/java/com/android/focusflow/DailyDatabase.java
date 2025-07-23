package com.android.focusflow;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DailyEntity.class}, version = 2)
public abstract class DailyDatabase extends RoomDatabase {

    public abstract DailyDao getDailyDao();
}
