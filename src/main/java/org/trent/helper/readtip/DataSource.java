package org.trent.helper.readtip;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataSource {
    private String baseURL;
    private String nextURL;
    private String thisURL;
    private String previousURL;
    private String title;
    private String chapterTitle;
    private List<String> textList = new ArrayList<>();
    private int index = 0;
}
