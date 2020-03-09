package com.lee.spider;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;

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

            while (true) {
                ArrayList<String> unProcessedLinks = queryLinksFromDataBase(connection, "select * from LINKS_TO_BE_PROCESSED");
                if (unProcessedLinks.isEmpty()) {
                    break;
                }

                String processingLink = unProcessedLinks.remove(unProcessedLinks.size() - 1);
                removeLinkFromDB(connection, processingLink, "delete from LINKS_TO_BE_PROCESSED where link = ?");
                if (isProcessedLink(connection, processingLink)) {
                    continue;
                }

                if (isNewsLink(processingLink)) {
                    Document doc = getHTMLDocument(processingLink);
                    doc.select("body a").stream().map(link -> link.attr("href")).forEach(link -> {
                        addLinkToDB(connection, link, "insert into LINKS_TO_BE_PROCESSED values(?)");
                    });
                } else {
                    // 不感兴趣的不处理
                    // System.out.println("不感兴趣的");
                }

                // 加入处理过的set池
                addLinkToDB(connection, processingLink, "insert into LINKS_ALREADY_PROCESSED values(?)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

    private static ArrayList<String> queryLinksFromDataBase(Connection connection, String sql) {
        ArrayList<String> list = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery();) {
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
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
