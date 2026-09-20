package org.trent.helper.readtip.remote;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.trent.helper.readtip.common.DataSource;
import org.trent.helper.readtip.common.ReadTipState;
import org.trent.helper.readtip.common.SharedUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 远程书源处理器：负责 URL 抓取、HTML 解析、翻页导航
 */
public class RemoteSourceHandler {

    public static void ensureContentLoaded(ReadTipState state) {
        if (state.textList.isEmpty()) {
            loadThisList(state);
        }
    }

    public static String nextPage(ReadTipState state) {
        ensureContentLoaded(state);
        if (state.index >= state.textList.size() - 1) {
            try {
                loadNextList(state);
                return SharedUtils.firstChunk(state.textList);
            } catch (Exception e) {
                return SharedUtils.friendlyMessage(e, "已到达最后一页");
            }
        }
        state.index = state.index + 1;
        if (state.index < state.textList.size()) {
            state.saveProgress();
            return state.textList.get(state.index);
        }
        return "已到达最后一页";
    }

    public static String prevPage(ReadTipState state) {
        ensureContentLoaded(state);
        if (state.index == 0) {
            return hasPreviousTarget(state) ? "已经是当前页的开头了" : "当前没有可返回的上一页";
        }
        state.index = state.index - 1;
        state.saveProgress();
        return state.textList.get(state.index);
    }

    public static void loadThisList(ReadTipState state) {
        String targetUrl = resolveTargetUrl(state.baseURL, state.thisURL);
        if (targetUrl == null) {
            throw new RuntimeException("当前页面地址未配置");
        }
        loadList(state, targetUrl);
    }

    public static void loadPreviousList(ReadTipState state) {
        String targetUrl = resolveNavigationUrl(state.baseURL, state.previousURL, state.thisURL);
        if (targetUrl == null) {
            throw new RuntimeException("当前页暂未解析到上一页地址，可以先继续阅读当前内容");
        }
        loadList(state, targetUrl);
    }

