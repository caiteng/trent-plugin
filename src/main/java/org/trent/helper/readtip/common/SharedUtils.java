package org.trent.helper.readtip.common;

import java.util.ArrayList;
import java.util.List;

public class SharedUtils {

    public static final int DEFAULT_CHUNK_SIZE = 35;
    public static final String FALLBACK_EMPTY_CONTENT = "当前页面暂时没有解析到可显示的内容";

    private static final int MAX_PAGE_TURNS = 8;
    private static final int TIME_WINDOW_MS = 3000;
    private static long[] pageTurnTimestamps = new long[MAX_PAGE_TURNS];
    private static int pageIndex = 0;

    public static synchronized boolean checkPageTurnSpeed() {
        long currentTime = System.currentTimeMillis();
        int validCount = 0;
        for (int i = 0; i < pageIndex; i++) {
            if ((currentTime - pageTurnTimestamps[i]) <= TIME_WINDOW_MS) {
                pageTurnTimestamps[validCount] = pageTurnTimestamps[i];
                validCount++;
            }
        }
        pageIndex = validCount;
        if (pageIndex >= MAX_PAGE_TURNS) {
            return false;
        }
        pageTurnTimestamps[pageIndex] = currentTime;
        pageIndex++;
        return true;
    }

    public static List<String> splitText(String text, int chunkSize) {
        List<String> textList = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            textList.add(FALLBACK_EMPTY_CONTENT);
            return textList;
        }
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            textList.add(text.substring(i, end));
        }
        return textList;
    }

    public static void appendParagraph(StringBuilder builder, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(text.trim());
    }

    public static String defaultString(String value) {
        return value == null ? "" : value;
    }

    public static String friendlyMessage(Exception e, String fallback) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            return fallback;
        }
        return message;
    }

    public static String firstChunk(List<String> textList) {
        return textList.isEmpty() ? FALLBACK_EMPTY_CONTENT : textList.get(0);
    }

    public static boolean isLocalSource(ReadTipState state) {
        return "local".equals(state.type);
    }
}
