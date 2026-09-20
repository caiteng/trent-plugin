package org.trent.helper.readtip;

import org.junit.jupiter.api.*;
import org.trent.helper.db.DatabaseManager;
import org.trent.helper.db.DataSourceDao;
import org.trent.helper.readtip.common.DataSource;
import org.trent.helper.readtip.common.ReadTipState;
import org.trent.helper.readtip.local.LocalSourceHandler;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 功能级测试：覆盖数据库 CRUD、TXT 导入、翻页（上一页/下一页）、chunk 边界跨越、active_source 切换。
 * 使用临时 SQLite 数据库文件，每个测试前重置。
 */
class DatabaseAndPaginationTest {

    private File tempDb;

    @BeforeEach
    void setUp() throws Exception {
        tempDb = Files.createTempFile("trent-test-", ".db").toFile();
        DatabaseManager.getInstance().initDatabase(tempDb.getAbsolutePath());
        // 若 IntelliJ 平台在测试环境不可用，跳过全部测试
        assumeTrue(DatabaseManager.getInstance().getConnection() != null,
                "Database connection not available — skipping tests");
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.getInstance().close();
        if (tempDb != null) tempDb.delete();
    }

    // ==================== 数据库初始化 ====================

    @Test
    void databaseInit_createsAllTables() throws Exception {
        List<String> tables = queryTableNames();
        assertTrue(tables.contains("data_source"), "data_source 表应存在");
        assertTrue(tables.contains("text_chunk"), "text_chunk 表应存在");
        assertTrue(tables.contains("reading_progress"), "reading_progress 表应存在");
        assertTrue(tables.contains("active_source"), "active_source 表应存在");
        assertEquals(6, tables.size(), "应恰好有 6 张表");
    }

    // ==================== 数据源 CRUD ====================

    @Test
    void insertAndRetrieve_localSource() {
        DataSource ds = localDS("测试书源", "/tmp/test.txt");
        long id = DataSourceDao.insertDataSource(ds);
        assertTrue(id > 0, "插入应返回正数 ID");

        DataSource loaded = DataSourceDao.getDataSourceByTitle("测试书源");
        assertNotNull(loaded);
        assertEquals("测试书源", loaded.getTitle());
        assertEquals("local", loaded.getType());
        assertEquals("/tmp/test.txt", loaded.getBaseURL());
    }

    @Test
    void insertAndRetrieve_remoteSource() {
        DataSource ds = new DataSource();
        ds.setTitle("远程源");
        ds.setType("remote");
        ds.setBaseURL("https://example.com");
        ds.setThisURL("/book/1/chapter/1");
        ds.setNextURL("/book/1/chapter/2");
        ds.setPreviousURL("/book/1/chapter/0");
        assertTrue(DataSourceDao.insertDataSource(ds) > 0);

        DataSource loaded = DataSourceDao.getDataSourceByTitle("远程源");
        assertNotNull(loaded);
        assertEquals("remote", loaded.getType());
        assertEquals("https://example.com", loaded.getBaseURL());
        assertEquals("/book/1/chapter/1", loaded.getThisURL());
        assertEquals("/book/1/chapter/2", loaded.getNextURL());
        assertEquals("/book/1/chapter/0", loaded.getPreviousURL());
    }

    @Test
    void getAllDataSources_returnsAll() {
        DataSourceDao.insertDataSource(localDS("源A", "/a.txt"));
        DataSourceDao.insertDataSource(localDS("源B", "/b.txt"));
        assertEquals(2, DataSourceDao.getAllDataSources().size());
    }

    @Test
    void updateDataSource_changesFields() {
        DataSourceDao.insertDataSource(localDS("测试源", "/old.txt"));
        DataSource ds = DataSourceDao.getDataSourceByTitle("测试源");
        assertNotNull(ds);
        ds.setBaseURL("/new.txt");
        ds.setThisURL("/new/path");
        DataSourceDao.updateDataSource(ds);

        DataSource loaded = DataSourceDao.getDataSourceByTitle("测试源");
        assertNotNull(loaded, "数据源应存在");
        assertEquals("/new.txt", loaded.getBaseURL());
        assertEquals("/new/path", loaded.getThisURL());
    }

