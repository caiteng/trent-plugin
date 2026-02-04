package org.trent.helper.settings;

import org.trent.helper.readtip.ReadTipState;

import javax.swing.*;
import java.awt.*;

public class MybatisPanel implements Setting {
    private JPanel panel;
    private JRadioButton enableXmlInterfaceJumpRadio;
    private JRadioButton enabledClickLightRadio;
    private JRadioButton enabledReadTipsRadio;
    private JTextField readPath;
    private JButton left;
    private JButton right;
    private JTextField titleText;
    private JTextField nextURL;
    private JTextField textField2;
    private JTextField textField3;
    private JTable table1;

    private final AppSettingsState appSettings;
    private final ReadTipState readTipSettings;

    public MybatisPanel(AppSettingsState appSettings, ReadTipState readTipSettings) {
        this.appSettings = appSettings;
        this.readTipSettings = readTipSettings;
//        init();
    }

//    private void init() {
//        ReadTipState readTipState = ReadTipState.getInstance();
//        titleText.setText(readTipState.title);
//        titleText.setEnabled(false);
//        left.addActionListener(e -> {
//            ReadTipState state = ReadTipState.getInstance();
//            HandleUtils.loadPreviousList(state);
//            titleText.setText(state.title);
//        });
//        right.addActionListener(e -> {
//            ReadTipState state = ReadTipState.getInstance();
//            HandleUtils.loadNextList(state);
//            titleText.setText(state.title);
//        });
//    }

    @Override
    public Component createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        boolean modified = !enabledReadTipsRadio.isSelected() == appSettings.enableXmlInterfaceJump;
        modified |= !nextURL.getText().equals(readTipSettings.nextURL);
        return modified;
    }

    @Override
    public void reset() {
        enabledReadTipsRadio.setSelected(appSettings.readTipEnabled);
        nextURL.setText(readTipSettings.nextURL);
    }

    @Override
    public void apply() {
        appSettings.readTipEnabled = enabledReadTipsRadio.isSelected();
        readTipSettings.nextURL = nextURL.getText();
    }

}
