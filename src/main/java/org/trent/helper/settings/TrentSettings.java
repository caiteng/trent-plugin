package org.trent.helper.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.trent.helper.readtip.ReadTipState;

import javax.swing.*;


public class TrentSettings implements Configurable {
    private JPanel panel;
    private JBTabbedPane tabHost;
    private JBScrollPane scroll1;
    private final AppSettingsState settings;
    private final ReadTipState readTipSetting;

    private ReadTipPanel readTipPanel;

    public TrentSettings() {
        settings = AppSettingsState.getInstance();
        readTipSetting = ReadTipState.getInstance();

        initializeComponents();
    }

    private void initializeComponents() {
        // 首先清空所有现有的标签页
        tabHost.removeAll();
        
        // 阅读提示设置
        readTipPanel = new ReadTipPanel(settings, readTipSetting);
        scroll1.setBorder(BorderFactory.createTitledBorder("阅读提示设置"));
        scroll1.setViewportView(readTipPanel.createComponent());

        // 重新添加我们需要的标签页
        tabHost.addTab("Read Tip", scroll1);
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Trent Settings";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        return readTipPanel.isModified();
    }

    @Override
    public void apply() {
        readTipPanel.apply();
        settings.loadState(settings.getState());
        readTipSetting.loadState(readTipSetting.getState());
    }

    @Override
    public void reset() {
        readTipPanel.reset();
    }

}
