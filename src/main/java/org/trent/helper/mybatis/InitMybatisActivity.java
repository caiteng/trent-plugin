package org.trent.helper.mybatis;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtilRt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.trent.helper.mybatis.entity.MybatisJumpFile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class InitMybatisActivity implements ProjectActivity, DumbAware {

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {

        ApplicationManager.getApplication().runReadAction(() -> {
            System.out.println("***** InitMybatisActivity execute *****");

            Set<String> filenames = new HashSet<>();
            FilenameIndex.processAllFileNames((String s) -> {
                if (s.endsWith("Mapper.xml") || s.endsWith("Mapper.java")) {
                    filenames.add(s);
                }
                return true;
            }, GlobalSearchScope.allScope(project), null);

            Map<String, MybatisJumpFile> map = new HashMap<>();
            for (String filename : filenames) {
                String key = filename.split("\\.")[0];
                MybatisJumpFile mybatisJumpFile = map.getOrDefault(key, new MybatisJumpFile());
                System.out.println("filename: " + filename);
                if (filename.endsWith("Mapper.xml")) {
                    mybatisJumpFile.setXmlFilePath(filename);
                    map.put(key, mybatisJumpFile);
                } else if (filename.endsWith("Mapper.java")) {
                    mybatisJumpFile.setJavaFilePath(filename);
                    map.put(key, mybatisJumpFile);
                }
            }
            //获得同时具备xml和java文件的所有文件名
            Set<String> fileNameSet = map.entrySet().stream().filter(entry -> entry.getValue().isFull()).map(Map.Entry::getKey).collect(Collectors.toSet());
            for (String fileName : fileNameSet) {
                MybatisJumpFile jumpFile = map.get(fileName);
                String xmlFilePath = jumpFile.getXmlFilePath();
                String javaFilePath = jumpFile.getJavaFilePath();
                Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(xmlFilePath, GlobalSearchScope.allScope(project));
                for (VirtualFile file : files) {
                    System.out.println(file.getName());
                    System.out.println(file.getName());
                }


//                PsiFile xmlFile = PsiManager.getInstance(project).findFile(VfsUtil.findFileByIoFile(new File(xmlFilePath), false));
//                PsiFile javaFile = PsiManager.getInstance(project).findFile(VfsUtil.findFileByIoFile(new File(javaFilePath), false));
//
//                if (xmlFile != null && javaFile != null) {
//                    // 假设XML文件中的SQL语句和Java文件中的方法名相同
//                    PsiElement[] xmlElements = xmlFile.getChildren();
//                    for (PsiElement xmlElement : xmlElements) {
//
//                    }
//                }
            }

        });

        return null;
    }
}
