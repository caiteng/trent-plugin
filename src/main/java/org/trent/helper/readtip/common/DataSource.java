package org.trent.helper.readtip.common;

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
    
    @SerializedName("textList")
    private List<String> textList = new ArrayList<>();
    
    @SerializedName("index")
    private int index = 0;
    
    @SerializedName("type")
    private String type = "remote";
    
    @SerializedName("localPath")
    private String localPath;
    
    @SerializedName("localChapterIndex")
    private int localChapterIndex = 0;
    
    @SerializedName("localChunkIndex")
    private int localChunkIndex = 0;
    
    @SerializedName("localCurrentLineNumber")
    private int localCurrentLineNumber = 0;
    
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
    
    public List<String> getTextList() { return textList; }
    public void setTextList(List<String> textList) {
        this.textList = textList == null ? new ArrayList<>() : new ArrayList<>(textList);
    }
    
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    
    public String getType() { return type == null ? "remote" : type; }
    public void setType(String type) { this.type = type; }
    
    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }
    
    public int getLocalChapterIndex() { return localChapterIndex; }
    public void setLocalChapterIndex(int localChapterIndex) { this.localChapterIndex = localChapterIndex; }
    
    public int getLocalChunkIndex() { return localChunkIndex; }
    public void setLocalChunkIndex(int localChunkIndex) { this.localChunkIndex = localChunkIndex; }
    
    public int getLocalCurrentLineNumber() { return localCurrentLineNumber; }
    public void setLocalCurrentLineNumber(int localCurrentLineNumber) { this.localCurrentLineNumber = localCurrentLineNumber; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DataSource that = (DataSource) obj;
        return index == that.index &&
               localChapterIndex == that.localChapterIndex &&
               localChunkIndex == that.localChunkIndex &&
               localCurrentLineNumber == that.localCurrentLineNumber &&
               java.util.Objects.equals(baseURL, that.baseURL) &&
               java.util.Objects.equals(nextURL, that.nextURL) &&
               java.util.Objects.equals(thisURL, that.thisURL) &&
               java.util.Objects.equals(previousURL, that.previousURL) &&
               java.util.Objects.equals(title, that.title) &&
               java.util.Objects.equals(textList, that.textList) &&
               java.util.Objects.equals(type, that.type) &&
               java.util.Objects.equals(localPath, that.localPath);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(baseURL, nextURL, thisURL, previousURL, title, textList, index, type, localPath, localChapterIndex, localChunkIndex, localCurrentLineNumber);
    }
}
