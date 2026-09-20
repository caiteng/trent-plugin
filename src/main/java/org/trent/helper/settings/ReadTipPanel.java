package org.trent.helper.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.trent.helper.db.DatabaseManager;
import org.trent.helper.db.DataSourceDao;
import org.trent.helper.readtip.common.DataSource;
import org.trent.helper.readtip.common.ReadTipState;
import org.trent.helper.readtip.local.LocalSourceHandler;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class ReadTipPanel implements Setting {
    private static final Logger LOG = Logger.getInstance(ReadTipPanel.class);
    private JCheckBox chkEnabled;
    private JComboBox<String> comboDataSource;
    private JButton btnApplyDataSource;
    private JLabel lblCurrentDataSource;
    private JLabel lblMessage;
    private JPanel panel;
    private JTextField textDatabasePath;
    private JButton btnBrowseDb;
    private JButton btnSaveDb;
    private JLabel lblDbStatus;
    private JButton btnNewDataSource;
    private JButton btnEditDataSource;
    private JButton btnDeleteDataSource;

    private final AppSettingsState appSettings;
    private final ReadTipState readTipSetting;
    private final List<DataSource> dataSourceList;
    private String lastMessageToken = "";

    private static final String DEFAULT_DATASOURCE_TITLE = "示例数据源（不可删除）";

    public ReadTipPanel(AppSettingsState appSettings, ReadTipState readTipSetting) {
        this.appSettings = appSettings;
        this.readTipSetting = readTipSetting;
        this.dataSourceList = new ArrayList<>();

        ensureDefaultDataSource();
        try {
            String dbPath = appSettings.databasePath;
            if (dbPath != null && !dbPath.isEmpty()) {
                DatabaseManager.getInstance().initDatabase(dbPath);
            } else {
                DatabaseManager.getInstance().getConnection();
            }
        } catch (Exception e) {
            DatabaseManager.getInstance().getConnection();
        }
        initializeComponents();
        loadData();
    }

    private void ensureDefaultDataSource() {
        boolean hasDefault = readTipSetting.dataSource.stream()
                .anyMatch(ds -> DEFAULT_DATASOURCE_TITLE.equals(ds.getTitle()));
        if (!hasDefault) {
            DataSource defaultDataSource = new DataSource();
            defaultDataSource.setTitle(DEFAULT_DATASOURCE_TITLE);
            defaultDataSource.setBaseURL("https://www.84kanshu.com");
            defaultDataSource.setThisURL("");
            defaultDataSource.setNextURL("");
            defaultDataSource.setPreviousURL("");
            readTipSetting.dataSource.add(0, defaultDataSource);
        }
    }

    // ==================== 主面板 ====================

    private void initializeComponents() {
        panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));

        // ── 启用 ──
        chkEnabled = new JCheckBox("启用阅读提示功能");
        chkEnabled.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(chkEnabled);

        // ── 数据库设置 ──
        panel.add(sectionLabel("数据库设置"));
        JPanel dbPanel = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        textDatabasePath = new JTextField();
        btnBrowseDb = new JButton("浏览...");
        btnSaveDb = new JButton("保存");

        gbc.gridx = 0; gbc.weightx = 1.0;
        dbPanel.add(textDatabasePath, gbc);
        gbc.gridx = 1; gbc.weightx = 0;
        dbPanel.add(btnBrowseDb, gbc);
        gbc.gridx = 2;
        dbPanel.add(btnSaveDb, gbc);
        dbPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(dbPanel);

        lblDbStatus = new JLabel(" ");
        lblDbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblDbStatus);

        panel.add(new javax.swing.JSeparator());

        // ── 数据源管理 ──
        panel.add(sectionLabel("数据源管理"));

        comboDataSource = new JComboBox<>();
        btnApplyDataSource = new JButton("设为当前");
        JPanel switchPanel = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints sgbc = new java.awt.GridBagConstraints();
        sgbc.insets = new Insets(2, 2, 2, 2);
        sgbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        sgbc.gridx = 0; sgbc.weightx = 1.0;
        switchPanel.add(comboDataSource, sgbc);
        sgbc.gridx = 1; sgbc.weightx = 0;
        switchPanel.add(btnApplyDataSource, sgbc);
        switchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(switchPanel);

        lblCurrentDataSource = new JLabel("当前使用的数据源：未设置");
        lblCurrentDataSource.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblCurrentDataSource);

        btnNewDataSource = new JButton("新建");
        btnEditDataSource = new JButton("编辑");
        btnDeleteDataSource = new JButton("删除");
        JPanel crudPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        crudPanel.add(btnNewDataSource);
        crudPanel.add(btnEditDataSource);
        crudPanel.add(btnDeleteDataSource);
        crudPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(crudPanel);

        panel.add(new javax.swing.JSeparator());

        lblMessage = new JLabel(" ");
        lblMessage.setVerticalAlignment(SwingConstants.TOP);
        lblMessage.setPreferredSize(new Dimension(-1, 36));
        lblMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblMessage);

        panel.add(new JPanel()); // spacer

        setupEventListeners();
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void setupEventListeners() {
        btnBrowseDb.addActionListener(e -> browseDatabaseFile());
        btnSaveDb.addActionListener(e -> saveDatabaseConfig());
        btnApplyDataSource.addActionListener(e -> applySelectedDataSource());
        btnNewDataSource.addActionListener(e -> createNewDataSource());
        btnEditDataSource.addActionListener(e -> editSelectedDataSource());
        btnDeleteDataSource.addActionListener(e -> deleteSelectedDataSource());
    }

    // ==================== 数据加载 ====================

    private void loadData() {
        dataSourceList.clear();
        String dbPath = appSettings.databasePath;
        textDatabasePath.setText(defaultString(dbPath));
        chkEnabled.setSelected(appSettings.readTipEnabled);

        List<DataSource> dbSources = DataSourceDao.getAllDataSources();
        LOG.info("loadData: dbPath=" + dbPath + ", dbSources.size=" + dbSources.size()
                + ", memSources=" + readTipSetting.dataSource.size()
                + ", connPath=" + DatabaseManager.getInstance().getDatabasePath());
        if (!dbSources.isEmpty()) {
            dataSourceList.addAll(dbSources);
        } else {
            for (DataSource ds : readTipSetting.dataSource) {
                dataSourceList.add(copyDataSource(ds));
            }
            ensureDefaultDataSource();
        }

        updateDataSourceComboBox();
        updateDbStatus();

        if (!dataSourceList.isEmpty()) {
            String currentTitle = readTipSetting.title;
            if (currentTitle != null && dataSourceList.stream().anyMatch(ds -> currentTitle.equals(ds.getTitle()))) {
                comboDataSource.setSelectedItem(currentTitle);
            } else if (dataSourceList.stream().anyMatch(ds -> DEFAULT_DATASOURCE_TITLE.equals(ds.getTitle()))) {
                comboDataSource.setSelectedItem(DEFAULT_DATASOURCE_TITLE);
            } else {
                comboDataSource.setSelectedIndex(0);
            }
        } else {
            showMessage("暂无数据源，请点击「新建」创建", Color.BLUE);
        }

        updateCurrentDataSourceLabel();
        updateUiState();
    }

    private void reloadFromDatabase() {
        dataSourceList.clear();
        List<DataSource> dbSources = DataSourceDao.getAllDataSources();
        dataSourceList.addAll(dbSources);
        updateDataSourceComboBox();
    }

    // ==================== 数据库配置 ====================

    private void saveDatabaseConfig() {
        String path = textDatabasePath.getText().trim();
        if (path.isEmpty()) {
            showMessage("请选择数据库文件路径", Color.ORANGE);
            return;
        }
        String oldPath = appSettings.databasePath == null ? "" : appSettings.databasePath;
        appSettings.databasePath = path;
        if (!path.equals(oldPath)) {
            DatabaseManager.getInstance().reconnect(path);
        }
        // 检测表结构是否一致
        if (!DatabaseManager.getInstance().checkSchema()) {
            int result = JOptionPane.showConfirmDialog(panel,
                    "检测到数据库表结构与当前版本不一致。\n是否删除所有表并按最新版本重建？\n\n注意：此操作将清除所有数据源、文本块和阅读进度。",
                    "数据库结构不一致", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                DatabaseManager.getInstance().rebuildSchema();
                readTipSetting.dataSource.clear();
                showMessage("数据库表已重建，请重新创建数据源", Color.GREEN);
                loadData();
                return;
            }
        }
        updateDbStatus();
        loadData();
        showMessage("数据库配置已保存", Color.GREEN);
    }

    private void updateDbStatus() {
        String path = textDatabasePath.getText().trim();
        if (path.isEmpty()) {
            lblDbStatus.setText("<html><font color='orange'>数据库未配置</font></html>");
        } else {
            lblDbStatus.setText("<html><font color='green'>✓ 数据库已配置</font></html>");
        }
    }

    private void browseDatabaseFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQLite 数据库", "db"));
        if (fc.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".db")) path += ".db";
            textDatabasePath.setText(path);
        }
    }

    // ==================== 数据源切换 ====================

    private void applySelectedDataSource() {
        String selected = (String) comboDataSource.getSelectedItem();
        if (selected == null) {
            showMessage("请先选择一个数据源", Color.ORANGE);
            return;
        }
        DataSource ds = findDataSourceByTitle(selected);
        if (ds == null) {
            showMessage("选中的数据源不存在", Color.RED);
            return;
        }
        if (selected.equals(readTipSetting.title)) {
            showMessage("当前已在使用该数据源", Color.BLUE);
            return;
        }
        // 确保数据源已入库
        if (DataSourceDao.getSourceIdByTitle(selected) <= 0) {
            DataSourceDao.insertDataSource(ds);
        }
        DataSourceDao.setActiveSource(selected);
        readTipSetting.changeSource(copyDataSource(ds));
        readTipSetting.textList.clear();
        readTipSetting.index = 0;
        updateCurrentDataSourceLabel();
        showMessage("已切换到：" + ds.getTitle(), Color.GREEN);
        updateUiState();
    }

    // ==================== 数据源 CRUD ====================

    private void createNewDataSource() {
        DataSource result = showDataSourceDialog(null);
        if (result == null) return;
        reloadFromDatabase();
        comboDataSource.setSelectedItem(result.getTitle());
        showMessage("数据源已创建：" + result.getTitle(), Color.GREEN);
    }

    private void editSelectedDataSource() {
        String selected = (String) comboDataSource.getSelectedItem();
        if (selected == null) {
            showMessage("请先选择一个数据源", Color.ORANGE);
            return;
        }
        DataSource ds = findDataSourceByTitle(selected);
        if (ds == null) return;
        DataSource result = showDataSourceDialog(ds);
        if (result == null) return;
        reloadFromDatabase();
        comboDataSource.setSelectedItem(result.getTitle());
        showMessage("数据源已更新：" + result.getTitle(), Color.GREEN);
    }

    private void deleteSelectedDataSource() {
        String selected = (String) comboDataSource.getSelectedItem();
        if (selected == null) {
            showMessage("请先选择一个数据源", Color.ORANGE);
            return;
        }
        if (DEFAULT_DATASOURCE_TITLE.equals(selected)) {
            showMessage("示例数据源不允许删除", Color.RED);
            return;
        }
        int r = JOptionPane.showConfirmDialog(panel,
                "确定要删除数据源 '" + selected + "' 吗？\n此操作将同时删除相关的文本块和阅读进度，不可恢复。",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        DataSourceDao.deleteDataSource(selected);
        reloadFromDatabase();
        showMessage("数据源已删除：" + selected, Color.GREEN);
    }

    // ==================== 数据源编辑弹窗 ====================

    private DataSource showDataSourceDialog(DataSource existing) {
        boolean isEdit = existing != null;
        DataSource ds = isEdit ? copyDataSource(existing) : new DataSource();

        JTextField titleField = new JTextField(isEdit ? defaultString(ds.getTitle()) : "", 24);
        JRadioButton radioRemote = new JRadioButton("远程书源（URL）");
        JRadioButton radioLocal = new JRadioButton("本地书源（TXT 文件）");
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(radioRemote);
        typeGroup.add(radioLocal);

        JTextField textBaseURL = new JTextField(isEdit ? defaultString(ds.getBaseURL()) : "", 24);
        JTextField textThisURL = new JTextField(isEdit ? defaultString(ds.getThisURL()) : "", 24);
        JTextField textNextURL = new JTextField(isEdit ? defaultString(ds.getNextURL()) : "", 24);
        JTextField textPreviousURL = new JTextField(isEdit ? defaultString(ds.getPreviousURL()) : "", 24);
        JTextField textLocalPath = new JTextField(isEdit ? defaultString(ds.getLocalPath()) : "", 20);
        JButton btnBrowse = new JButton("浏览...");
        btnBrowse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("TXT 文件", "txt"));
            if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                textLocalPath.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        boolean initialIsLocal = isEdit && "local".equals(ds.getType());
        radioRemote.setSelected(!initialIsLocal);
        radioLocal.setSelected(initialIsLocal);

        // ── 远程源面板 ──
        JPanel remotePanel = new JPanel(new java.awt.GridBagLayout());
        remotePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("远程源 URL 配置"));
        java.awt.GridBagConstraints rgbc = gbc();
        rgbc.gridx = 0; rgbc.anchor = java.awt.GridBagConstraints.WEST;
        rgbc.insets = new Insets(2, 4, 2, 4);
        rgbc.fill = java.awt.GridBagConstraints.NONE; rgbc.weightx = 0;
        remotePanel.add(new JLabel("基础 URL:"), rgbc);
        rgbc.gridx = 1; rgbc.fill = java.awt.GridBagConstraints.HORIZONTAL; rgbc.weightx = 1.0;
        remotePanel.add(textBaseURL, rgbc);
        rgbc.gridx = 0; rgbc.fill = java.awt.GridBagConstraints.NONE; rgbc.weightx = 0;
        remotePanel.add(new JLabel("当前页 URL:"), rgbc);
        rgbc.gridx = 1; rgbc.fill = java.awt.GridBagConstraints.HORIZONTAL; rgbc.weightx = 1.0;
        remotePanel.add(textThisURL, rgbc);
        rgbc.gridx = 0; rgbc.fill = java.awt.GridBagConstraints.NONE; rgbc.weightx = 0;
        remotePanel.add(new JLabel("下一页 URL:"), rgbc);
        rgbc.gridx = 1; rgbc.fill = java.awt.GridBagConstraints.HORIZONTAL; rgbc.weightx = 1.0;
        remotePanel.add(textNextURL, rgbc);
        rgbc.gridx = 0; rgbc.fill = java.awt.GridBagConstraints.NONE; rgbc.weightx = 0;
        remotePanel.add(new JLabel("上一页 URL:"), rgbc);
        rgbc.gridx = 1; rgbc.fill = java.awt.GridBagConstraints.HORIZONTAL; rgbc.weightx = 1.0;
        remotePanel.add(textPreviousURL, rgbc);

        // ── 本地源面板 ──
        JPanel localPanel = new JPanel(new java.awt.GridBagLayout());
        localPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("本地源文件配置"));
        java.awt.GridBagConstraints lgbc = gbc();
        lgbc.gridx = 0; lgbc.anchor = java.awt.GridBagConstraints.WEST;
        lgbc.insets = new Insets(2, 4, 2, 4);
        lgbc.fill = java.awt.GridBagConstraints.NONE; lgbc.weightx = 0;
        localPanel.add(new JLabel("文件路径:"), lgbc);
        lgbc.gridx = 1; lgbc.fill = java.awt.GridBagConstraints.HORIZONTAL; lgbc.weightx = 1.0;
        localPanel.add(textLocalPath, lgbc);
        lgbc.gridx = 2; lgbc.fill = java.awt.GridBagConstraints.NONE; lgbc.weightx = 0;
        localPanel.add(btnBrowse, lgbc);

        remotePanel.setVisible(!initialIsLocal);
        localPanel.setVisible(initialIsLocal);

        // ── 主面板（GridBagLayout 表单式排列）──
        JPanel dialogPanel = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints dgbc = gbc();
        dgbc.insets = new Insets(4, 4, 4, 4);

        radioRemote.addActionListener(e -> { remotePanel.setVisible(true); localPanel.setVisible(false); revalidateDialog(dialogPanel); });
        radioLocal.addActionListener(e -> { remotePanel.setVisible(false); localPanel.setVisible(true); revalidateDialog(dialogPanel); });

        // ── 类型选择行 ──
        JPanel typePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        typePanel.add(radioRemote);
        typePanel.add(radioLocal);

        // 标题行
        dgbc.gridx = 0; dgbc.anchor = java.awt.GridBagConstraints.WEST;
        dgbc.fill = java.awt.GridBagConstraints.NONE; dgbc.weightx = 0;
        dialogPanel.add(new JLabel("标题:"), dgbc);
        dgbc.gridx = 1; dgbc.fill = java.awt.GridBagConstraints.HORIZONTAL; dgbc.weightx = 1.0;
        dialogPanel.add(titleField, dgbc);

        // 类型行
        dgbc.gridx = 0; dgbc.gridwidth = 2; dgbc.fill = java.awt.GridBagConstraints.HORIZONTAL; dgbc.weightx = 1.0;
        dialogPanel.add(typePanel, dgbc);
        dgbc.gridwidth = 1;

        // 远程/本地配置区
        dgbc.gridx = 0; dgbc.gridwidth = 2; dgbc.fill = java.awt.GridBagConstraints.HORIZONTAL; dgbc.weightx = 1.0;
        dialogPanel.add(remotePanel, dgbc);
        dialogPanel.add(localPanel, dgbc);
        dgbc.gridwidth = 1;

        int result = JOptionPane.showConfirmDialog(panel, dialogPanel,
                isEdit ? "编辑数据源" : "新建数据源",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return null;

        String title = titleField.getText().trim();
        if (StringUtil.isEmpty(title)) {
            showMessage("标题不能为空", Color.ORANGE);
            return null;
        }
        if (dataSourceList.stream().anyMatch(d -> title.equals(d.getTitle()) && d != existing)) {
            showMessage("数据源标题已存在", Color.ORANGE);
            return null;
        }

        ds.setTitle(title);
        boolean isLocal = radioLocal.isSelected();
        ds.setType(isLocal ? "local" : "remote");
        if (isLocal) {
            ds.setLocalPath(textLocalPath.getText().trim());
            ds.setBaseURL(textLocalPath.getText().trim());
            ds.setThisURL(""); ds.setNextURL(""); ds.setPreviousURL("");
        } else {
            ds.setLocalPath(null);
            ds.setBaseURL(textBaseURL.getText().trim());
            ds.setThisURL(textThisURL.getText().trim());
            ds.setNextURL(textNextURL.getText().trim());
            ds.setPreviousURL(textPreviousURL.getText().trim());
        }

        // 保存到数据库
        if (isEdit) {
            DataSourceDao.updateDataSource(ds);
        } else {
            long id = DataSourceDao.insertDataSource(ds);
            if (id <= 0) {
                showMessage("数据源保存到数据库失败，请检查 IDE 日志", Color.RED);
                return null;
            }
        }

        // 本地源：加载内容到数据库
        if (isLocal) {
            String filePath = ds.getLocalPath();
            if (!StringUtil.isEmpty(filePath)) {
                int sourceId = DataSourceDao.getSourceIdByTitle(title);
                if (sourceId > 0) {
                    loadContentToDb(sourceId, filePath, title);
                }
            }
        }
        return ds;
    }

    private void loadContentToDb(int sourceId, String filePath, String title) {
        ReadTipState tempState = new ReadTipState();
        tempState.baseURL = filePath;
        tempState.title = title;
        tempState.sourceId = sourceId;
        ProgressManager.getInstance().run(new Task.Backgroundable(null, "加载数据源", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("正在导入文本内容到数据库...");
                LocalSourceHandler.importChunksFromTxt(tempState, lines -> {
                    if (lines % 200 == 0) indicator.setFraction(-1);
                });
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (tempState.totalChunks > 0) {
                        showMessage("已加载 " + tempState.totalChunks + " 个文本块到数据库", Color.GREEN);
                    } else {
                        showMessage("文件为空或无法读取", Color.ORANGE);
                    }
                });
            }
        });
    }

    // ==================== 工具方法 ====================

    private String defaultString(String text) { return text == null ? "" : text; }

    private static java.awt.GridBagConstraints gbc() {
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        return c;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String abbreviate(String text) {
        if (text == null) return "";
        return text.length() > 30 ? text.substring(0, 30) + "..." : text;
    }

    private DataSource findDataSourceByTitle(String title) {
        for (DataSource ds : dataSourceList) {
            if (title.equals(ds.getTitle())) return ds;
        }
        return null;
    }

    private DataSource copyDataSource(DataSource source) {
        DataSource copy = new DataSource();
        copy.setBaseURL(source.getBaseURL());
        copy.setThisURL(source.getThisURL());
        copy.setNextURL(source.getNextURL());
        copy.setPreviousURL(source.getPreviousURL());
        copy.setTitle(source.getTitle());
        copy.setTextList(new ArrayList<>(source.getTextList()));
        copy.setIndex(source.getIndex());
        copy.setType(source.getType());
        copy.setLocalPath(source.getLocalPath());
        copy.setLocalChapterIndex(source.getLocalChapterIndex());
        copy.setLocalChunkIndex(source.getLocalChunkIndex());
        copy.setLocalCurrentLineNumber(source.getLocalCurrentLineNumber());
        return copy;
    }

    private void updateDataSourceComboBox() {
        String currentSelection = (String) comboDataSource.getSelectedItem();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (DataSource ds : dataSourceList) model.addElement(ds.getTitle());
        comboDataSource.setModel(model);
        if (currentSelection != null && dataSourceList.stream().anyMatch(ds -> currentSelection.equals(ds.getTitle()))) {
            comboDataSource.setSelectedItem(currentSelection);
        } else if (!dataSourceList.isEmpty()) {
            comboDataSource.setSelectedItem(DEFAULT_DATASOURCE_TITLE);
        }
    }

    private void updateCurrentDataSourceLabel() {
        if (readTipSetting.title != null) {
            lblCurrentDataSource.setText("<html>当前使用的数据源：<b>" + readTipSetting.title + "</b></html>");
        } else {
            lblCurrentDataSource.setText("<html>当前使用的数据源：<b>未设置</b></html>");
        }
    }

    private void showMessage(String message, Color color) {
        String safeMessage = message == null ? "" : message.trim();
        String token = safeMessage + "|" + color.getRGB() + "|" + System.nanoTime();
        lastMessageToken = token;
        if (safeMessage.isEmpty()) {
            lblMessage.setText(" ");
        } else {
            lblMessage.setText("<html>" + escapeHtml(safeMessage).replace("\n", "<br/>") + "</html>");
        }
        lblMessage.setForeground(color);
        Timer timer = new Timer(3000, e -> {
            if (token.equals(lastMessageToken)) lblMessage.setText(" ");
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void updateUiState() {
        String selected = comboDataSource == null ? null : (String) comboDataSource.getSelectedItem();
        boolean hasSelection = !StringUtil.isEmpty(selected);
        boolean isDefault = DEFAULT_DATASOURCE_TITLE.equals(selected);
        boolean isCurrent = hasSelection && selected.equals(readTipSetting.title);

        btnApplyDataSource.setEnabled(hasSelection && !isCurrent);
        btnEditDataSource.setEnabled(hasSelection);
        btnDeleteDataSource.setEnabled(hasSelection && !isDefault);
        btnSaveDb.setEnabled(!textDatabasePath.getText().trim().isEmpty());

        if (isCurrent) {
            btnApplyDataSource.setToolTipText("该数据源已经在使用中");
        } else {
            btnApplyDataSource.setToolTipText("切换为当前使用的数据源");
        }
    }

    private void revalidateDialog(JPanel p) {
        p.revalidate();
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(p);
        if (w != null) { w.pack(); return; }
        if (panel != null) { panel.revalidate(); panel.repaint(); }
    }

    // ==================== Setting 接口 ====================

    @Override
    public Component createComponent() { return panel; }

    @Override
    public boolean isModified() {
        if (chkEnabled.isSelected() != appSettings.readTipEnabled) return true;
        String newPath = textDatabasePath.getText().trim();
        String oldPath = appSettings.databasePath == null ? "" : appSettings.databasePath;
        return !newPath.equals(oldPath);
    }

    @Override
    public void reset() { loadData(); }

    @Override
    public void apply() {
        appSettings.readTipEnabled = chkEnabled.isSelected();
        String newPath = textDatabasePath.getText().trim();
        String oldPath = appSettings.databasePath == null ? "" : appSettings.databasePath;
        appSettings.databasePath = newPath;
        if (!newPath.equals(oldPath) && !newPath.isEmpty()) {
            DatabaseManager.getInstance().reconnect(newPath);
        }
    }
}
