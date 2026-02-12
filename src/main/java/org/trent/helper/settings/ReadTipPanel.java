package org.trent.helper.settings;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBScrollPane;
import org.trent.helper.readtip.DataSource;
import org.trent.helper.readtip.HandleUtils;
import org.trent.helper.readtip.ReadTipState;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadTipPanel implements Setting {
    // 基础设置组件
    private JCheckBox chkEnabled;
    private JTextField textBaseURL;
    private JTextField textThisURL;
    private JTextField textNextURL;
    private JTextField textPreviousURL;
    private JTextField textTitle;
    
    // 数据源管理组件
    private JComboBox<String> comboDataSource;
    private JTextField newUrl;
    private JTextField newTitle;
    private JButton btnLoadDataSource;
    private JButton btnDeleteDataSource;
    private JLabel lblMessage;
    
    // 导航测试按钮
    private JButton btnPrevTest;
    private JButton btnNextTest;
    
    // 主面板
    private JPanel panel;
    
    private final AppSettingsState appSettings;
    private final ReadTipState readTipSetting;
    private final Map<String, DataSource> dataSourceMap;

    public ReadTipPanel(AppSettingsState appSettings, ReadTipState readTipSetting) {
        this.appSettings = appSettings;
        this.readTipSetting = readTipSetting;
        // 确保数据源已加载
        readTipSetting.getState();
        // 从配置文件加载数据源
        this.dataSourceMap = readTipSetting.dataSource.stream()
            .collect(Collectors.toMap(DataSource::getTitle, v -> v));
        
        initializeComponents();
        setupEventListeners();
        loadData();
    }

    private void initializeComponents() {
        // 组件已经在 .form 文件中初始化，这里确保 panel 不为 null
        if (panel == null) {
            panel = new JPanel();
        }
    }

    private void setupEventListeners() {
        // 数据源选择事件
        comboDataSource.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                String selected = (String) comboDataSource.getSelectedItem();
                if (selected != null && dataSourceMap.containsKey(selected)) {
                    DataSource selectedDataSource = dataSourceMap.get(selected);
                    if (selectedDataSource != null) {
                        updateFieldsFromDataSource(selectedDataSource);
                    }
                }
            });
        });
        
        // URL输入事件
        newUrl.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateLoadButtonState(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateLoadButtonState(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateLoadButtonState(); }
        });
        
        newTitle.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateLoadButtonState(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateLoadButtonState(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateLoadButtonState(); }
        });
        
        // 为newTitle添加提示信息
        newTitle.setToolTipText("为此数据源设置一个易于识别的名称");
        
        // 加载数据源按钮
        btnLoadDataSource.addActionListener(e -> loadNewDataSource());
        
        // 删除数据源按钮
        btnDeleteDataSource.addActionListener(e -> deleteSelectedDataSource());
        
        // 上一页测试按钮
        btnPrevTest.addActionListener(e -> testPreviousNavigation());
        
        // 下一页测试按钮
        btnNextTest.addActionListener(e -> testNextNavigation());
    }

    private void updateLoadButtonState() {
        boolean canLoad = StringUtil.isNotEmpty(newUrl.getText()) && 
                         StringUtil.isNotEmpty(newTitle.getText());
        btnLoadDataSource.setEnabled(canLoad);
    }

    private void loadNewDataSource() {
        try {
            DataSource dataSource = HandleUtils.loadNewDataSource(newUrl.getText());
            dataSource.setTitle(newTitle.getText());
            dataSourceMap.put(dataSource.getTitle(), dataSource);
            
            // 同步到配置文件
            syncDataSourceToConfig();
            
            // 更新下拉框
            updateDataSourceComboBox();
            comboDataSource.setSelectedItem(dataSource.getTitle());
            
            showMessage("数据源添加成功! 可在下拉框中切换使用", Color.GREEN);
            
            // 清空输入框
            newUrl.setText("");
            newTitle.setText("");
        } catch (Exception ex) {
            showMessage("数据源加载失败: " + ex.getMessage(), Color.RED);
        }
    }

    private void deleteSelectedDataSource() {
        String selected = (String) comboDataSource.getSelectedItem();
        if (selected != null && dataSourceMap.containsKey(selected)) {
            int result = JOptionPane.showConfirmDialog(panel, 
                "确定要删除数据源 '" + selected + "' 吗?", 
                "确认删除", 
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                dataSourceMap.remove(selected);
                updateDataSourceComboBox();
                showMessage("数据源已删除", Color.GREEN);
            }
        }
    }

    private void updateDataSourceComboBox() {
        comboDataSource.setModel(new DefaultComboBoxModel<>(dataSourceMap.keySet().toArray(new String[0])));
    }


    
    private void testPreviousNavigation() {
        try {
            // 保存当前配置到ReadTipState
            saveCurrentConfigToState();
            
            // 检查必要配置
            if (StringUtil.isEmpty(readTipSetting.baseURL) || StringUtil.isEmpty(readTipSetting.previousURL)) {
                showMessage("请先配置基础URL和上一页URL", Color.ORANGE);
                return;
            }
            
            // 测试上一页功能
            HandleUtils.loadPreviousList(readTipSetting);
            String content = HandleUtils.getTipContent();
            
            showMessage("上一页测试成功: " + (content.length() > 50 ? content.substring(0, 50) + "..." : content), Color.GREEN);
        } catch (Exception ex) {
            showMessage("上一页测试失败: " + ex.getMessage(), Color.RED);
        }
    }
    
    private void testNextNavigation() {
        try {
            // 保存当前配置到ReadTipState
            saveCurrentConfigToState();
            
            // 检查必要配置
            if (StringUtil.isEmpty(readTipSetting.baseURL) || StringUtil.isEmpty(readTipSetting.nextURL)) {
                showMessage("请先配置基础URL和下一页URL", Color.ORANGE);
                return;
            }
            
            // 测试下一页功能
            HandleUtils.loadNextList(readTipSetting);
            String content = HandleUtils.getTipContent();
            
            showMessage("下一页测试成功: " + (content.length() > 50 ? content.substring(0, 50) + "..." : content), Color.GREEN);
        } catch (Exception ex) {
            showMessage("下一页测试失败: " + ex.getMessage(), Color.RED);
        }
    }
    
    private void syncDataSourceToConfig() {
        // 将dataSourceMap同步到ReadTipState
        readTipSetting.dataSource.clear();
        readTipSetting.dataSource.addAll(dataSourceMap.values());
        
        // 触发配置保存
        readTipSetting.loadState(readTipSetting.getState());
    }
    
    private void saveCurrentConfigToState() {
        // 将当前界面配置保存到ReadTipState
        readTipSetting.baseURL = textBaseURL.getText();
        readTipSetting.thisURL = textThisURL.getText();
        readTipSetting.nextURL = textNextURL.getText();
        readTipSetting.previousURL = textPreviousURL.getText();
        readTipSetting.title = textTitle.getText();
    }
    
    private void showMessage(String message, Color color) {
        lblMessage.setText(message);
        lblMessage.setForeground(color);
    }

    private void loadData() {
        // 加载基本设置
        chkEnabled.setSelected(appSettings.readTipEnabled);
        
        // 加载当前配置
        textBaseURL.setText(readTipSetting.baseURL != null ? readTipSetting.baseURL : "");
        textThisURL.setText(readTipSetting.thisURL != null ? readTipSetting.thisURL : "");
        textNextURL.setText(readTipSetting.nextURL != null ? readTipSetting.nextURL : "");
        textPreviousURL.setText(readTipSetting.previousURL != null ? readTipSetting.previousURL : "");
        textTitle.setText(readTipSetting.title != null ? readTipSetting.title : "");
        
        // 加载数据源列表
        updateDataSourceComboBox();
        
        // 如果没有数据源，显示引导信息
        if (dataSourceMap.isEmpty()) {
            showMessage("暂无数据源，请添加新的数据源开始使用", Color.BLUE);
        } else {
            // 安全设置选中项
            if (comboDataSource.getItemCount() > 0) {
                comboDataSource.setSelectedIndex(0);
            }
        }
    }

    private void updateFieldsFromDataSource(DataSource dataSource) {
        textBaseURL.setText(dataSource.getBaseURL());
        textThisURL.setText(dataSource.getThisURL());
        textNextURL.setText(dataSource.getNextURL());
        textPreviousURL.setText(dataSource.getPreviousURL());
        textTitle.setText(dataSource.getTitle());
    }

    @Override
    public Component createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        // 安全的空值检查
        if (chkEnabled.isSelected() != appSettings.readTipEnabled) return true;
        
        // 检查ReadTipState配置是否修改（安全比较）
        if (!safeEquals(textBaseURL.getText(), readTipSetting.baseURL)) return true;
        if (!safeEquals(textThisURL.getText(), readTipSetting.thisURL)) return true;
        if (!safeEquals(textNextURL.getText(), readTipSetting.nextURL)) return true;
        if (!safeEquals(textPreviousURL.getText(), readTipSetting.previousURL)) return true;
        if (!safeEquals(textTitle.getText(), readTipSetting.title)) return true;
        
        return false;
    }
    
    // 安全的字符串比较方法
    private boolean safeEquals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }

    @Override
    public void reset() {
        loadData();
    }

    @Override
    public void apply() {
        // 应用基础设置
        appSettings.readTipEnabled = chkEnabled.isSelected();
        
        // 应用ReadTipState配置
        readTipSetting.baseURL = textBaseURL.getText();
        readTipSetting.thisURL = textThisURL.getText();
        readTipSetting.nextURL = textNextURL.getText();
        readTipSetting.previousURL = textPreviousURL.getText();
        readTipSetting.title = textTitle.getText();
        
        // 应用数据源更改
        readTipSetting.dataSource.clear();
        readTipSetting.dataSource.addAll(dataSourceMap.values());
        
        // 保存状态
        appSettings.loadState(appSettings.getState());
        readTipSetting.loadState(readTipSetting.getState());
        
        showMessage("配置已保存", Color.GREEN);
    }
}
