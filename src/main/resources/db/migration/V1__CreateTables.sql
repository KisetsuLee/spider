drop table if exists LINKS_TO_BE_PROCESSED;

create table LINKS_TO_BE_PROCESSED
(
    link varchar(2000)
);

drop table if exists LINKS_ALREADY_PROCESSED;

create table LINKS_ALREADY_PROCESSED
(
    link varchar(2000)
);

drop table if exists NEWS;

create table if not exists NEWS
(
    ID          BIGINT primary key auto_increment,
    title       text,
    content     text,
    url         varchar(255),
    created_at  timestamp,
    modified_at timestamp
);

