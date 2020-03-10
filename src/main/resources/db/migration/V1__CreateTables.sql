ALTER DATABASE news CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

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
    url         varchar(2000),
    created_at  timestamp default now(),
    modified_at timestamp default now()
);

