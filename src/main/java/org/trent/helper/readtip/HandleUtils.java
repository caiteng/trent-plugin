package org.trent.helper.readtip;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HandleUtils {

    public static String getTipContentPlus() {
        ReadTipState readTipState = ReadTipState.getInstance();
        if (readTipState.textList.isEmpty()) {
            loadThisList(readTipState);
        }
        
        // 检查是否需要加载下一页
        if (readTipState.index >= readTipState.textList.size() - 1) {
            try {
                loadNextList(readTipState);
                return readTipState.textList.get(0);
            } catch (Exception e) {
                return "已到达最后一页";
            }
        } else {
            readTipState.index = readTipState.index + 1;
            // 双重检查边界
            if (readTipState.index < readTipState.textList.size()) {
                return readTipState.textList.get(readTipState.index);
            } else {
                return "已到达最后一页";
            }
        }
    }

    public static String getTipContentSub() {
        ReadTipState readTipState = ReadTipState.getInstance();
        if (readTipState.textList.isEmpty()) {
            loadThisList(readTipState);
        }
        if (readTipState.index == 0) {
            return "NOT DATA";
        }
        readTipState.index = readTipState.index - 1;
        return readTipState.textList.get(readTipState.index);
    }

    public static String getTipContent() {
        ReadTipState readTipState = ReadTipState.getInstance();
        if (readTipState.textList.isEmpty()) {
            loadThisList(readTipState);
        }
        // 边界检查
        if (readTipState.index >= 0 && readTipState.index < readTipState.textList.size()) {
            return readTipState.textList.get(readTipState.index);
        } else {
            return "无可用内容";
        }
    }


    public static void loadThisList(ReadTipState readTipState) {
        loadList(readTipState, readTipState.baseURL + readTipState.thisURL);
    }

    public static void loadPreviousList(ReadTipState readTipState) {
        loadList(readTipState, readTipState.baseURL + readTipState.previousURL);
    }

    public static void loadNextList(ReadTipState readTipState) {
        readTipState.previousURL = readTipState.thisURL;
        readTipState.thisURL = readTipState.nextURL;
        loadList(readTipState, readTipState.baseURL + readTipState.nextURL);
    }

    private static void loadList(ReadTipState readTipState, String url) {
        try {
            Document document = Jsoup.connect(url).get();
            Elements elements = document.select("p");
            StringBuilder stringBuffer = new StringBuilder();
            for (int i = 1; i < elements.size() - 1; i++) {
                Element element = elements.get(i);
                stringBuffer.append(element.text());
            }
            String text = stringBuffer.toString();
            List<String> textList = new ArrayList<>();
            for (int i = 0; i < text.length(); i += 35) {
                int end = Math.min(i + 35, text.length());
                textList.add(text.substring(i, end));
            }
            readTipState.index = 0;
            readTipState.chapterTitle = document.select("h1").get(0).text();
            readTipState.textList = textList;
            readTipState.nextURL = getNext(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static DataSource loadNewDataSource(String newUrl) {
        DataSource dataSource = new DataSource();
        try {
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                newUrl = "https://" + newUrl;
            }
            Pattern pattern = Pattern.compile("^(https?://[^/]+)(/.*)?$");
            Matcher matcher = pattern.matcher(newUrl);

            if (matcher.find()) {
                dataSource.setBaseURL(matcher.group(1));
                dataSource.setThisURL(matcher.group(2) == null ? "" : matcher.group(2));
            } else {
                throw new RuntimeException("url不可用");
            }
            Document document = Jsoup.connect(newUrl).get();
            Elements elements = document.select("p");
            StringBuilder stringBuffer = new StringBuilder();
            for (int i = 1; i < elements.size() - 1; i++) {
                Element element = elements.get(i);
                stringBuffer.append(element.text());
            }
            dataSource.setTitle(document.select("h1").get(0).text());
            dataSource.setNextURL(getNext(document));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return dataSource;
    }

    public static String extractBaseUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url; // 默认添加 https
        }
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(https?://[^/\\s]+)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("url不可用");
    }

    private static String getNext(Document document) {
        Elements contentElements = document.select("a.button");
        if (contentElements.isEmpty()) {
            throw new RuntimeException("END.");
        }
        return contentElements.last().attr("href");
    }


}
