
package org.trent.helper.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MyBatisJumpAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        Editor editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR);
        if (editor == null) return;

        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (psiFile == null) return;

        if (psiFile instanceof XmlFile) {
            jumpFromXmlToJava(project, (XmlFile) psiFile, editor);
        } else if (psiFile instanceof PsiJavaFile) {
            jumpFromJavaToXml(project, (PsiJavaFile) psiFile, editor);
        }
    }

    private void jumpFromXmlToJava(Project project, XmlFile xmlFile, Editor editor) {
        XmlTag rootTag = xmlFile.getRootTag();
        if (rootTag == null) return;

        String namespace = rootTag.getAttributeValue("namespace");
        if (namespace == null) return;

        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(namespace, scope);
        if (psiClass != null) {
            FileEditorManager.getInstance(project).openFile(psiClass.getContainingFile().getVirtualFile(), true);
        }
    }

    private void jumpFromJavaToXml(Project project, PsiJavaFile javaFile, Editor editor) {
        PsiClass[] classes = javaFile.getClasses();
        if (classes.length == 0) return;

        PsiClass psiClass = classes[0];
        String className = psiClass.getQualifiedName();

        // 使用 FilenameIndex 查找所有 *.xml 文件
        Collection<VirtualFile> virtualFiles = com.intellij.psi.search.FilenameIndex.getVirtualFilesByName(project, "*.xml", GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (!(psiFile instanceof XmlFile)) continue;

            XmlFile xmlFile = (XmlFile) psiFile;
            XmlTag rootTag = xmlFile.getRootTag();
            if (rootTag == null) continue;

            String namespace = rootTag.getAttributeValue("namespace");
            if (className.equals(namespace)) {
                FileEditorManager.getInstance(project).openFile(xmlFile.getVirtualFile(), true);
                break;
            }
        }
    }
}