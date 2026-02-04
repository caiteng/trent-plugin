package org.trent.helper.mybatis;

import com.intellij.psi.PsiElement;

public class MybatisMethodPsiElement  {
    private final PsiElement element;

    public MybatisMethodPsiElement(PsiElement element) {
        this.element = element;
    }

    public PsiElement getNavigationElement() {
        return element;
    }

    public PsiElement getContext() {
        return element.getContext();
    }
}
