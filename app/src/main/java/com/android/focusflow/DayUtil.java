package com.android.focusflow;

import java.util.Calendar;

public class DayUtil {
    public static String getTodayAbbreviation() {
        return getDayAbbreviation(0);
    }

    public static String getTomorrowAbbreviation() {
        return getDayAbbreviation(1);
    }

    private static String getDayAbbreviation(int daysAhead) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_WEEK, daysAhead);
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY: return "SU";
            case Calendar.MONDAY: return "M";
            case Calendar.TUESDAY: return "T";
            case Calendar.WEDNESDAY: return "W";
            case Calendar.THURSDAY: return "TH";
            case Calendar.FRIDAY: return "F";
            case Calendar.SATURDAY: return "SA";
            default: return "";
        }
    }
}