    @Test
    void deleteDataSource_cascadesChunksAndProgress() {
        DataSourceDao.insertDataSource(localDS("待删除", "/del.txt"));
        int sid = DataSourceDao.getSourceIdByTitle("待删除");
        assertTrue(sid > 0);
        DataSourceDao.saveChunk(sid, 0, "内容");
        DataSourceDao.saveProgress("待删除", 0, 0);

        DataSourceDao.deleteDataSource("待删除");

        assertNull(DataSourceDao.getDataSourceByTitle("待删除"));
        assertEquals(0, DataSourceDao.getTotalChunks(sid));
    }

    @Test
    void getSourceIdByTitle_notFound_returnsNegative() {
        assertTrue(DataSourceDao.getSourceIdByTitle("不存在") < 0);
    }

    @Test
    void getSourceIdByTitle_found_returnsPositive() {
        DataSourceDao.insertDataSource(localDS("存在", "/x.txt"));
        assertTrue(DataSourceDao.getSourceIdByTitle("存在") > 0);
    }

    // ==================== active_source 切换 ====================

    @Test
    void activeSource_initiallyEmpty() {
        assertNull(DataSourceDao.getActiveSource(), "初始状态 active_source 应为空");
    }

    @Test
    void setActiveSource_canBeRetrieved() {
        DataSourceDao.insertDataSource(localDS("活跃源", "/a.txt"));
        DataSourceDao.setActiveSource("活跃源");

        DataSource active = DataSourceDao.getActiveSource();
        assertNotNull(active);
        assertEquals("活跃源", active.getTitle());
    }

    @Test
    void setActiveSource_switchOverwrites() {
        DataSourceDao.insertDataSource(localDS("源A", "/a.txt"));
        DataSourceDao.insertDataSource(localDS("源B", "/b.txt"));

        DataSourceDao.setActiveSource("源A");
        assertEquals("源A", DataSourceDao.getActiveSource().getTitle());

        DataSourceDao.setActiveSource("源B");
        assertEquals("源B", DataSourceDao.getActiveSource().getTitle(), "切换后应为源B");
    }

    // ==================== chunk 存取 ====================

    @Test
    void saveAndLoadChunk() {
        DataSourceDao.insertDataSource(localDS("chunk测试", "/c.txt"));
        int sid = DataSourceDao.getSourceIdByTitle("chunk测试");

        DataSourceDao.saveChunk(sid, 0, "第一块内容");
        DataSourceDao.saveChunk(sid, 1, "第二块内容");

        assertEquals("第一块内容", DataSourceDao.loadChunk(sid, 0));
        assertEquals("第二块内容", DataSourceDao.loadChunk(sid, 1));
        assertNull(DataSourceDao.loadChunk(sid, 2), "不存在的 chunk 应返回 null");
        assertEquals(2, DataSourceDao.getTotalChunks(sid));
    }

    // ==================== 进度保存 ====================

    @Test
    void saveProgress_insertAndRetrieve() {
        DataSourceDao.insertDataSource(localDS("进度测试", "/p.txt"));
        DataSourceDao.saveProgress("进度测试", 3, 7);

        DataSource ds = DataSourceDao.getDataSourceByTitle("进度测试");
        assertNotNull(ds);
        assertEquals(3, ds.getLocalChunkIndex());
        assertEquals(7, ds.getIndex());
    }

    @Test
    void saveProgress_upsertOnConflict() {
        DataSourceDao.insertDataSource(localDS("进度更新", "/p.txt"));
        DataSourceDao.saveProgress("进度更新", 0, 0);
        DataSourceDao.saveProgress("进度更新", 5, 10);

        DataSource ds = DataSourceDao.getDataSourceByTitle("进度更新");
        assertNotNull(ds);
        assertEquals(5, ds.getLocalChunkIndex(), "应更新为最新 chunkIndex");
        assertEquals(10, ds.getIndex(), "应更新为最新 index");
    }

    // ==================== TXT 导入 + 翻页 ====================

