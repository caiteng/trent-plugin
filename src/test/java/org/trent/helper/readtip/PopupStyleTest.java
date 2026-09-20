package org.trent.helper.readtip;

import java.awt.*;
import javax.swing.*;

/**
 * 独立 Swing 测试：验证 JBPopup 透明样式的渲染效果。
 * 直接运行 main() 即可看到效果，无需打包插件。
 * <p>
 * 窗口背景设为渐变色，方便观察透明效果。
 * 点击按钮弹出 popup，观察是否透明。
 */
public class PopupStyleTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PopupStyleTest::createAndShow);
    }

    private static void createAndShow() {
        // === 主窗口（带渐变背景，方便观察透明效果）===
        JFrame frame = new JFrame("Popup 透明样式测试");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);

        // 渐变背景面板
        JPanel bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(70, 130, 180),
                        getWidth(), getHeight(), new Color(255, 165, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bgPanel.setLayout(new FlowLayout());
        frame.setContentPane(bgPanel);

        // 提示标签
        JLabel hint = new JLabel("点击按钮弹出 Popup，观察透明效果");
        hint.setForeground(Color.WHITE);
        hint.setFont(hint.getFont().deriveFont(16f));
        bgPanel.add(hint);

        // 弹出按钮
        JButton btn = new JButton("显示 Popup");
        bgPanel.add(btn);

        btn.addActionListener(e -> showPopup(btn));

        frame.setVisible(true);
    }

    /**
     * 模拟 Render.java 的 popup 创建逻辑，使用纯 Swing 复现 JBPopup 的内部层级：
     * JWindow → JPanel(MyContentPanel) → JLabel(contentLabel)
     */
    private static JWindow currentWindow;

    private static void showPopup(JButton owner) {
        if (currentWindow != null) {
            currentWindow.dispose();
        }

        String text = "这是一段测试文本，用来验证透明背景和无边框的效果是否生效。";
        String html = "<html><body><span style='color: gray;'>" + text + "</span></body></html>";

        // --- 模拟 JBPopup 内部层级 ---

        // 1. contentLabel（对应 Render.java 的 contentLabel）
        JLabel contentLabel = new JLabel(html);
        contentLabel.setOpaque(false);
        contentLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        // 2. MyContentPanel（对应 JBPopup 内部的 MyContentPanel）
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(contentLabel, BorderLayout.CENTER);
        contentPanel.setOpaque(false);
        contentPanel.setBackground(new Color(0, 0, 0, 0));

        // 3. JWindow（对应 JBPopup 的 Window 层）
        currentWindow = new JWindow();
        currentWindow.setBackground(new Color(0, 0, 0, 0));
        currentWindow.setContentPane(contentPanel);

        // 设置大小
        Dimension pref = contentLabel.getPreferredSize();
        currentWindow.setSize(pref.width + 25, pref.height);

        // 定位到按钮下方
        Point loc = owner.getLocationOnScreen();
        currentWindow.setLocation(loc.x, loc.y + owner.getHeight() + 5);
        currentWindow.setVisible(true);

        // 5 秒后自动关闭
        Timer timer = new Timer(5000, ev -> {
            if (currentWindow != null) {
                currentWindow.dispose();
                currentWindow = null;
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}
