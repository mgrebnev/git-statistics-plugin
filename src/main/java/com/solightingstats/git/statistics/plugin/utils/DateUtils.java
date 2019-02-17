package com.solightingstats.git.statistics.plugin.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }
}
