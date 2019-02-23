package com.solightingstats.git.statistics.plugin.utils;

import org.apache.commons.lang3.StringUtils;

public class PrintoutUtils {
    public static String getFormattedColumn(Object data, Integer maxLength) {
        final String textValue = data.toString();
        return textValue
                .concat(
                        StringUtils
                            .repeat(" ", maxLength - textValue.length())  
                );
    } 
}
