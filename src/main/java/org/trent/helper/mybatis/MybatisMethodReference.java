package org.trent.helper.mybatis;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MybatisMethodReference extends PsiReferenceBase<PsiElement> {
    private final String methodName;

    public MybatisMethodReference(@NotNull PsiElement element, String methodName) {
        super(element, TextRange.from(0, methodName.length()));
        this.methodName = methodName;
    }

    @Override
    public PsiElement resolve() {
        Project project = getElement().getProject();
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
//        Collection<PsiFile> files = FilenameIndex.getFilesByName(project, "YourMapper.java", scope);
//
//        for (PsiFile file : files) {
//            PsiClass[] classes = PsiTreeUtil.getChildrenOfType(file, PsiClass.class);
//            if (classes != null) {
//                for (PsiClass clazz : classes) {
//                    PsiMethod method = clazz.findMethodByName(methodName);
//                    if (method != null) {
//                        return method;
//                    }
//                }
//            }
//        }

        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }
}
