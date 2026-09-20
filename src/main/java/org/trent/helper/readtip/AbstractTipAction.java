package org.trent.helper.readtip;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;
import org.trent.helper.settings.AppSettingsState;

public abstract class AbstractTipAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        AppSettingsState settings = AppSettingsState.getInstance();
        if (!settings.readTipEnabled) {
            Render.getInstance().render(e, "阅读提示功能已禁用，请先在设置中启用");
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Loading Read Tip", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String tip;
                try {
                    tip = loadTip();
                } catch (Exception ex) {
                    tip = "内容加载失败：" + safeMessage(ex);
                }
                Render.getInstance().render(e, tip);
            }
        });
    }

    protected abstract String loadTip();

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }
}
