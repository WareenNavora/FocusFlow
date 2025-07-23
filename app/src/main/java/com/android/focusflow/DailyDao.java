package com.android.focusflow;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao()
public interface DailyDao {

    @Insert
    public void addDaily(DailyEntity dailyEntity);

    @Update
    public void updateDaily(DailyEntity dailyEntity);

    @Delete
    public void deleteDaily(DailyEntity dailyEntity);

    @Query("SELECT * FROM tbldaily")
    public List<DailyEntity> getAllDaily();

    @Query("SELECT COUNT(Daily_id) AS daily_size FROM tbldaily")
    public long getDailySize();
}
