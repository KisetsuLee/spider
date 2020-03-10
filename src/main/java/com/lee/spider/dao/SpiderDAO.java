package com.lee.spider.dao;

import com.lee.spider.domain.News;

import java.util.HashMap;

/**
 * Description:
 * User: Lzj
 * Date: 2020-03-10
 * Time: 10:57
 */
public interface SpiderDAO {
    String selectUnProcessLink();

    void deleteOneUnProcessLink(String link);

    int selectProcessLink(String link);

    void insertLink(HashMap<String, String> param);

    void addNewsInfo(News news);
}
