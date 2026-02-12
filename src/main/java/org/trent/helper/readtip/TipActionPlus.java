package org.trent.helper.readtip;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import org.trent.helper.settings.AppSettingsState;

import java.awt.*;
import java.util.Objects;

public class TipActionPlus extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 检查功能是否启用
        AppSettingsState settings = AppSettingsState.getInstance();
        if (!settings.readTipEnabled) {
            // 功能被禁用时显示提示
            Render.getInstance().render(e, "阅读提示功能已禁用，请在设置中启用");
            return;
        }
        Render.getInstance().render(e, HandleUtils.getTipContentPlus());
    }
}
