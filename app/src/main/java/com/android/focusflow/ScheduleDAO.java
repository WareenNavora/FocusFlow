package com.android.focusflow;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ScheduleDAO {

    @Insert
    public void addSchedule(ScheduleEntity scheduleEntity);

    @Update
    public void editSchedule(ScheduleEntity scheduleEntity);

    @Delete
    public void deleteSchedule(ScheduleEntity scheduleEntity);

    @Query("SELECT * FROM tblSchedule")
    public List<ScheduleEntity> getAllSchedule();

    @Query("SELECT * FROM tblSchedule WHERE Task_Id==:scheduleEntity_id")
    public ScheduleEntity getSchedule(int scheduleEntity_id);

    @Query("SELECT COUNT(Task_Id) AS schedule_size FROM tblSchedule")
    public Long getScheduleSize();

    @Query("SELECT COUNT(Task_Progress) AS schedule_size FROM tblSchedule WHERE Task_Progress = 'Do'")
    public Long getScheduleDoSize();

    @Query("SELECT COUNT(Task_Progress) AS schedule_size FROM tblSchedule WHERE Task_Progress = 'Doing'")
    public Long getScheduleDoingSize();

    @Query("SELECT COUNT(Task_Progress) AS schedule_size FROM tblSchedule WHERE Task_Progress = 'Done'")
    public Long getScheduleDoneSize();

    @Query("SELECT * FROM tblSchedule WHERE Task_Progress = :progressStatus")
    List<ScheduleEntity> getSchedulesByProgress(String progressStatus);

    @Query("DELETE FROM tblschedule")
    public void deleteAllSchedule();

}
