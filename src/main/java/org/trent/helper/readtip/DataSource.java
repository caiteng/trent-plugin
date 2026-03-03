package org.trent.helper.readtip;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class DataSource {
    @SerializedName("baseURL")
    private String baseURL;
    
    @SerializedName("nextURL")
    private String nextURL;
    
    @SerializedName("thisURL")
    private String thisURL;
    
    @SerializedName("previousURL")
    private String previousURL;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("chapterTitle")
    private String chapterTitle;
    
    @SerializedName("textList")
    private List<String> textList = new ArrayList<>();
    
    @SerializedName("index")
    private int index = 0;
    
    // 手动实现getter/setter方法
    public String getBaseURL() { return baseURL; }
    public void setBaseURL(String baseURL) { this.baseURL = baseURL; }
    
    public String getNextURL() { return nextURL; }
    public void setNextURL(String nextURL) { this.nextURL = nextURL; }
    
    public String getThisURL() { return thisURL; }
    public void setThisURL(String thisURL) { this.thisURL = thisURL; }
    
    public String getPreviousURL() { return previousURL; }
    public void setPreviousURL(String previousURL) { this.previousURL = previousURL; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }
    
    public List<String> getTextList() { return textList; }
    public void setTextList(List<String> textList) { this.textList = textList; }
    
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DataSource that = (DataSource) obj;
        return index == that.index &&
               java.util.Objects.equals(baseURL, that.baseURL) &&
               java.util.Objects.equals(nextURL, that.nextURL) &&
               java.util.Objects.equals(thisURL, that.thisURL) &&
               java.util.Objects.equals(previousURL, that.previousURL) &&
               java.util.Objects.equals(title, that.title) &&
               java.util.Objects.equals(chapterTitle, that.chapterTitle) &&
               java.util.Objects.equals(textList, that.textList);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(baseURL, nextURL, thisURL, previousURL, title, chapterTitle, textList, index);
    }
}
