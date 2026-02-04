package org.trent.helper.readtip;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class TipActionSub extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Render.getInstance().render(e, HandleUtils.getTipContentSub());

    }
}
