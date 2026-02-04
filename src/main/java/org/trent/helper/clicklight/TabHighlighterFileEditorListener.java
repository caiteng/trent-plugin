package org.trent.helper.clicklight;


import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorComposite;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import com.intellij.ui.tabs.TabInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;


public class TabHighlighterFileEditorListener implements FileEditorManagerListener {

    private static final Logger LOGGER = Logger.getInstance(TabHighlighterFileEditorListener.class);
    private final Project project;
    private final LinkedList<VirtualFile> queue = new LinkedList<>();
    private final static int LIMIT = 4;

    public TabHighlighterFileEditorListener(Project project) {
        this.project = project;
        init();
    }

    private void init() {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
        if (selectedEditor != null) {
            VirtualFile file = selectedEditor.getFile();
            SwingUtilities.invokeLater(() -> {
                queue.offer(file);
//                final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);
//                for (EditorWindow editorWindow : manager.getWindows()) {
//                    highlight(file, editorWindow, 0);
//                }
            });
        }
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent fileEditorManagerEvent) {
        if (fileEditorManagerEvent.getManager().getProject().equals(project)) {
            final FileEditorManagerEx manager = FileEditorManagerEx.getInstanceEx(project);
            final FileColorManager fileColorManager = FileColorManager.getInstance(project);
            if (queue.contains(fileEditorManagerEvent.getNewFile())) {
                queue.removeIf(next -> next.equals(fileEditorManagerEvent.getNewFile()));
            }
            if (queue.size() + 1 > LIMIT) {
                VirtualFile virtualFile = queue.poll();
                for (EditorWindow editorWindow : manager.getWindows()) {
                    unHighLight(fileColorManager, virtualFile, editorWindow);
                }
            }

            queue.offer(fileEditorManagerEvent.getNewFile());
            for (EditorWindow editorWindow : manager.getWindows()) {
                for (int i = 0; i < queue.size(); i++) {
                    VirtualFile virtualFile = queue.get(i);
                    if (queue.size() - i - 1 == 0) {
                        // first to unHighLight
                        unHighLight(fileColorManager, virtualFile, editorWindow);
                        continue;
                    }
                    highlight(virtualFile, editorWindow, queue.size() - i - 1);
                }
            }
        }

    }

    private void highlight(VirtualFile file, EditorWindow editorWindow, int i) {
        if (file != null && editorWindow.getComposite(file) != null) {
            switch (i) {
                case 0:
                    setTabColor(new Color(190, 150, 100), file, editorWindow);
                    break;
                case 1:
                    setTabColor(new Color(160, 150, 100), file, editorWindow);
                    break;
                case 2:
                    setTabColor(new Color(130, 130, 110), file, editorWindow);
                    break;
                case 3:
                    setTabColor(new Color(120, 120, 120), file, editorWindow);
                    break;
                default:
                    setTabColor(new Color(190, 150, 100), file, editorWindow);
            }
        }
    }

    private void unHighLight(FileColorManager fileColorManager, VirtualFile oldFile, EditorWindow editorWindow) {
        if (oldFile != null && editorWindow.getComposite(oldFile) != null) {
            setTabColor(fileColorManager.getFileColor(oldFile), oldFile, editorWindow);
        }
    }

    private void setTabColor(Color color, @NotNull VirtualFile file, @NotNull EditorWindow editorWindow) {
        final EditorComposite fileComposite = editorWindow.getComposite(file);
        final int index = editorWindow.getAllComposites().indexOf(fileComposite);
        if (index >= 0) {
            TabInfo tabInfo = editorWindow.getTabbedPane().getTabs().getTabAt(index);
            tabInfo.setTabColor(color);

        }
    }


    @Override
    public void fileOpened(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
        LOGGER.info(String.format("fileOpen %s", virtualFile.getUrl()));
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager fileEditorManager, @NotNull VirtualFile virtualFile) {
        LOGGER.info(String.format("fileClose %s", virtualFile.getUrl()));
    }

}