    @Test
    void importTxtAndPaginate_forwardOnly() throws Exception {
        File txt = createTxtFile(120); // 120 行 / 50 行每 chunk = 3 chunks
        int sourceId = setupLocalSourceWithTxt("导入翻页", txt);

        ReadTipState state = createState("导入翻页", sourceId, 3);

        // 首次 nextPage 触发 ensureContentLoaded → 加载 chunk 0
        String p1 = LocalSourceHandler.nextPage(state);
        assertNotNull(p1);
        assertEquals(0, state.chunkIndex);
        assertEquals(1, state.index);

        // 翻过 chunk 0 剩余页面（chunk 0 换行为空格后 340 字符，按 35 切分 = 10 页，idx 0-9）
        for (int i = 0; i < 8; i++) {
            String p = LocalSourceHandler.nextPage(state);
            assertNotNull(p);
        }
        assertEquals(0, state.chunkIndex);
        assertEquals(9, state.index);

        // 跨入 chunk 1
        String pNext = LocalSourceHandler.nextPage(state);
        assertNotNull(pNext);
        assertEquals(1, state.chunkIndex, "应进入 chunk 1");
        assertEquals(0, state.index, "新 chunk 起始 index 为 0");
    }

    @Test
    void nextPage_atLastPage_returnsEndMessage() throws Exception {
        File txt = createTxtFile(55); // 2 chunks
        int sourceId = setupLocalSourceWithTxt("末页测试", txt);
        ReadTipState state = createState("末页测试", sourceId, 2);

        // 翻到 chunk 1 最后一页
        // chunk 0 换行为空格后 340 字符 → 10 页(idx 0-9)；chunk 1 换行为空格后 34 字符 → 1 页(idx 0)
        for (int i = 0; i < 9; i++) LocalSourceHandler.nextPage(state); // 遍历完 chunk 0 (idx 0→9)
        String last = LocalSourceHandler.nextPage(state); // → chunk1 idx0
        assertNotNull(last);
        assertEquals(1, state.chunkIndex);
        assertEquals(0, state.index);

        assertEquals("已到达最后一页", LocalSourceHandler.nextPage(state));
    }

    @Test
    void prevPage_atFirstPage_returnsNoPrevMessage() throws Exception {
        File txt = createTxtFile(55);
        int sourceId = setupLocalSourceWithTxt("首页测试", txt);
        ReadTipState state = createState("首页测试", sourceId, 2);

        // 加载内容后 index=0, chunkIndex=0
        LocalSourceHandler.nextPage(state); // 确保内容已加载，到 idx=1
        LocalSourceHandler.prevPage(state); // 回到 idx=0
        assertEquals(0, state.index);
        assertEquals(0, state.chunkIndex);

        assertEquals("当前没有可返回的上一页", LocalSourceHandler.prevPage(state));
    }

    @Test
    void prevPage_crossesChunkBoundary() throws Exception {
        File txt = createTxtFile(160); // 4 chunks (50+50+50+10)
        int sourceId = setupLocalSourceWithTxt("跨chunk回退", txt);
        ReadTipState state = createState("跨chunk回退", sourceId, 4);

        // chunk 0/1 换行为空格后各 340~349 字符 → 各 10 页(idx 0-9)
        // 翻到 chunk 2 第一页
        for (int i = 0; i < 9; i++) LocalSourceHandler.nextPage(state); // 遍历完 chunk 0 (idx 0→9)
        LocalSourceHandler.nextPage(state); // → chunk1 idx0
        for (int i = 0; i < 9; i++) LocalSourceHandler.nextPage(state); // 遍历完 chunk 1 (idx 0→9)
        LocalSourceHandler.nextPage(state); // → chunk2 idx0
        assertEquals(2, state.chunkIndex);
        assertEquals(0, state.index);

        // prevPage 应跨回 chunk 1 最后一页
        String prev = LocalSourceHandler.prevPage(state);
        assertNotNull(prev);
        assertEquals(1, state.chunkIndex, "应回退到 chunk 1");
        assertEquals(9, state.index, "应为 chunk 1 最后一页");
    }

