package com.example.medimanager.utils;

import android.content.Context;

import com.example.medimanager.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    /**
     * Get current date in yyyy-MM-dd format
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get current time in hh:mm a format
     */
    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Get current date and time
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Format date from yyyy-MM-dd to display format (MMM dd, yyyy)
     */
    public static String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }

    /**
     * Calculate age from date of birth
     */
    public static int calculateAge(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isEmpty()) {
            return 0;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
            Date birthDate = sdf.parse(dateOfBirth);

            if (birthDate != null) {
                Calendar birth = Calendar.getInstance();
                birth.setTime(birthDate);
                Calendar today = Calendar.getInstance();

                int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

                if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }

                return age;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Parse date string to Date object
     */
    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Compare two dates (returns -1 if date1 < date2, 0 if equal, 1 if date1 > date2)
     */
    public static int compareDates(String date1, String date2) {
        Date d1 = parseDate(date1);
        Date d2 = parseDate(date2);

        if (d1 == null || d2 == null) {
            return 0;
        }

        return d1.compareTo(d2);
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(String dateString) {
        String today = getCurrentDate();
        return today.equals(dateString);
    }

    /**
     * Check if date is in this week
     */
    public static boolean isThisWeek(String dateString) {
        Date date = parseDate(dateString);
        if (date == null) {
            return false;
        }

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);

        Calendar todayCalendar = Calendar.getInstance();

        return dateCalendar.get(Calendar.WEEK_OF_YEAR) == todayCalendar.get(Calendar.WEEK_OF_YEAR)
                && dateCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR);
    }

    /**
     * Get day name from date (e.g., "Monday")
     */
    public static String getDayName(String dateString) {
        Date date = parseDate(dateString);
        if (date == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Get month name from date (e.g., "January")
     */
    public static String getMonthName(String dateString) {
        Date date = parseDate(dateString);
        if (date == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Get time ago string (e.g., "2 days ago")
     */
    public static String getTimeAgo(Context context, String dateString) {
        Date date = parseDate(dateString);
        if (date == null) {
            return "";
        }

        long timeInMillis = date.getTime();
        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (years > 0) {
            return context.getResources().getQuantityString(R.plurals.time_ago_years, (int) years, years);
        } else if (months > 0) {
            return context.getResources().getQuantityString(R.plurals.time_ago_months, (int) months, months);
        } else if (weeks > 0) {
            return context.getResources().getQuantityString(R.plurals.time_ago_weeks, (int) weeks, weeks);
        } else if (days > 0) {
            return context.getResources().getQuantityString(R.plurals.time_ago_days, (int) days, days);
        } else if (hours > 0) {
            return context.getResources().getQuantityString(R.plurals.time_ago_hours, (int) hours, hours);
        } else if (minutes > 0) {
            return context.getResources().getQuantityString(R.plurals.time_ago_minutes, (int) minutes, minutes);
        } else {
            return context.getString(R.string.just_now);
        }
    }

    /**
     * Validate date format
     */
    public static boolean isValidDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return false;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
