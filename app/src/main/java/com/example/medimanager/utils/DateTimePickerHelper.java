package com.example.medimanager.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Shared helpers for date/time pickers to reduce duplicated setup code.
 */
public final class DateTimePickerHelper {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());

    private DateTimePickerHelper() {
    }

    public interface OnDateSelected {
        void onDateSelected(String formattedDate, Calendar selectedCalendar);
    }

    public interface OnTimeSelected {
        void onTimeSelected(int hourOfDay, int minute, String formattedTime);
    }

    /**
        * Show a date picker dialog.
        * @param initialCalendar starting calendar (nullable -> today)
        * @param minToday if true, min date is today
        * @param maxToday if true, max date is today
        */
    public static void showDatePicker(Context context,
                                      Calendar initialCalendar,
                                      boolean minToday,
                                      boolean maxToday,
                                      OnDateSelected callback) {
        Calendar cal = initialCalendar != null ? (Calendar) initialCalendar.clone() : Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    String formatted = DATE_FORMATTER.format(selected.getTime());
                    callback.onDateSelected(formatted, selected);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        if (minToday) {
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        }
        if (maxToday) {
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }

        dialog.show();
    }

    /**
     * Show a time picker dialog, returning a formatted 12-hour time string (hh:mm AM/PM).
     */
    public static void showTimePicker(Context context,
                                      int initialHour,
                                      int initialMinute,
                                      OnTimeSelected callback) {
        TimePickerDialog dialog = new TimePickerDialog(
                context,
                (view, hourOfDay, minute) -> {
                    String[] amPmStrings = DateFormatSymbols.getInstance().getAmPmStrings();
                    String amPm = hourOfDay >= 12 ? amPmStrings[1] : amPmStrings[0];
                    int displayHour = hourOfDay % 12;
                    if (displayHour == 0) displayHour = 12;
                    String timeString = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
                    callback.onTimeSelected(hourOfDay, minute, timeString);
                },
                initialHour,
                initialMinute,
                false
        );
        dialog.show();
    }
}
