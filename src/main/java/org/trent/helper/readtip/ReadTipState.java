package org.trent.helper.readtip;

import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.json.editor.smartEnter.JsonSmartEnterProcessor.LOG;

@State(name = "ReadTipState", storages = @Storage("ReadTipStatePlugin.xml"))
public class ReadTipState implements PersistentStateComponent<ReadTipState> {

    public String baseURL;
    public String nextURL;
    public String thisURL;
    public String previousURL;
    public String title;
    public String chapterTitle;
    public List<String> textList = new ArrayList<>();
    public int index = 0;

    public List<String> dataSourceList = new ArrayList<>();
    public List<DataSource> dataSource = new ArrayList<>();

    public static ReadTipState getInstance() {
        ReadTipState instance = ApplicationManager.getApplication().getService(ReadTipState.class);
        instance.baseURL = StringUtil.isEmpty(instance.baseURL) ? "https://www.84kanshu.com" : instance.baseURL;
        instance.thisURL = StringUtil.isEmpty(instance.thisURL) ? "/book/94554459/121684942.html" : instance.thisURL;
        LOG.info("AppSettingsState instance: " + instance);
        return instance.getState();
    }

    @Nullable
    @Override
    public ReadTipState getState() {
        deserializeDataSource(this.dataSourceList);
        return this;
    }

    @Override
    public void loadState(@NotNull ReadTipState state) {
        dataSourceList = serializeDataSource(state.dataSource);
        baseURL = state.baseURL;
        thisURL = state.thisURL;
        previousURL = state.previousURL;
        textList = state.textList;
        index = state.index;
        nextURL = state.nextURL;
        title = state.title;
        chapterTitle = state.chapterTitle;
    }

    private static final Gson gson = new Gson();

    public List<String> serializeDataSource(List<DataSource> dataSource) {
        List<String> serializedData = new ArrayList<>();
        for (DataSource dataSourceItem : dataSource) {
            serializedData.add(gson.toJson(dataSourceItem));
        }
        return serializedData;
    }

    public void deserializeDataSource(List<String> serializedData) {
        dataSource.clear();
        for (String json : serializedData) {
            DataSource item = gson.fromJson(json, DataSource.class);
            dataSource.add(item);
        }
    }

    public void changeSource(DataSource dataSource) {
        baseURL = dataSource.getBaseURL();
        thisURL = dataSource.getThisURL();
        previousURL = dataSource.getPreviousURL();
        nextURL = dataSource.getNextURL();
        title = dataSource.getTitle();
    }
}