    @Test
    void nextPagePrevPage_forwardThenBackward_multipleChunks() throws Exception {
        File txt = createTxtFile(105); // 3 chunks (50+50+5)
        int sourceId = setupLocalSourceWithTxt("来回翻页", txt);
        ReadTipState state = createState("来回翻页", sourceId, 3);

        // chunk 0 换行为空格后 340 字符 → 10 页(idx 0-9)
        // 前进 10 步：9步到 chunk 0 末尾(idx 9)，第10步跨入 chunk 1
        for (int i = 0; i < 10; i++) LocalSourceHandler.nextPage(state);
        assertEquals(1, state.chunkIndex);
        assertEquals(0, state.index);

        // 后退：chunk1 idx0 → prevPage 跨回 chunk 0 最后一页(idx 9)
        LocalSourceHandler.prevPage(state);
        assertEquals(0, state.chunkIndex);
        assertEquals(9, state.index);
    }

    @Test
    void loadChunkContent_emptyChunk_returnsFallback() {
        DataSourceDao.insertDataSource(localDS("空chunk", "/empty.txt"));
        int sid = DataSourceDao.getSourceIdByTitle("空chunk");

        ReadTipState state = new ReadTipState();
        state.sourceId = sid;
        state.chunkIndex = 99; // 不存在的 chunk
        LocalSourceHandler.loadChunkContent(state);

        assertEquals(1, state.textList.size());
        assertEquals("当前页面暂时没有解析到可显示的内容", state.textList.get(0));
    }

    // ==================== changeSource 集成 ====================

    @Test
    void changeSource_localSource_setsIdAndTotalChunks() throws Exception {
        File txt = createTxtFile(120); // 3 chunks
        int sourceId = setupLocalSourceWithTxt("切换源", txt);

        ReadTipState state = new ReadTipState();
        DataSource ds = DataSourceDao.getDataSourceByTitle("切换源");
        assertNotNull(ds);

        state.changeSource(ds);

        assertEquals("local", state.type);
        assertEquals(sourceId, state.sourceId);
        assertEquals(3, state.totalChunks);
        assertEquals(0, state.chunkIndex);
        assertTrue(state.textList.isEmpty(), "changeSource 应清空 textList");
    }

    // ==================== 辅助方法 ====================

    private List<String> queryTableNames() throws Exception {
        java.sql.Connection conn = DatabaseManager.getInstance().getConnection();
        assertNotNull(conn);
        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name")) {
            var tables = new java.util.ArrayList<String>();
            while (rs.next()) tables.add(rs.getString("name"));
            return tables;
        }
    }

    /** 创建含指定行数的临时 TXT 文件 */
    private File createTxtFile(int lines) throws Exception {
        File f = Files.createTempFile("test-book-", ".txt").toFile();
        try (FileWriter w = new FileWriter(f)) {
            for (int i = 1; i <= lines; i++) {
                w.write("第" + i + "行内容\n");
            }
        }
        return f;
    }

    /** 创建本地数据源、导入 TXT、返回 sourceId */
    private int setupLocalSourceWithTxt(String title, File txtFile) {
        DataSource ds = localDS(title, txtFile.getAbsolutePath());
        DataSourceDao.insertDataSource(ds);
        int sourceId = DataSourceDao.getSourceIdByTitle(title);
        DataSourceDao.importTxtFile(sourceId, txtFile.getAbsolutePath());
        return sourceId;
    }

    /** 创建预加载内容的 ReadTipState */
    private ReadTipState createState(String title, int sourceId, int totalChunks) {
        ReadTipState state = new ReadTipState();
        state.title = title;
        state.type = "local";
        state.sourceId = sourceId;
        state.totalChunks = totalChunks;
        state.baseURL = ""; // 已导入，不需要原文件路径
        state.chunkIndex = 0;
        state.index = 0;
        return state;
    }

    private static DataSource localDS(String title, String path) {
        DataSource ds = new DataSource();
        ds.setTitle(title);
        ds.setType("local");
        ds.setBaseURL(path);
        return ds;
    }
}
