package org.trent.helper.settings;

import com.intellij.ui.IdeBorderFactory;

import javax.swing.*;
import java.awt.event.ItemEvent;


public class ClickLightPanel implements Setting {
    private JComboBox<String> comboSend;
    private JCheckBox chkNotify;
    private JCheckBox chkNotifyUnread;
    private JCheckBox chkSendBtn;
    private JPanel panel;
    private JCheckBox chkNotifyGroupMsg;
    private JCheckBox chkNotifyUnknown;
    private JCheckBox chkHideMyInput;
    private JCheckBox chkHistory;
    private JPanel sendGroup;
    private JPanel notifyGroup;
    private JPanel otherGroup;
    private JLabel version;
    private JCheckBox chkEnabled;
    private final AppSettingsState settings;

    public ClickLightPanel(AppSettingsState settings) {
        this.settings = settings;
        chkEnabled.setSelected(settings.clickLightEnabled);
        notifyGroup.setBorder(IdeBorderFactory.createTitledBorder("轨迹跟踪"));
        chkNotify.addItemListener(e -> {
            chkNotifyUnread.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            chkNotifyGroupMsg.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            chkNotifyUnknown.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        });
    }

    @Override
    public JComponent createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        return chkEnabled.isSelected() != settings.clickLightEnabled;
    }

    @Override
    public void apply() {
        settings.clickLightEnabled = chkEnabled.isSelected();
    }

    @Override
    public void reset() {
        chkEnabled.setSelected(settings.clickLightEnabled);
    }
}
