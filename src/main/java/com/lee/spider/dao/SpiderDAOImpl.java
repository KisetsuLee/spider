package com.lee.spider.dao;

import com.lee.spider.domain.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Description:
 * User: Lzj
 * Date: 2020-03-10
 * Time: 11:20
 */
public class SpiderDAOImpl implements SpiderDAO {
    private SqlSessionFactory sqlSessionFactory;

    public SpiderDAOImpl(String resource) {
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String selectUnProcessLink() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectOne("com.lee.spider.Spider.queryNextLink");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteOneUnProcessLink(String link) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.delete("com.lee.spider.Spider.deleteUnProcessLink", link);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int selectProcessLink(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectOne("com.lee.spider.Spider.isProcessedLink", link);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertLink(HashMap<String, String> param) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.lee.spider.Spider.insertLink", param);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addNewsInfo(News news) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.lee.spider.Spider.insertNews", news);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
