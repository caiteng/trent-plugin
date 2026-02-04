package org.trent.helper.readtip;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.util.Objects;

public class TipActionPlus extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
       Render.getInstance().render(e, HandleUtils.getTipContentPlus());
    }
}
