package com.android.focusflow;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tblDaily")
public class DailyEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Daily_id")
    int dailyId;

    @ColumnInfo(name = "Daily_name")
    String dailyName;

    @ColumnInfo(name = "Daily_total_iteration")
    String dailyTotalIteration;

    @ColumnInfo(name = "Daily_remaining_iteration")
    String dailyRemainingIteration;

    @ColumnInfo(name = "Daily_latest_iterated_date")
    String dailyLatestIteratedDate;

    @ColumnInfo(name = "Daily_hour")
    String dailyHour;

    @ColumnInfo(name = "Daily_days")
    String dailyDays;

    public DailyEntity(
            String dailyName,
            String dailyTotalIteration,
            String dailyRemainingIteration,
            String dailyLatestIteratedDate,
            String dailyHour,
            String dailyDays) {

        this.dailyName = dailyName;
        this.dailyTotalIteration = dailyTotalIteration;
        this.dailyRemainingIteration = dailyRemainingIteration;
        this.dailyLatestIteratedDate = dailyLatestIteratedDate;
        this.dailyHour = dailyHour;
        this.dailyDays = dailyDays;
    }

    public int getDailyId() {
        return dailyId;
    }

    public void setDailyId(int dailyId) {
        this.dailyId = dailyId;
    }

    public String getDailyName() {
        return dailyName;
    }

    public void setDailyName(String dailyName) {
        this.dailyName = dailyName;
    }

    public String getDailyTotalIteration() {
        return dailyTotalIteration;
    }

    public void setDailyTotalIteration(String dailyTotalIteration) {
        this.dailyTotalIteration = dailyTotalIteration;
    }

    public String getDailyRemainingIteration() {
        return dailyRemainingIteration;
    }

    public void setDailyRemainingIteration(String dailyRemainingIteration) {
        this.dailyRemainingIteration = dailyRemainingIteration;
    }

    public String getDailyLatestIteratedDate() {
        return dailyLatestIteratedDate;
    }

    public void setDailyLatestIteratedDate(String dailyLatestIteratedDate) {
        this.dailyLatestIteratedDate = dailyLatestIteratedDate;
    }

    public String getDailyHour() {
        return dailyHour;
    }

    public void setDailyHour(String dailyHour) {
        this.dailyHour = dailyHour;
    }

    public String getDailyDays() {
        return dailyDays;
    }

    public void setDailyDays(String dailyDays) {
        this.dailyDays = dailyDays;
    }
}
