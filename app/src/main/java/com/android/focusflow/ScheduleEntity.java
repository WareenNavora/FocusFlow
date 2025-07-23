package com.android.focusflow;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "tblSchedule")
public class ScheduleEntity {

    @ColumnInfo(name = "Task_Id")
    @PrimaryKey(autoGenerate = true)
    int taskId;

    @ColumnInfo(name = "Task_Name")
    String taskName;

    @ColumnInfo(name = "Task_Description")
    String taskDescription;

    @ColumnInfo(name = "Task_Start_Date")
    String taskStartDate;

    @ColumnInfo(name = "Task_End_Date")
    String taskEndDate;

    @ColumnInfo(name = "Task_Progress")
    String taskProgress;

    public ScheduleEntity(String taskName, String taskDescription, String taskStartDate, String taskEndDate, String taskProgress) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStartDate = taskStartDate;
        this.taskEndDate = taskEndDate;
        this.taskProgress = taskProgress;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskStartDate() {
        return taskStartDate;
    }

    public void setTaskStartDate(String taskStartDate) {
        this.taskStartDate = taskStartDate;
    }

    public String getTaskEndDate() {
        return taskEndDate;
    }

    public void setTaskEndDate(String taskEndDate) {
        this.taskEndDate = taskEndDate;
    }

    public String getTaskProgress() {
        return taskProgress;
    }

    public void setTaskProgress(String taskProgress) {
        this.taskProgress = taskProgress;
    }
}
