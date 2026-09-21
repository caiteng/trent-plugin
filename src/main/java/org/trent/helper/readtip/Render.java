package org.trent.helper.readtip;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.awt.RelativePoint;
import org.trent.helper.db.SettingsDao;
import org.trent.helper.readtip.common.ReadTipState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelListener;

public class Render {
    private static Render instance;
    private JBPopup currentPopup;
    private JLabel contentLabel;
    private Timer fadeoutTimer;
    private MouseWheelListener wheelHandler;
    private DataContext lastDataContext;
    private AnActionEvent lastEvent;
    private boolean isRendering;
    private long lastWheelTime;
    private int lastWheelDirection;

    private Render() {
    }

    public static synchronized Render getInstance() {
        if (instance == null) {
            instance = new Render();
        }
        return instance;
    }

    public void render(AnActionEvent e, String tip) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> render(e, tip));
            return;
        }
        if (isRendering) return;
        isRendering = true;
        try {
            if (currentPopup != null && !currentPopup.isDisposed()) {
                currentPopup.dispose();
            }
            JBPopupFactory factory = JBPopupFactory.getInstance();
            String escapedTip = StringUtil.escapeXmlEntities(tip == null ? "" : tip).replace("\n", "<br/>");
            String grayTip = "<html><body><span style='color: gray;'>" + escapedTip + "</span></body></html>";

            contentLabel = new JLabel(grayTip);
            contentLabel.setOpaque(false);
            contentLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

            currentPopup = factory.createComponentPopupBuilder(contentLabel, null)
                    .setRequestFocus(false)
                    .setFocusable(false)
                    .setShowBorder(false)
                    .setShowShadow(false)
                    .setBorderColor(new Color(0, 0, 0, 0))
                    .createPopup();

            //设置popup尺寸：优先使用用户配置的固定宽度，否则遍历所有分页取最大宽度
            Dimension labelPref = contentLabel.getPreferredSize();
            int configuredWidth = SettingsDao.getConfigInt("bubble_width", 0);
            int popupWidth;
            if (configuredWidth > 0) {
                popupWidth = configuredWidth;
            } else {
                JLabel tempLabel = new JLabel();
                tempLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                popupWidth = labelPref.width;
                ReadTipState state = ReadTipState.getInstance();
                for (String page : state.textList) {
                    String esc = StringUtil.escapeXmlEntities(page).replace("\n", "<br/>");
                    tempLabel.setText("<html><body><span style='color: gray;'>" + esc + "</span></body></html>");
                    popupWidth = Math.max(popupWidth, tempLabel.getPreferredSize().width);
                }
                popupWidth += 20;
            }
            currentPopup.setMinimumSize(new Dimension(popupWidth, labelPref.height + 10));
            currentPopup.setSize(new Dimension(popupWidth, labelPref.height + 10));

            startFadeoutTimer();

            lastDataContext = e.getDataContext();
            lastEvent = e;
            installWheelListener();

            // 定位popup：优先用编辑器位置，fallback到鼠标位置
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (editor != null) {
                currentPopup.showInBestPositionFor(e.getDataContext());
            } else {
                Point mousePos = MouseInfo.getPointerInfo().getLocation();
                Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
                Component target = activeWindow != null ? activeWindow : contentLabel;
                SwingUtilities.convertPointFromScreen(mousePos, target);
                currentPopup.show(new RelativePoint(target, mousePos));
            }

            // show之后Window已创建沿层级链处理
            Container parent = contentLabel.getParent();
            boolean isFirst = true;
            while (parent != null) {
                if (parent instanceof JComponent) {
                    if (isFirst) {
                        ((JComponent) parent).setOpaque(true);
                        ((JComponent) parent).setBackground(new Color(0, 0, 0, 1));
                    } else {
                        ((JComponent) parent).setOpaque(false);
                        ((JComponent) parent).setBackground(new Color(0, 0, 0, 0));
                    }
                    ((JComponent) parent).setBorder(null);
                }
                parent = parent.getParent();
                isFirst = false;
            }
            Window window = SwingUtilities.getWindowAncestor(contentLabel);
            if (window != null) {
                window.setBackground(new Color(0, 0, 0, 0));
                if (wheelHandler != null) {
                    window.addMouseWheelListener(wheelHandler);
                }
            }

            // 延迟再次清除边框防止LAF初始化时重新安装
            SwingUtilities.invokeLater(() -> {
                Container p = contentLabel.getParent();
                while (p != null) {
                    if (p instanceof JComponent) {
                        ((JComponent) p).setBorder(null);
                    }
                    p = p.getParent();
                }
            });
        } finally {
            isRendering = false;
        }
    }

    private void installWheelListener() {
        if (contentLabel == null) return;
        wheelHandler = ev -> {
            if (isRendering) return;
            if (currentPopup == null || currentPopup.isDisposed()) return;
            int direction = ev.getWheelRotation() > 0 ? 1 : -1;
            long now = System.currentTimeMillis();
            if (direction == lastWheelDirection && (now - lastWheelTime) < 500) return;
            lastWheelTime = now;
            lastWheelDirection = direction;
            String tip;
            if (ev.getWheelRotation() > 0) {
                tip = ReadTipDispatcher.getTipContentPlus();
            } else {
                tip = ReadTipDispatcher.getTipContentSub();
            }
            if (lastEvent != null) {
                render(lastEvent, tip);
            }
        };
    }

    private void startFadeoutTimer() {
        if (fadeoutTimer != null) fadeoutTimer.stop();
        int fadeoutMs = SettingsDao.getConfigInt("fadeout_seconds", 5) * 1000;
        fadeoutTimer = new Timer(fadeoutMs, ev -> {
            if (currentPopup != null && !currentPopup.isDisposed()) {
                currentPopup.dispose();
            }
        });
        fadeoutTimer.setRepeats(false);
        fadeoutTimer.start();
    }
}