    public static void loadNextList(ReadTipState state) {
        String currentUrl = state.thisURL;
        String nextUrl = state.nextURL;
        String targetUrl = resolveNavigationUrl(state.baseURL, nextUrl, state.thisURL);
        if (targetUrl == null) {
            throw new RuntimeException("当前页暂未解析到下一页地址，可以稍后重试或检查数据源配置");
        }
        loadList(state, targetUrl);
        state.previousURL = currentUrl;
        state.thisURL = nextUrl;
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
                throw new RuntimeException("URL 不可用");
            }
            Document document = fetchDocument(newUrl);
            updateCurrentLocation(dataSource, document.location(), newUrl);
            dataSource.setTitle(firstText(document.select("h1"), newUrl));
            dataSource.setNextURL(getNext(document));
            dataSource.setPreviousURL(getPrevious(document));
        } catch (Exception e) {
            throw new RuntimeException(SharedUtils.friendlyMessage(e, "数据源解析失败，请检查地址是否正确"));
        }
        return dataSource;
    }

    public static String extractBaseUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        Pattern pattern = Pattern.compile("^(https?://[^/\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("URL 不可用");
    }

    // ==================== 内部方法 ====================

    private static void loadList(ReadTipState state, String url) {
        try {
            Document document = fetchDocument(url);
            Elements elements = document.select("p");
            updateCurrentLocation(state, document.location(), url);
            state.index = 0;
            state.textList = SharedUtils.splitText(extractText(document, elements), SharedUtils.DEFAULT_CHUNK_SIZE);
            state.nextURL = getNext(document);
            state.previousURL = getPrevious(document);
        } catch (IOException e) {
            throw new RuntimeException("页面加载失败，请稍后重试");
        }
    }

    private static Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Trent-Helper/1.0")
                .timeout(5000)
                .get();
    }

    private static String getPrevious(Document document) {
        Elements prevElements = document.select("a:contains(上一页)");
        if (prevElements.isEmpty()) prevElements = document.select("a:contains(上一章)");
        if (prevElements.isEmpty()) prevElements = document.select("a[href~=.*(prev|上一页|上一章).*]");
        if (prevElements.isEmpty()) {
            for (Element link : document.select("a[href]")) {
                String href = link.attr("href");
                if (href.contains("prev") || href.contains("上一页") || href.contains("上一章")) return href;
            }
            return "";
        }
        return prevElements.first().attr("href");
    }

    private static String getNext(Document document) {
        Elements nextElements = document.select("a.button");
        if (nextElements.isEmpty()) nextElements = document.select("a:contains(下一页)");
        if (nextElements.isEmpty()) nextElements = document.select("a:contains(下一章)");
        if (nextElements.isEmpty()) nextElements = document.select("a[href~=.*(next|下一页|下一章).*]");
        if (nextElements.isEmpty()) {
            for (Element link : document.select("a[href]")) {
                String href = link.attr("href");
                if (href.contains("next") || href.contains("下一页") || href.contains("下一章")) return href;
            }
            return "";
        }
        return nextElements.last().attr("href");
    }

    private static String extractText(Document document, Elements elements) {
        StringBuilder sb = new StringBuilder();
        int start = elements.size() > 2 ? 1 : 0;
        int end = elements.size() > 2 ? elements.size() - 1 : elements.size();
        for (int i = start; i < end; i++) {
            SharedUtils.appendParagraph(sb, elements.get(i).text());
        }
        if (sb.length() == 0) {
            SharedUtils.appendParagraph(sb, document.body() == null ? "" : document.body().text());
        }
        return sb.toString();
    }

    private static String buildUrl(String baseUrl, String path) {
        String safeBase = Objects.requireNonNullElse(baseUrl, "");
        String safePath = Objects.requireNonNullElse(path, "");
        if (safePath.startsWith("http://") || safePath.startsWith("https://")) return safePath;
        return safeBase + safePath;
    }

    private static String resolveTargetUrl(String baseUrl, String path) {
        if (path != null && !path.isBlank()) return buildUrl(baseUrl, path);
        if (baseUrl != null && !baseUrl.isBlank()) return baseUrl;
        return null;
    }

    private static String resolveNavigationUrl(String baseUrl, String navPath, String currentPath) {
        if (navPath != null && !navPath.isBlank()) return buildUrl(baseUrl, navPath);
        if (currentPath != null && !currentPath.isBlank()) return buildUrl(baseUrl, currentPath);
        return null;
    }

    private static void updateCurrentLocation(ReadTipState state, String docLocation, String fallbackUrl) {
        String actualUrl = firstNonBlank(docLocation, fallbackUrl);
        if (actualUrl == null) return;
        try {
            state.baseURL = extractBaseUrl(actualUrl);
            state.thisURL = extractPath(actualUrl);
        } catch (Exception ignored) {}
    }

    private static void updateCurrentLocation(DataSource ds, String docLocation, String fallbackUrl) {
        String actualUrl = firstNonBlank(docLocation, fallbackUrl);
        if (actualUrl == null) return;
        try {
            ds.setBaseURL(extractBaseUrl(actualUrl));
            ds.setThisURL(extractPath(actualUrl));
        } catch (Exception ignored) {}
    }

    private static String extractPath(String fullUrl) {
        try {
            if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) fullUrl = "https://" + fullUrl;
            Pattern pattern = Pattern.compile("^https?://[^/]+(/.*)?$");
            Matcher matcher = pattern.matcher(fullUrl);
            if (matcher.find()) {
                String path = matcher.group(1);
                return path == null ? "" : path;
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static boolean hasPreviousTarget(ReadTipState state) {
        return state.previousURL != null && !state.previousURL.isBlank();
    }

    private static String firstText(Elements elements, String fallback) {
        return elements.isEmpty() ? fallback : elements.first().text();
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return null;
    }
}
