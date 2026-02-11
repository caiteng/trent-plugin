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
    
    // 主面板和布局
    private JPanel panel;
    private JPanel mainPanel;
    
    private final AppSettingsState appSettings;
    private final ReadTipState readTipSetting;
    private final Map<String, DataSource> dataSourceMap;

    public ReadTipPanel(AppSettingsState appSettings, ReadTipState readTipSetting) {
        this.appSettings = appSettings;
        this.readTipSetting = readTipSetting;
        this.dataSourceMap = readTipSetting.dataSource.stream().collect(Collectors.toMap(DataSource::getTitle, v -> v));
        
        initializeComponents();
        setupEventListeners();
        loadData();
    }

    private void initializeComponents() {
        // 初始化面板
        panel = new JPanel(new BorderLayout());
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // 添加标题
        JLabel titleLabel = new JLabel("阅读提示设置");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 启用设置
        JPanel enablePanel = createEnablePanel();
        mainPanel.add(enablePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 当前配置设置
        JPanel currentConfigPanel = createCurrentConfigPanel();
        mainPanel.add(currentConfigPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 数据源管理
        JPanel dataSourcePanel = createDataSourcePanel();
        mainPanel.add(dataSourcePanel);
        
        // 包装到滚动面板
        JBScrollPane scrollPane = new JBScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createEnablePanel() {
        JPanel enablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        enablePanel.setBorder(BorderFactory.createTitledBorder("基础设置"));
        
        chkEnabled = new JCheckBox("启用阅读提示功能");
        enablePanel.add(chkEnabled);
        
        return enablePanel;
    }

    private JPanel createCurrentConfigPanel() {
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setBorder(BorderFactory.createTitledBorder("当前配置"));
        
        // 基础URL
        JPanel baseUrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        baseUrlPanel.add(new JLabel("基础URL:"));
        textBaseURL = new JTextField(30);
        baseUrlPanel.add(textBaseURL);
        configPanel.add(baseUrlPanel);
        
        // 当前页面URL
        JPanel thisUrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thisUrlPanel.add(new JLabel("当前页面:"));
        textThisURL = new JTextField(30);
        thisUrlPanel.add(textThisURL);
        configPanel.add(thisUrlPanel);
        
        // 下一页URL
        JPanel nextUrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nextUrlPanel.add(new JLabel("下一页:"));
        textNextURL = new JTextField(30);
        nextUrlPanel.add(textNextURL);
        configPanel.add(nextUrlPanel);
        
        // 上一页URL
        JPanel prevUrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        prevUrlPanel.add(new JLabel("上一页:"));
        textPreviousURL = new JTextField(30);
        prevUrlPanel.add(textPreviousURL);
        configPanel.add(prevUrlPanel);
        
        // 标题
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.add(new JLabel("标题:"));
        textTitle = new JTextField(30);
        titlePanel.add(textTitle);
        configPanel.add(titlePanel);
        
        return configPanel;
    }

    private JPanel createDataSourcePanel() {
        JPanel dataSourcePanel = new JPanel();
        dataSourcePanel.setLayout(new BoxLayout(dataSourcePanel, BoxLayout.Y_AXIS));
        dataSourcePanel.setBorder(BorderFactory.createTitledBorder("数据源管理"));
        
        // 选择现有数据源
        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectPanel.add(new JLabel("选择数据源:"));
        comboDataSource = new JComboBox<>();
        comboDataSource.setPreferredSize(new Dimension(200, 25));
        selectPanel.add(comboDataSource);
        dataSourcePanel.add(selectPanel);
        
        // 新增数据源
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.add(new JLabel("新URL:"));
        newUrl = new JTextField(20);
        addPanel.add(newUrl);
        
        addPanel.add(new JLabel("标题:"));
        newTitle = new JTextField(15);
        addPanel.add(newTitle);
        
        btnLoadDataSource = new JButton("加载");
        addPanel.add(btnLoadDataSource);
        
        btnDeleteDataSource = new JButton("删除");
        addPanel.add(btnDeleteDataSource);
        
        dataSourcePanel.add(addPanel);
        
        // 消息显示
        lblMessage = new JLabel();
        lblMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        dataSourcePanel.add(lblMessage);
        
        return dataSourcePanel;
    }

    private void setupEventListeners() {
        // 数据源选择事件
        comboDataSource.addActionListener(e -> {
            String selected = (String) comboDataSource.getSelectedItem();
            if (selected != null && dataSourceMap.containsKey(selected)) {
                DataSource selectedDataSource = dataSourceMap.get(selected);
                updateFieldsFromDataSource(selectedDataSource);
            }
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
        
        // 加载数据源按钮
        btnLoadDataSource.addActionListener(e -> loadNewDataSource());
        
        // 删除数据源按钮
        btnDeleteDataSource.addActionListener(e -> deleteSelectedDataSource());
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
            
            // 更新下拉框
            updateDataSourceComboBox();
            comboDataSource.setSelectedItem(dataSource.getTitle());
            
            showMessage("数据源加载成功!", Color.GREEN);
            
            // 清空输入框
            newUrl.setText("");
            newTitle.setText("");
        } catch (Exception ex) {
            showMessage("加载失败: " + ex.getMessage(), Color.RED);
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

    private void showMessage(String message, Color color) {
        lblMessage.setText(message);
        lblMessage.setForeground(color);
    }

    private void loadData() {
        // 加载基本设置
        chkEnabled.setSelected(appSettings.readTipEnabled);
        
        // 加载当前配置
        textBaseURL.setText(readTipSetting.baseURL);
        textThisURL.setText(readTipSetting.thisURL);
        textNextURL.setText(readTipSetting.nextURL);
        textPreviousURL.setText(readTipSetting.previousURL);
        textTitle.setText(readTipSetting.title);
        
        // 加载数据源列表
        updateDataSourceComboBox();
        if (!dataSourceMap.isEmpty()) {
            comboDataSource.setSelectedIndex(0);
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
        // 检查基础设置是否修改
        if (chkEnabled.isSelected() != appSettings.readTipEnabled) return true;
        
        // 检查ReadTipState配置是否修改
        if (!textBaseURL.getText().equals(readTipSetting.baseURL)) return true;
        if (!textThisURL.getText().equals(readTipSetting.thisURL)) return true;
        if (!textNextURL.getText().equals(readTipSetting.nextURL)) return true;
        if (!textPreviousURL.getText().equals(readTipSetting.previousURL)) return true;
        if (!textTitle.getText().equals(readTipSetting.title)) return true;
        
        return false;
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
    }
}
