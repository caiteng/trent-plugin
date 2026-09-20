package org.trent.helper.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "AppSettingsState", storages = @Storage("AppSettingsPlugin.xml"))
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    private static final Logger LOG = Logger.getInstance(AppSettingsState.class);

    public boolean readTipEnabled = false;
    public String databasePath = "";

    public static AppSettingsState getInstance() {
        AppSettingsState instance = ApplicationManager.getApplication().getService(AppSettingsState.class);
        LOG.debug("AppSettingsState instance: " + instance);
        return instance;
    }

    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        readTipEnabled = state.readTipEnabled;
        databasePath = state.databasePath == null ? "" : state.databasePath;
    }
}
