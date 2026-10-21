package com.abcgreen.evnet.util;

import java.util.List;

/** CSV export helper (Module III: file handling). */
public final class CsvUtil {
    private CsvUtil() {}

    public static String toCsv(String[] header, List<String[]> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", header)).append("\n");
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(escape(row[i]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static String escape(String v) {
        if (v == null) return "";
        return v.contains(",") || v.contains("\"") || v.contains("\n")
                ? '"' + v.replace("\"", "\"\"") + '"' : v;
    }
}
