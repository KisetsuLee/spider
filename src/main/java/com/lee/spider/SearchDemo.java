package com.lee.spider;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

/**
 * Description:
 * User: Lzj
 * Date: 2020-03-12
 * Time: 10:44
 */
public class SearchDemo {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        while (true) {
            System.out.println("请输入标题进行搜索（%exit%退出程序）");
            String keyWord = scanner.next();
            if ("%exit%".equals(keyWord)) {
                System.exit(-1);
            }
            try (RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
                SearchRequest searchRequest = new SearchRequest("news");

                QueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery(keyWord, "title", "content");
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(matchQueryBuilder);
                searchRequest.source(sourceBuilder);
                SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
                String hits = response.getHits().getTotalHits().toString();
                System.out.println(hits);
                for (SearchHit hit : response.getHits().getHits()) {
                    String sourceAsString = hit.getSourceAsString();
                    System.out.println(sourceAsString);
                }
            }
        }
    }
}
