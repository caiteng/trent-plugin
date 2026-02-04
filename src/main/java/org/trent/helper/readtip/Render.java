package org.trent.helper.readtip;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.util.Objects;

public class Render {
    private static Render instance;
    private Balloon currentBalloon;

    private Render() {
    }

    public static synchronized Render getInstance() {
        if (instance == null) {
            instance = new Render();
        }
        return instance;
    }

    public void render(AnActionEvent e, String tip) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (currentBalloon != null && !currentBalloon.isDisposed()) {
                currentBalloon.dispose();
            }
            JBPopupFactory factory = JBPopupFactory.getInstance();
            // 使用 HTML 标签设置文本颜色为灰色
            String grayTip = "<html><body><span style='color: gray;'>" + tip + "</span></body></html>";
            BalloonBuilder builder = factory.createHtmlTextBalloonBuilder(grayTip, null, new JBColor(new Color(0, 0, 0, 0), new Color(0, 0, 0, 0)), null);
            builder.setFadeoutTime(5000);
            builder.setBorderInsets(new Insets(0, 0, 0, 0));
            builder.setBorderColor(new JBColor(new Color(0, 0, 0, 0), new Color(0, 0, 0, 0)));
            builder.setCloseButtonEnabled(false);
            builder.setAnimationCycle(0);
            currentBalloon = builder.createBalloon();
            currentBalloon.show(factory.guessBestPopupLocation(Objects.requireNonNull(e.getData(PlatformDataKeys.EDITOR))), Balloon.Position.above);
        });
    }
}