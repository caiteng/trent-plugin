package org.trent.helper.mybatis;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MybatisGutterIconRenderer extends GutterIconRenderer {
    private final MybatisMethodPsiElement element;

    public MybatisGutterIconRenderer(MybatisMethodPsiElement element) {
        this.element = element;
    }

    @Override
    public Icon getIcon() {
        return AllIcons.General.ChevronRight;
    }

    @Override
    public String getTooltipText() {
        return "Navigate to Java method";
    }

    @Override
    public AnAction getClickAction() {
        return new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                PsiElement resolvedElement = element.getNavigationElement();
                if (resolvedElement != null) {
                    Editor editor = FileEditorManager.getInstance(element.getContext().getProject()).getSelectedTextEditor();
                    if (editor != null) {
                        PsiManager psiManager = PsiManager.getInstance(element.getContext().getProject());
                        PsiFile psiFile = psiManager.findFile(resolvedElement.getContainingFile().getVirtualFile());
                        if (psiFile != null) {
                            new OpenFileDescriptor(element.getContext().getProject(), psiFile.getVirtualFile(), resolvedElement.getTextOffset()).navigate(true);
                        }
                    }
                }
            }
        };
    }

    @Override
    public boolean isNavigateAction() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
