package com.ustcinfo.hftnews.model;

/**
 * 新闻资讯实体
 *
 * @author zhang.yifan
 * @date 2020-01-16 10:34
 */
public class News {

    /**
     * 唯一标识
     */
    private String id;

    /**
     * 新闻标题
     */
    private String newsTitle;

    /**
     * 新闻来源
     */
    private String newsSource;

    /**
     * 新闻内容
     */
    private String newsContent;

    /**
     * 新闻缩略图
     */
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getNewsSource() {
        return newsSource;
    }

    public void setNewsSource(String newsSource) {
        this.newsSource = newsSource;
    }

    public String getNewsContent() {
        return newsContent;
    }

    public void setNewsContent(String newsContent) {
        this.newsContent = newsContent;
    }
}

