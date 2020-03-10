package com.lee.spider;

import com.lee.spider.dao.SpiderDAO;
import com.lee.spider.dao.SpiderDAOImpl;
import com.lee.spider.domain.News;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Description:
 * User: Lzj
 * Date: 2020-03-09
 * Time: 10:08
 */
public class Spider {
    private SpiderDAO spiderDAO = new SpiderDAOImpl("db/mybatis/config.xml");

    public static void main(String[] args) {
        try {
            new Spider().run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void run() throws IOException {
        String processingLink;
        while ((processingLink = queryOneUnProcessLinksAndDeleteFromDB()) != null) {
            if (!isProcessedLink(processingLink)) {
                Document doc = getHTMLDocument(processingLink);
                addHTMLNewsLinksToDB(doc);
                if (isNewsPage(doc)) {
                    fetchNewInfoFromHTMLPageToDB(doc, processingLink);
                }
                addLinkToProcessedDB(processingLink);
            }
        }
    }

    private void fetchNewInfoFromHTMLPageToDB(Document doc, String processingLink) {
        Element article = doc.selectFirst("article.art_box");
        String title = article.selectFirst("h1").text();
        String content = article.select(".art_content .art_p").stream().map(Element::text).collect(Collectors.joining("\n"));
        System.out.println(title);
        spiderDAO.addNewsInfo(new News(title, content, processingLink));
    }

    private boolean isNewsPage(Document doc) {
        Elements articles = doc.select("article.art_box");
        return articles.size() != 0;
    }

    private void addHTMLNewsLinksToDB(Document doc) {
        doc.select("body a").stream().map(link -> link.attr("href")).forEach(link -> {
            if (isNewsLink(link)) {
                HashMap<String, String> map = new HashMap<>();
                map.put("tableName", "LINKS_TO_BE_PROCESSED");
                map.put("link", link);
                spiderDAO.insertLink(map);
            }
        });
    }

    private void addLinkToProcessedDB(String processingLink) {
        HashMap<String, String> map = new HashMap<>();
        map.put("tableName", "LINKS_ALREADY_PROCESSED");
        map.put("link", processingLink);
        spiderDAO.insertLink(map);
    }

    private Document getHTMLDocument(String processingLink) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(processingLink);
        httpGet.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
        httpGet.setURI(URI.create(processingLink));
        CloseableHttpResponse response = httpclient.execute(httpGet);
        System.out.println(processingLink);
        HttpEntity entity = response.getEntity();

        String content = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        return Jsoup.parse(content);
    }

    private boolean isProcessedLink(String processingLink) {
        return spiderDAO.selectProcessLink(processingLink) > 0;
    }

    private String queryOneUnProcessLinksAndDeleteFromDB() {
        String link = spiderDAO.selectUnProcessLink();
        if (link != null) {
            spiderDAO.deleteOneUnProcessLink(link);
        }
        return link;
    }

    private boolean isNewsLink(String href) {
        return href.equals("http://sina.cn") || (href.contains("news.sina.cn") && !href.contains("passport.sina.cn") && isValidURL(href));
    }

    private boolean isValidURL(String href) {
        try {
            URL url = new URL(href);
            url.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
    }
}
