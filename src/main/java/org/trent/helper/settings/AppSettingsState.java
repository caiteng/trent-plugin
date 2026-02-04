package org.trent.helper.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.json.editor.smartEnter.JsonSmartEnterProcessor.LOG;

@State(name = "AppSettingsState", storages = @Storage("AppSettingsPlugin.xml"))
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {

    public boolean enableXmlInterfaceJump = false;
    public boolean clickLightEnabled = false;
    public boolean readTipEnabled = false;

    public static AppSettingsState getInstance() {
        AppSettingsState instance = ApplicationManager.getApplication().getService(AppSettingsState.class);
        LOG.info("AppSettingsState instance: " + instance);
        return instance;
    }

    @Nullable
    @Override
    public AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        enableXmlInterfaceJump = state.enableXmlInterfaceJump;
        clickLightEnabled = state.clickLightEnabled;
        readTipEnabled = state.readTipEnabled;
    }
}