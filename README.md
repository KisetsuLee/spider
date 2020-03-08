## 多线程爬虫 

多线程爬新闻玩玩

测试CI

#### 算法分析

找到新闻网站入口，使用广度优先算法，对link进行处理

主要流程如下：

!()[简单爬虫算法分析]

- 排除登录页面
- 只爬取新闻页面
- 反爬，加浏览器http头

#### maven自动化插件

发现bug的插件
- spotBugs
- checkstyle插件，调整到verify阶段

#### 数据库处理数据

先暂时使用H2数据库，存储数据

```sql
create table LINKS_TO_BE_PROCESSED (
link varchar (2000)
);

create table LINKS_ALREADY_PROCESSED (
link varchar (2000)
);

create table NEWS (
ID BIGINT primary key auto_increment,
title text,
content text,
url varchar (255),
created_at timestamp ,
modified_at timestamp 
); 
```
> 压制spotbugs的警告，或者说无视检查
> 需要引入SuppressFBWarnings库
> 的前身
> findBugs - annotations

#### 拒绝手工初始化数据库

Flyway数据库迁移工具

数据库结构的版本管理工具


