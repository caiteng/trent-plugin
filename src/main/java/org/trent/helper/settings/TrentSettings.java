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
    private JBScrollPane scroll2;
    private JBScrollPane scroll3;
    private JBScrollPane scroll4;
    private final AppSettingsState settings;
    private final ReadTipState readTipSetting;

    private final ReadTipPanel readTipPanel;
    private final MybatisPanel mybatisPanel;
    private final ClickLightPanel clickLightPanel;
    private final MybatisPanel aaa2;

    public TrentSettings() {
        settings = AppSettingsState.getInstance();
        readTipSetting = ReadTipState.getInstance();

        clickLightPanel = new ClickLightPanel(settings);
        scroll1.setBorder(null);
        scroll1.setViewportView(clickLightPanel.createComponent());

        readTipPanel = new ReadTipPanel(settings, readTipSetting);
        scroll2.setBorder(null);
        scroll2.setViewportView(readTipPanel.createComponent());

        mybatisPanel = new MybatisPanel(settings, readTipSetting);
        scroll3.setBorder(null);
        scroll3.setViewportView(mybatisPanel.createComponent());

        aaa2 = new MybatisPanel(settings, readTipSetting);
        scroll4.setBorder(null);
        scroll4.setViewportView(aaa2.createComponent());
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
        return clickLightPanel.isModified() || mybatisPanel.isModified() || readTipPanel.isModified();
    }

    @Override
    public void apply() {
        clickLightPanel.apply();
        mybatisPanel.apply();
        readTipPanel.apply();
        settings.loadState(settings.getState());
        readTipSetting.loadState(readTipSetting.getState());
    }

    @Override
    public void reset() {
        clickLightPanel.reset();
        mybatisPanel.reset();
        readTipPanel.reset();
    }

}
