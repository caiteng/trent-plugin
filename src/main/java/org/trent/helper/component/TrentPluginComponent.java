package org.trent.helper.component;


import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.Extensions;
import org.jetbrains.annotations.NotNull;
import org.trent.helper.clicklight.CustomEditorTabColorProvider;

public class TrentPluginComponent implements ApplicationComponent {

    @Override
    public void initComponent() {
        // editorTabColorProvider
//        Extensions.getRootArea().getExtensionPoint("com.intellij.editorTabColorProvider")
//                .registerExtension(new CustomEditorTabColorProvider());
    }

    @Override
    public void disposeComponent() {
        // 清理工作
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ClickLightPluginComponent";
    }
}