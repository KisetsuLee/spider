package com.lee.spider.mock;

import com.lee.spider.domain.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

/**
 * Description:
 * User: Lzj
 * Date: 2020-03-10
 * Time: 16:11
 */
public class MockGenerator {
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        generateMockNews(sqlSessionFactory, 1000000);
    }

    private static void generateMockNews(SqlSessionFactory sqlSessionFactory, int targetRowCount) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);) {
            List<News> newsList = session.selectList("com.lee.spider.mock.selectNews");
            int count = targetRowCount - newsList.size();
            if (count < 0) {
                throw new RuntimeException("目标数据条数小于现存数据条数，请输入大于" + newsList.size() + "的数字");
            }
            Random random = new Random();
            try {
                while (count-- > 0) {
                    // 随机选取一个操作
                    News willBeInsertNews = newsList.get(random.nextInt(newsList.size()));
                    // 改变时间，减少一年内的随机时间
                    int randomTime = random.nextInt(3600 * 24 * 365);
                    Instant currentTime = willBeInsertNews.getCreatedAt().minusSeconds(randomTime);
                    currentTime = currentTime.getEpochSecond() < 0 ? Instant.now() : currentTime;
                    willBeInsertNews.setCreatedAt(currentTime);
                    willBeInsertNews.setModifiedAt(currentTime);
                    session.insert("com.lee.spider.mock.insertNews", willBeInsertNews);
                    System.out.println("left: " + count);
                    if (count % 2000 == 0) {
                        session.flushStatements();
                    }
                }
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
