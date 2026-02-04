package org.trent.helper.mybatis.entity;

import lombok.Data;

@Data
public class MybatisJumpFile {
    private String xmlFilePath;
    private String javaFilePath;

    public boolean isFull() {
        return xmlFilePath != null && javaFilePath != null;
    }
}
