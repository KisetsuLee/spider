package com.lee.spider.mock;

import com.lee.spider.domain.News;
import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Description:
 * User: Lzj
 * Date: 2020-03-11
 * Time: 20:45
 */
public class ElasticSearchGenerator {
    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            List<News> seedNews = getSeedNews(sqlSessionFactory);
            try (RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost("localhost", 9200, "http")))
            ) {
                BulkRequest bulkRequest = new BulkRequest();
                for (News seedNew : seedNews) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("title", seedNew.getTitle());
                    map.put("content", seedNew.getContent());
                    map.put("url", seedNew.getUrl());

                    IndexRequest request = new IndexRequest("news");
                    request.source(map, XContentType.JSON);
                    // IndexResponse response = client.index(request, RequestOptions.DEFAULT);
                    // System.out.println(response.getIndex());
                    bulkRequest.add(request);
                }
                BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println(bulk.status().getStatus());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<News> getSeedNews(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.lee.spider.mock.selectNews");
        }
    }
}
