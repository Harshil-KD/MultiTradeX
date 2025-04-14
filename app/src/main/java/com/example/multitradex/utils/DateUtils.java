package com.example.multitradex.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String formatTimestamp(long timestampMillis) {
        Date date = new Date(timestampMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(date);
    }
}
