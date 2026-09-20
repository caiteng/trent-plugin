package org.trent.helper.readtip.common;

import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.trent.helper.db.DataSourceDao;

import java.util.ArrayList;
import java.util.List;

@State(name = "ReadTipState", storages = @Storage("ReadTipStatePlugin.xml"))
public class ReadTipState implements PersistentStateComponent<ReadTipState> {

    private static final Logger LOG = Logger.getInstance(ReadTipState.class);
    private static final Gson GSON = new Gson();

    public String baseURL;
    public String nextURL;
    public String thisURL;
    public String previousURL;
    public String title;
    public String type = "remote";
    public List<String> textList = new ArrayList<>();
    public int index = 0;

    // 本地书源专用字段
    public int chunkIndex = 0;

    // 运行时计算，不参与持久化
    public transient int sourceId = -1;
    public transient int totalChunks = 0;

    public List<String> dataSourceList = new ArrayList<>();
    public List<DataSource> dataSource = new ArrayList<>();

    public static ReadTipState getInstance() {
        ReadTipState instance = ApplicationManager.getApplication().getService(ReadTipState.class);
        instance.syncActiveSource();
        if (!"local".equals(instance.type)) {
            instance.baseURL = StringUtil.isEmpty(instance.baseURL) ? "https://www.84kanshu.com" : instance.baseURL;
            instance.thisURL = StringUtil.isEmpty(instance.thisURL) ? "/book/94554459/121684942.html" : instance.thisURL;
        }
        LOG.debug("ReadTipState instance: " + instance);
        return instance;
    }

    /**
     * 确保内存状态与数据库 active_source 同步：
     * - active_source 与当前 title 不一致 → 调用 changeSource 切换
     * - title 一致但 sourceId 未初始化（如 IDE 重启后）→ 补填 transient 字段
     */
    private void syncActiveSource() {
        DataSource activeSource;
        try {
            activeSource = DataSourceDao.getActiveSource();
        } catch (Exception e) {
            LOG.debug("Failed to query active_source: " + e.getMessage());
            return;
        }
        if (activeSource == null) return;

        String activeTitle = activeSource.getTitle();
        String currentTitle = this.title;
        boolean titleMatch = activeTitle != null && activeTitle.equals(currentTitle);

        if (!titleMatch) {
            changeSource(activeSource);
        } else if ("local".equals(this.type) && this.sourceId <= 0) {
            // title 匹配但 transient 字段未初始化（IDE 重启等场景）
            this.sourceId = DataSourceDao.getSourceIdByTitle(this.title);
            this.totalChunks = this.sourceId > 0 ? DataSourceDao.getTotalChunks(this.sourceId) : 0;
        }
    }

    @Nullable
    @Override
    public ReadTipState getState() {
        if (!dataSource.isEmpty()) {
            dataSourceList = serializeDataSource(dataSource);
        }
        return this;
    }

    @Override
    public void loadState(@NotNull ReadTipState state) {
        baseURL = state.baseURL;
        type = state.type == null ? "remote" : state.type;
        thisURL = state.thisURL;
        previousURL = state.previousURL;
        textList = new ArrayList<>();
        index = state.index;
        nextURL = state.nextURL;
        title = state.title;
        chunkIndex = state.chunkIndex;
        dataSourceList = state.dataSourceList == null ? new ArrayList<>() : new ArrayList<>(state.dataSourceList);
        deserializeDataSource(state.dataSourceList);
    }

    public List<String> serializeDataSource(List<DataSource> dataSource) {
        List<String> serializedData = new ArrayList<>();
        for (DataSource dataSourceItem : dataSource) {
            serializedData.add(GSON.toJson(dataSourceItem));
        }
        return serializedData;
    }

    public void deserializeDataSource(List<String> serializedData) {
        dataSource.clear();
        if (serializedData == null) {
            return;
        }
        for (String json : serializedData) {
            DataSource item = GSON.fromJson(json, DataSource.class);
            if (item != null) {
                dataSource.add(item);
            }
        }
    }

    public void changeSource(DataSource dataSource) {
        type = dataSource.getType();
        baseURL = dataSource.getBaseURL();
        thisURL = dataSource.getThisURL();
        previousURL = dataSource.getPreviousURL();
        nextURL = dataSource.getNextURL();
        title = dataSource.getTitle();
        index = dataSource.getIndex();
        textList = new ArrayList<>();
        if ("local".equals(type)) {
            chunkIndex = dataSource.getLocalChunkIndex();
            sourceId = DataSourceDao.getSourceIdByTitle(title);
            totalChunks = sourceId > 0 ? DataSourceDao.getTotalChunks(sourceId) : 0;
        } else {
            chunkIndex = 0;
            sourceId = -1;
            totalChunks = 0;
        }
    }

    public void saveProgress() {
        if (title == null || title.isEmpty()) return;
        if ("local".equals(type)) {
            DataSourceDao.saveProgress(title, chunkIndex, index);
        } else {
            DataSourceDao.saveProgress(title, index);
        }
    }
}
