package org.trent.helper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.trent.helper.settings.AppSettingsState;

import java.util.Arrays;

public class AppSettings extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        AppSettingsState settings = AppSettingsState.getInstance();
        Messages.showDialog("XML-Interface Jump Enabled: " + settings.enableXmlInterfaceJump,
                "MyBatis Settings", Arrays.asList("OK", "KOI").toArray(new String[]{}), 1, Messages.getInformationIcon());
    }
}
