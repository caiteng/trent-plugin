package org.trent.helper.clicklight;

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorComposite;
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;


public class CustomEditorTabColorProvider implements EditorTabColorProvider {
    @Nullable
    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        final FileEditorManagerEx fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(project);
        final FileColorManager fileColorManager = FileColorManager.getInstance(project);
        final EditorWindow activeWindow = fileEditorManagerEx.getCurrentWindow();
        if (activeWindow != null) {
            final EditorComposite selectedEditor = activeWindow.getSelectedComposite();
            if (selectedEditor != null && virtualFile.equals(selectedEditor.getFile())) {
                return new Color(190, 150, 100);
            }
        }
        return fileColorManager.getFileColor(virtualFile);
    }
}
