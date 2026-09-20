package org.trent.helper.readtip.local;

import org.trent.helper.db.DataSourceDao;
import org.trent.helper.readtip.common.ReadTipState;
import org.trent.helper.readtip.common.SharedUtils;

import java.util.ArrayList;
import java.util.function.IntConsumer;

/**
 * 本地书源处理器：负责 TXT 文件按固定行数切分、SQLite chunk 存取、翻页
 */
public class LocalSourceHandler {

    /**
     * 确保内容已加载：如果 chunk 未导入则从 TXT 导入，然后加载当前 chunk
     */
    public static void ensureContentLoaded(ReadTipState state) {
        ensureContentLoaded(state, null);
    }

    /**
     * 确保内容已加载：如果 chunk 未导入则从 TXT 导入，然后加载当前 chunk
     * @param progressCallback 可选的导入进度回调
     */
    public static void ensureContentLoaded(ReadTipState state, IntConsumer progressCallback) {
        if (state.textList.isEmpty()) {
            if (state.sourceId <= 0 || state.totalChunks == 0) {
                importChunksFromTxt(state, progressCallback);
            }
            if (state.totalChunks > 0) {
                loadChunkContent(state);
            }
        }
    }

    public static String nextPage(ReadTipState state) {
        ensureContentLoaded(state);
        // 先尝试在当前 chunk 内翻到下一个 display page
        if (state.index < state.textList.size() - 1) {
            state.index++;
            state.saveProgress();
            return state.textList.get(state.index);
        }
        // 当前 chunk 已到底，切换到下一个 chunk
        int nextChunk = state.chunkIndex + 1;
        if (nextChunk < state.totalChunks) {
            state.chunkIndex = nextChunk;
            state.index = 0;
            loadChunkContent(state);
            DataSourceDao.saveProgress(state.title, nextChunk, 0);
            return state.textList.isEmpty() ? "无可用内容" : state.textList.get(0);
        }
        return "已到达最后一页";
    }

    public static String prevPage(ReadTipState state) {
        ensureContentLoaded(state);
        // 先尝试在当前 chunk 内翻到上一个 display page
        if (state.index > 0) {
            state.index--;
            state.saveProgress();
            return state.textList.get(state.index);
        }
        // 当前 chunk 已到顶，切换到上一个 chunk
        if (state.chunkIndex > 0) {
            state.chunkIndex--;
            loadChunkContent(state);
            state.index = state.textList.size() - 1;
            DataSourceDao.saveProgress(state.title, state.chunkIndex, state.index);
            return state.textList.isEmpty() ? "无可用内容" : state.textList.get(state.index);
        }
        return "当前没有可返回的上一页";
    }

    /**
     * 从 TXT 文件导入 chunk 到 SQLite
     */
    public static void importChunksFromTxt(ReadTipState state) {
        importChunksFromTxt(state, null);
    }

    /**
     * 从 TXT 文件导入 chunk 到 SQLite，支持进度回调
     * @param progressCallback 可选的导入进度回调，参数为已处理行数
     */
    public static void importChunksFromTxt(ReadTipState state, IntConsumer progressCallback) {
        String filePath = state.baseURL;
        if (filePath == null || filePath.isEmpty()) return;
        if (state.sourceId <= 0 && state.title != null && !state.title.isEmpty()) {
            state.sourceId = DataSourceDao.getSourceIdByTitle(state.title);
        }
        if (state.sourceId <= 0) return;
        int total = DataSourceDao.importTxtFile(state.sourceId, filePath, progressCallback);
        state.totalChunks = total;
    }

    /**
     * 从 SQLite 加载当前 chunk 到 textList
     */
    public static void loadChunkContent(ReadTipState state) {
        String content = DataSourceDao.loadChunk(state.sourceId, state.chunkIndex);
        if (content != null && !content.isEmpty()) {
            // 将多行合并为连续文本（与远程源行为一致），再由 splitText 按固定长度切分
            String continuous = content.replace("\n", " ");
            state.textList = SharedUtils.splitText(continuous, SharedUtils.DEFAULT_CHUNK_SIZE);
        } else {
            state.textList = new ArrayList<>();
            state.textList.add(SharedUtils.FALLBACK_EMPTY_CONTENT);
        }
    }
}
