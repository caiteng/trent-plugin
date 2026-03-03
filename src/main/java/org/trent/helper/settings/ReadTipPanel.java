package org.trent.helper.settings;

import com.intellij.openapi.util.text.StringUtil;
import org.trent.helper.readtip.DataSource;
import org.trent.helper.readtip.HandleUtils;
import org.trent.helper.readtip.ReadTipState;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReadTipPanel implements Setting {
    // 功能启用开关
    private JCheckBox chkEnabled;
    
    // 数据源管理组件
    private JComboBox<String> comboDataSource;
    private JButton btnNewDataSource;
    private JButton btnDeleteDataSource;
    private JButton btnApplyDataSource; // 应用当前选中的数据源
    private JLabel lblCurrentDataSource; // 显示当前正在使用的数据源
    
    // 数据源编辑组件
    private JTextField textTitle;
    private JTextField textBaseURL;
    private JTextField textThisURL;
    private JTextField textNextURL;
    private JTextField textPreviousURL;
    
    // 消息提示和测试按钮
    private JLabel lblMessage;
    private JButton btnPrevTest;
    private JButton btnNextTest;
    
    // 主面板
    private JPanel panel;
    
    private final AppSettingsState appSettings;
    private final ReadTipState readTipSetting;
    private List<DataSource> dataSourceList;
    private String currentEditingTitle; // 当前正在编辑的数据源标题
    
    private boolean isUpdatingUI = false; // 防止 UI 更新时触发事件循环

    private static final String DEFAULT_DATASOURCE_TITLE = "示例数据源（不可删除）";
    
    public ReadTipPanel(AppSettingsState appSettings, ReadTipState readTipSetting) {
        this.appSettings = appSettings;
        this.readTipSetting = readTipSetting;
        
        // 初始化数据源列表
        this.dataSourceList = new ArrayList<>(readTipSetting.dataSource);
        
        // 确保至少有一个默认数据源
        ensureDefaultDataSource();
        
        initializeComponents();
        setupEventListeners();
        loadData();
    }
    
    // 确保默认数据源存在
    private void ensureDefaultDataSource() {
        boolean hasDefault = dataSourceList.stream()
            .anyMatch(ds -> ds.getTitle().equals(DEFAULT_DATASOURCE_TITLE));
        
        if (!hasDefault) {
            DataSource defaultDataSource = new DataSource();
            defaultDataSource.setTitle(DEFAULT_DATASOURCE_TITLE);
            defaultDataSource.setBaseURL("https://www.84kanshu.com");
            defaultDataSource.setThisURL("");
            defaultDataSource.setNextURL("");
            defaultDataSource.setPreviousURL("");
            
            // 添加到列表开头
            dataSourceList.add(0, defaultDataSource);
        }
    }

    private void initializeComponents() {
        if (panel == null) {
            panel = new JPanel();
        }
    }

    private void setupEventListeners() {
        // 数据源选择事件 - 切换数据源时加载其所有属性
        comboDataSource.addActionListener(e -> {
            if (!isUpdatingUI) {
                String selected = (String) comboDataSource.getSelectedItem();
                if (selected != null) {
                    loadDataSourceToFields(selected);
                }
            }
        });
        
        // 应用数据源按钮 - 立即切换到选中的数据源
        btnApplyDataSource.addActionListener(e -> applySelectedDataSource());
        
        // 字段变更监听 - 实时更新当前编辑的数据源
        java.awt.event.FocusListener fieldUpdateListener = new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {}
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                updateCurrentDataSourceFromFields();
            }
        };
        
        textTitle.addFocusListener(fieldUpdateListener);
        textBaseURL.addFocusListener(fieldUpdateListener);
        textThisURL.addFocusListener(fieldUpdateListener);
        textNextURL.addFocusListener(fieldUpdateListener);
        textPreviousURL.addFocusListener(fieldUpdateListener);
        
        // 新建数据源按钮
        btnNewDataSource.addActionListener(e -> createNewDataSource());
        
        // 删除数据源按钮
        btnDeleteDataSource.addActionListener(e -> deleteSelectedDataSource());
        
        // 上一页测试按钮
        btnPrevTest.addActionListener(e -> testPreviousNavigation());
        
        // 下一页测试按钮
        btnNextTest.addActionListener(e -> testNextNavigation());
    }

    private void createNewDataSource() {
        try {
            // 弹出对话框输入新数据源的 URL 和标题
            JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField urlField = new JTextField();
            JTextField titleField = new JTextField();
            
            inputPanel.add(new JLabel("数据源 URL:"));
            inputPanel.add(urlField);
            inputPanel.add(new JLabel("数据源标题:"));
            inputPanel.add(titleField);
            
            int result = JOptionPane.showConfirmDialog(panel, inputPanel, 
                "新建数据源", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                String url = urlField.getText().trim();
                String title = titleField.getText().trim();
                
                if (StringUtil.isEmpty(url) || StringUtil.isEmpty(title)) {
                    showMessage("URL 和标题不能为空", Color.RED);
                    return;
                }
                
                // 检查是否已存在同名数据源
                if (dataSourceList.stream().anyMatch(ds -> ds.getTitle().equals(title))) {
                    showMessage("数据源标题已存在，请使用其他名称", Color.RED);
                    return;
                }
                
                // 尝试加载数据源，如果失败则创建空数据源让用户自己编辑
                DataSource dataSource;
                try {
                    dataSource = HandleUtils.loadNewDataSource(url);
                    dataSource.setTitle(title);
                    showMessage("数据源创建成功！现在可以编辑其属性", Color.GREEN);
                } catch (Exception e) {
                    // 如果连接失败，创建一个空的数据源，让用户自己填写
                    dataSource = new DataSource();
                    dataSource.setTitle(title);
                    dataSource.setBaseURL(extractBaseUrl(url));
                    dataSource.setThisURL(extractPath(url));
                    dataSource.setNextURL("");
                    dataSource.setPreviousURL("");
                    showMessage("数据源已创建（无法自动获取内容，请手动配置）", Color.ORANGE);
                }
                
                // 添加到列表
                dataSourceList.add(dataSource);
                
                // 更新下拉框并选中新添加的数据源
                updateDataSourceComboBox();
                comboDataSource.setSelectedItem(title);
            }
        } catch (Exception ex) {
            showMessage("数据源创建失败：" + ex.getMessage(), Color.RED);
        }
    }
    
    // 从完整 URL 中提取基础 URL
    private String extractBaseUrl(String fullUrl) {
        try {
            if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
                fullUrl = "https://" + fullUrl;
            }
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(https?://[^/]+)(/.*)?$");
            java.util.regex.Matcher matcher = pattern.matcher(fullUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return fullUrl;
    }
    
    // 从完整 URL 中提取路径部分
    private String extractPath(String fullUrl) {
        try {
            if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
                fullUrl = "https://" + fullUrl;
            }
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^https?://[^/]+(/.*)?$");
            java.util.regex.Matcher matcher = pattern.matcher(fullUrl);
            if (matcher.find()) {
                String path = matcher.group(1);
                return path != null ? path : "";
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "";
    }

    private void loadDataSourceToFields(String title) {
        DataSource dataSource = findDataSourceByTitle(title);
        if (dataSource != null) {
            isUpdatingUI = true;
            currentEditingTitle = title;
            
            textTitle.setText(dataSource.getTitle() != null ? dataSource.getTitle() : "");
            textBaseURL.setText(dataSource.getBaseURL() != null ? dataSource.getBaseURL() : "");
            textThisURL.setText(dataSource.getThisURL() != null ? dataSource.getThisURL() : "");
            textNextURL.setText(dataSource.getNextURL() != null ? dataSource.getNextURL() : "");
            textPreviousURL.setText(dataSource.getPreviousURL() != null ? dataSource.getPreviousURL() : "");
            
            isUpdatingUI = false;
        }
    }

    private void updateCurrentDataSourceFromFields() {
        String selectedTitle = (String) comboDataSource.getSelectedItem();
        if (selectedTitle == null || StringUtil.isEmpty(selectedTitle)) {
            return;
        }
        
        DataSource dataSource = findDataSourceByTitle(selectedTitle);
        if (dataSource != null) {
            // 如果标题被修改，需要更新数据源列表中的引用
            String newTitle = textTitle.getText().trim();
            if (!newTitle.equals(selectedTitle) && !StringUtil.isEmpty(newTitle)) {
                // 检查新标题是否与其他数据源冲突
                if (dataSourceList.stream()
                    .anyMatch(ds -> ds.getTitle().equals(newTitle) && !ds.equals(dataSource))) {
                    showMessage("数据源标题已存在，请使用其他名称", Color.ORANGE);
                    textTitle.setText(selectedTitle);
                    return;
                }
                
                // 更新标题
                dataSource.setTitle(newTitle);
                
                // 更新下拉框
                updateDataSourceComboBox();
                comboDataSource.setSelectedItem(newTitle);
                currentEditingTitle = newTitle;
            }
            
            // 更新其他属性
            dataSource.setBaseURL(textBaseURL.getText().trim());
            dataSource.setThisURL(textThisURL.getText().trim());
            dataSource.setNextURL(textNextURL.getText().trim());
            dataSource.setPreviousURL(textPreviousURL.getText().trim());
            
            showMessage("数据源已自动保存", Color.GREEN);
        }
    }

    private DataSource findDataSourceByTitle(String title) {
        for (DataSource ds : dataSourceList) {
            if (ds.getTitle().equals(title)) {
                return ds;
            }
        }
        return null;
    }

    private void deleteSelectedDataSource() {
        String selected = (String) comboDataSource.getSelectedItem();
        if (selected != null) {
            // 检查是否是默认数据源
            if (DEFAULT_DATASOURCE_TITLE.equals(selected)) {
                showMessage("示例数据源不允许删除", Color.RED);
                return;
            }
            
            int result = JOptionPane.showConfirmDialog(panel, 
                "确定要删除数据源 '" + selected + "' 吗？\n此操作不可恢复！", 
                "确认删除", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                DataSource toRemove = findDataSourceByTitle(selected);
                if (toRemove != null) {
                    dataSourceList.remove(toRemove);
                    updateDataSourceComboBox();
                    
                    // 如果还有数据源，选中第一个
                    if (!dataSourceList.isEmpty()) {
                        comboDataSource.setSelectedIndex(0);
                    } else {
                        clearFields();
                    }
                    
                    showMessage("数据源已删除", Color.GREEN);
                }
            }
        } else {
            showMessage("请先选择一个数据源", Color.ORANGE);
        }
    }

    private void updateDataSourceComboBox() {
        String currentSelection = (String) comboDataSource.getSelectedItem();
        
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (DataSource ds : dataSourceList) {
            model.addElement(ds.getTitle());
        }
        
        comboDataSource.setModel(model);
        
        // 尝试保持原来的选择
        if (currentSelection != null && dataSourceList.stream().anyMatch(ds -> ds.getTitle().equals(currentSelection))) {
            comboDataSource.setSelectedItem(currentSelection);
        } else if (dataSourceList.size() > 0) {
            // 优先选中默认数据源
            comboDataSource.setSelectedItem(DEFAULT_DATASOURCE_TITLE);
        }
    }

    private void clearFields() {
        textTitle.setText("");
        textBaseURL.setText("");
        textThisURL.setText("");
        textNextURL.setText("");
        textPreviousURL.setText("");
        currentEditingTitle = null;
    }

    private void testPreviousNavigation() {
        try {
            // 先保存当前编辑的内容
            updateCurrentDataSourceFromFields();
            
            // 检查必要配置
            if (StringUtil.isEmpty(textBaseURL.getText()) || StringUtil.isEmpty(textPreviousURL.getText())) {
                showMessage("请先配置基础 URL 和上一页 URL", Color.ORANGE);
                return;
            }
            
            // 创建临时状态用于测试，不影响当前配置
            ReadTipState tempState = new ReadTipState();
            tempState.baseURL = textBaseURL.getText();
            tempState.previousURL = textPreviousURL.getText();
            tempState.thisURL = textThisURL.getText();
            
            // 测试上一页功能
            HandleUtils.loadPreviousList(tempState);
            String content = HandleUtils.getTipContent();
            
            showMessage("上一页测试成功：" + (content.length() > 50 ? content.substring(0, 50) + "..." : content), Color.GREEN);
        } catch (Exception ex) {
            showMessage("上一页测试失败：" + ex.getMessage(), Color.RED);
            ex.printStackTrace();
        }
    }

    private void testNextNavigation() {
        try {
            // 先保存当前编辑的内容
            updateCurrentDataSourceFromFields();
            
            // 检查必要配置
            if (StringUtil.isEmpty(textBaseURL.getText()) || StringUtil.isEmpty(textNextURL.getText())) {
                showMessage("请先配置基础 URL 和下一页 URL", Color.ORANGE);
                return;
            }
            
            // 创建临时状态用于测试，不影响当前配置
            ReadTipState tempState = new ReadTipState();
            tempState.baseURL = textBaseURL.getText();
            tempState.nextURL = textNextURL.getText();
            tempState.thisURL = textThisURL.getText();
            
            // 测试下一页功能
            HandleUtils.loadNextList(tempState);
            String content = HandleUtils.getTipContent();
            
            showMessage("下一页测试成功：" + (content.length() > 50 ? content.substring(0, 50) + "..." : content), Color.GREEN);
        } catch (Exception ex) {
            showMessage("下一页测试失败：" + ex.getMessage(), Color.RED);
            ex.printStackTrace();
        }
    }

    private void showMessage(String message, Color color) {
        lblMessage.setText(message);
        lblMessage.setForeground(color);
        
        // 3 秒后清除消息
        Timer timer = new Timer(3000, e -> {
            if (lblMessage.getText().equals(message)) {
                lblMessage.setText("");
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void loadData() {
        // 加载基本设置
        chkEnabled.setSelected(appSettings.readTipEnabled);
            
        // 加载数据源列表
        updateDataSourceComboBox();
            
        // 默认选中当前正在使用的数据源
        if (!dataSourceList.isEmpty()) {
            // 找到当前正在使用的数据源
            String currentTitle = readTipSetting.title;
            if (currentTitle != null && dataSourceList.stream().anyMatch(ds -> ds.getTitle().equals(currentTitle))) {
                comboDataSource.setSelectedItem(currentTitle);
            } else {
                // 如果没有匹配的，选中默认数据源
                comboDataSource.setSelectedItem(DEFAULT_DATASOURCE_TITLE);
            }
        } else {
            clearFields();
            showMessage("暂无数据源", Color.BLUE);
        }
            
        // 更新当前使用的数据源提示
        updateCurrentDataSourceLabel();
    }
        
    // 更新当前使用的数据源标签
    private void updateCurrentDataSourceLabel() {
        if (readTipSetting.title != null) {
            lblCurrentDataSource.setText("<html>当前使用的数据源：<b>" + readTipSetting.title + "</b></html>");
        } else {
            lblCurrentDataSource.setText("<html>当前使用的数据源：<b>未设置</b></html>");
        }
    }
    
    private void applySelectedDataSource() {
        String selected = (String) comboDataSource.getSelectedItem();
        if (selected == null) {
            showMessage("请先选择一个数据源", Color.ORANGE);
            return;
        }
        
        DataSource selectedDataSource = findDataSourceByTitle(selected);
        if (selectedDataSource == null) {
            showMessage("选中的数据源不存在", Color.RED);
            return;
        }
        
        // 检查是否已经是当前使用的数据源
        if (readTipSetting.title != null && readTipSetting.title.equals(selectedDataSource.getTitle())) {
            showMessage("已在使用的数据源：" + selectedDataSource.getTitle(), Color.BLUE);
            return;
        }
        
        try {
            // 应用选中的数据源
            readTipSetting.changeSource(selectedDataSource);
            readTipSetting.textList.clear();
            readTipSetting.index = 0;
            
            // 保存状态
            readTipSetting.loadState(readTipSetting.getState());
            
            // 更新提示
            updateCurrentDataSourceLabel();
            
            showMessage("已切换到数据源：" + selectedDataSource.getTitle(), Color.GREEN);
        } catch (Exception e) {
            showMessage("切换数据源失败：" + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }

    @Override
    public Component createComponent() {
        return panel;
    }

    @Override
    public boolean isModified() {
        // 检查启用状态是否修改
        if (chkEnabled.isSelected() != appSettings.readTipEnabled) return true;
        
        // 检查数据源列表是否有变化
        if (dataSourceList.size() != readTipSetting.dataSource.size()) return true;
        
        // 逐个比较数据源
        for (DataSource newDs : dataSourceList) {
            boolean found = false;
            for (DataSource oldDs : readTipSetting.dataSource) {
                if (newDs.getTitle().equals(oldDs.getTitle())) {
                    found = true;
                    // 比较各个字段
                    if (!safeEquals(newDs.getBaseURL(), oldDs.getBaseURL()) ||
                        !safeEquals(newDs.getThisURL(), oldDs.getThisURL()) ||
                        !safeEquals(newDs.getNextURL(), oldDs.getNextURL()) ||
                        !safeEquals(newDs.getPreviousURL(), oldDs.getPreviousURL()) ||
                        !safeEquals(newDs.getTitle(), oldDs.getTitle())) {
                        return true;
                    }
                    break;
                }
            }
            if (!found) return true; // 有新的数据源
        }
        
        return false;
    }

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
        try {
            // 确保当前编辑的内容已保存
            updateCurrentDataSourceFromFields();
            
            // 应用基础设置
            appSettings.readTipEnabled = chkEnabled.isSelected();
            
            // 应用数据源更改
            readTipSetting.dataSource.clear();
            readTipSetting.dataSource.addAll(dataSourceList);
            
            // 确保序列化数据同步
            readTipSetting.dataSourceList = readTipSetting.serializeDataSource(readTipSetting.dataSource);
            
            // 保存状态到持久化存储
            appSettings.loadState(appSettings.getState());
            readTipSetting.loadState(readTipSetting.getState());
            
            showMessage("配置已保存并生效", Color.GREEN);
        } catch (Exception e) {
            showMessage("保存配置失败：" + e.getMessage(), Color.RED);
            e.printStackTrace();
        }
    }
}
