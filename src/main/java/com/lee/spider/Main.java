package com.lee.spider;

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
import java.sql.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Description:
 * User: Sujing
 * Date: 2020-03-09
 * Time: 10:08
 */
public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException {
        try {
            String jdbcUrl = "jdbc:h2:file:c:/tasks/project/spider/news";
            Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");
            String processingLink;
            while ((processingLink = queryOneUnProcessLinksFromDataBase(connection)) != null) {
                removeLinkFromDB(connection, processingLink, "delete from LINKS_TO_BE_PROCESSED where link = ?");
                if (!isProcessedLink(connection, processingLink)) {
                    Document doc = getHTMLDocument(processingLink);
                    addHTMLNewsLinksToDB(connection, doc);
                    if (isNewsPage(doc)) {
                        fetchNewInfoFromHTMLPageToDB(connection, doc, processingLink);
                    }
                    addLinkToDB(connection, processingLink, "insert into LINKS_ALREADY_PROCESSED values(?)");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void fetchNewInfoFromHTMLPageToDB(Connection connection, Document doc, String processingLink) throws SQLException {
        Element article = doc.selectFirst("article.art_box");
        String title = article.selectFirst("h1").text();
        String content = article.select(".art_content .art_p").stream().map(Element::text).collect(Collectors.joining("\n"));
        PreparedStatement preparedStatement = connection.prepareStatement("insert into NEWS (TITLE, CONTENT, URL, CREATED_AT, MODIFIED_AT) values (?, ?, ?, now(), now())");
        preparedStatement.setString(1, title);
        System.out.println(title);
        preparedStatement.setString(2, content);
        preparedStatement.setString(3, processingLink);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private static boolean isNewsPage(Document doc) {
        Elements articles = doc.select("article.art_box");
        return articles.size() != 0;
    }

    private static void addHTMLNewsLinksToDB(Connection connection, Document doc) {
        doc.select("body a").stream().map(link -> link.attr("href")).forEach(link -> {
            if (isNewsLink(link)) {
                addLinkToDB(connection, link, "insert into LINKS_TO_BE_PROCESSED values(?)");
            }
        });
    }

    private static void removeLinkFromDB(Connection connection, String processingLink, String sql) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, processingLink);
            int i = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addLinkToDB(Connection connection, String processingLink, String sql) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, processingLink);
            int i = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Document getHTMLDocument(String processingLink) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(processingLink);
        httpGet.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
        httpGet.setURI(URI.create(processingLink));
        CloseableHttpResponse response = httpclient.execute(httpGet);
        System.out.println(response.getStatusLine());
        System.out.println(processingLink);
        HttpEntity entity = response.getEntity();

        String content = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        return Jsoup.parse(content);
    }


    private static boolean isProcessedLink(Connection connection, String processingLink) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("select * from LINKS_ALREADY_PROCESSED where link = ?");
            preparedStatement.setString(1, processingLink);
            resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static String queryOneUnProcessLinksFromDataBase(Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select * from LINKS_TO_BE_PROCESSED limit 1");
             ResultSet resultSet = preparedStatement.executeQuery();) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static boolean isNewsLink(String href) {
        return href.equals("http://sina.cn") || (href.contains("news.sina.cn") && !href.contains("passport.sina.cn") && isValidURL(href));
    }

    private static boolean isValidURL(String href) {
        try {
            URL url = new URL(href);
            url.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
        return true;
    }


}
