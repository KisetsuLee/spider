package com.lee.spider.domain;

import java.util.Date;

/**
 * Description:
 * User: Lzj
 * Date: 2020-03-10
 * Time: 13:16
 */
public class News {
    private int id;
    private String title;
    private String content;
    private String url;
    private Date create_at;
    private Date modified_at;

    public News(String title, String content, String url) {
        this.title = title;
        this.content = content;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
