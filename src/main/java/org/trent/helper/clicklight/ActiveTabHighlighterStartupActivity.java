package org.trent.helper.clicklight;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.ExtensionsArea;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.trent.helper.settings.AppSettingsState;


public class ActiveTabHighlighterStartupActivity implements ProjectActivity, DumbAware {

    private static final Logger logger = Logger.getInstance(ActiveTabHighlighterStartupActivity.class);


    public void init(Project project) {
        logger.debug("Initializing component");
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        MessageBusConnection connection = bus.connect();
        // 监听文件编辑事件
        TabHighlighterFileEditorListener tabHighlighterFileEditorListener = new TabHighlighterFileEditorListener(project);
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, tabHighlighterFileEditorListener);

        ExtensionsArea area = ApplicationManager.getApplication().getExtensionArea();
        area.registerExtensionPoint("com.intellij.editorTabColorProvider", CustomEditorTabColorProvider.class.getName(), ExtensionPoint.Kind.INTERFACE);
        area.getExtensionPoint("com.intellij.editorTabColorProvider").registerExtension(new CustomEditorTabColorProvider());

    }


    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        AppSettingsState settings = AppSettingsState.getInstance();
        if (settings != null && !settings.clickLightEnabled) {
            System.out.println("click light is disabled");
            return null;
        }
        init(project);
        return null;
    }
}