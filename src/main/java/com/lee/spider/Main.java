package com.lee.spider;

import com.lee.spider.dao.SpiderDAO;
import com.lee.spider.dao.SpiderDAOImpl;

/**
 * Description:
 * User: Lzj
 * Date: 2020-03-10
 * Time: 15:53
 */
public class Main {
    public static void main(String[] args) {
        SpiderDAO spiderDAO = new SpiderDAOImpl("db/mybatis/config.xml");
        for (int i = 0; i < 8; i++) {
            new Spider(spiderDAO).start();
        }
    }
}
