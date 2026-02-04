package org.trent.helper.settings;

import com.intellij.openapi.util.text.StringUtil;
import org.trent.helper.readtip.DataSource;
import org.trent.helper.readtip.HandleUtils;
import org.trent.helper.readtip.ReadTipState;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadTipPanel implements Setting {
    private JCheckBox chkEnabled;
    private JTextField title;
    private JComboBox<String> comboDataSource;
    private JTextField textApiKey;
    private JTextField textWelcome;
    private JCheckBox chkGroupAny;
    private JCheckBox chkFriendAny;
    private JTextField textReplyEmpty;
    private JPanel panel;
    private JTextField newUrl;
    private JButton load;
    private JButton button1;
    private JTextField newTitle;
    private JLabel message;
    private final AppSettingsState appSettings;
    private final ReadTipState readTipSetting;
    private final Map<String, DataSource> dataSourceMap;

    public ReadTipPanel(AppSettingsState appSettings, ReadTipState readTipSetting) {
        this.appSettings = appSettings;
        this.readTipSetting = readTipSetting;
        this.dataSourceMap = readTipSetting.dataSource.stream().collect(Collectors.toMap(DataSource::getTitle, v -> v));

        this.comboDataSource.setModel(new DefaultComboBoxModel<>(dataSourceMap.keySet().toArray(new String[]{})));
        this.comboDataSource.addActionListener(e -> {
            String selected = (String) comboDataSource.getSelectedItem();
            DataSource selectedDataSource = dataSourceMap.get(selected);
            title.setText(selectedDataSource.getTitle());
        });
        this.newUrl.addActionListener(e -> {
            this.load.setEnabled(StringUtil.isNotEmpty(this.newUrl.getText()) && StringUtil.isNotEmpty(this.newTitle.getText()));
        });
        this.load.addActionListener(e -> {
            DataSource dataSource;
            try {
                dataSource = HandleUtils.loadNewDataSource(newUrl.getText());
            } catch (Exception ex) {
                message.setForeground(Color.RED);
                message.setText(ex.getMessage());
                return;
            }
            dataSource.setTitle(newTitle.getText());
            dataSourceMap.put(dataSource.getTitle(), dataSource);
            this.comboDataSource.setModel(new DefaultComboBoxModel<>(dataSourceMap.keySet().toArray(new String[]{})));
            this.comboDataSource.setSelectedIndex(dataSourceMap.size() - 1);

            message.setForeground(Color.RED);
            message.setText("success!!!");
        });

    }

    @Override
    public Component createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        return chkEnabled.isSelected() != appSettings.clickLightEnabled;
    }

    @Override
    public void reset() {
        chkEnabled.setSelected(appSettings.readTipEnabled);
    }

    @Override
    public void apply() {
        appSettings.readTipEnabled = chkEnabled.isSelected();
    }
}
