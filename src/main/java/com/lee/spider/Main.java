package com.lee.spider;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description:
 * User: Sujing
 * Date: 2020-03-09
 * Time: 10:08
 */
public class Main {
    public static void main(String[] args) throws IOException {
        String rootURL = "http://sina.cn";

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://sina.cn");
        httpGet.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
        // 用于存放未处理的链接
        ArrayList<String> unHandleLinks = new ArrayList<>();
        // 存放处理过的
        Set<String> handledLinks = new HashSet<>();

        // 使用过的放入处理过的set
        unHandleLinks.add(rootURL);
        while (!unHandleLinks.isEmpty()) {
            try {
                // 开始处理，从后面往前面取
                String processingLink = unHandleLinks.remove(unHandleLinks.size() - 1);
                if (handledLinks.contains(processingLink)) {
                    continue;
                }

                httpGet.setURI(URI.create(processingLink));

                CloseableHttpResponse response = httpclient.execute(httpGet);
                System.out.println(response.getStatusLine());
                System.out.println(processingLink);
                HttpEntity entity = response.getEntity();

                // html转成string
                String content = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);

                Document doc = Jsoup.parse(content);
                Elements links = doc.select("a");
                // 将读取到的放入到数组中
                links.forEach(link -> {
                    String href = link.attr("href");
                    if (href.contains("news.sina.cn") && !href.contains("passport.sina.cn")) {
                        unHandleLinks.add(href);
                    }
                });
                // 加入处理过的set池
                handledLinks.add(processingLink);
                // System.out.println(unHandleLinks);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
