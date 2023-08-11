drop table if exists kerdy.activity;
drop table if exists kerdy.event;
drop table if exists kerdy.member;
drop table if exists kerdy.comment;
drop table if exists kerdy.member_activity;
drop table if exists kerdy.tag;
drop table if exists kerdy.event_tag;
drop table if exists kerdy.member_tag;
drop table if exists kerdy.event_member;
drop table if exists kerdy.notification;
drop table if exists kerdy.fcm_token;
drop table if exists kerdy.block;

create table activity
(
    id   bigint auto_increment primary key,
    type varchar(255) not null,
    name varchar(255) not null
);

create table event
(
    id              bigint auto_increment primary key,
    created_at      datetime(6),
    updated_at      datetime(6),
    end_date        datetime(6)  not null,
    information_url varchar(255) not null,
    location        varchar(255) not null,
    name            varchar(255) not null,
    start_date      datetime(6)  not null,
    image_url       varchar(255),
    type            varchar(20)  not null
);

create table member
(
    id               bigint auto_increment primary key,
    created_at       datetime(6),
    updated_at       datetime(6),
    description      varchar(255) not null default '',
    github_id        bigint       not null unique,
    image_url        varchar(255) not null,
    name             varchar(255),
    open_profile_url varchar(255) null
);

create table comment
(
    id         bigint auto_increment primary key,
    created_at datetime(6),
    updated_at datetime(6),
    content    varchar(255) not null,
    is_deleted bit          not null,
    event_id   bigint       not null,
    member_id  bigint       not null,
    parent_id  bigint       null
);

create table member_activity
(
    id          bigint auto_increment primary key,
    created_at  datetime(6),
    updated_at  datetime(6),
    activity_id bigint not null,
    member_id   bigint not null
);

create table tag
(
    id   bigint auto_increment primary key,
    name varchar(255) not null
);

create table event_tag
(
    id       bigint auto_increment primary key,
    event_id bigint not null,
    tag_id   bigint not null
);

create table member_tag
(
    id         bigint auto_increment primary key,
    created_at datetime(6),
    updated_at datetime(6),
    member_id  bigint not null,
    tag_id     bigint not null
);

create table event_member
(
    id        bigint auto_increment primary key,
    member_id bigint not null,
    event_id  bigint not null
);
create table notification
(
    id          bigint auto_increment primary key,
    created_at  datetime(6),
    updated_at  datetime(6),
    event_id    bigint       not null,
    message     varchar(255) not null,
    receiver_id bigint       not null,
    sender_id   bigint       not null,
    status      varchar(255) not null
);

create table fcm_token
(
    id        bigint auto_increment primary key,
    token     varchar(255) not null,
    member_id bigint       not null unique
);

-- 2023-08-08 14:40
alter table event_member
    add column content varchar(255) not null;
alter table event_member
    add column created_at datetime(6);
alter table event_member
    add column updated_at datetime(6);

-- 2023-08-08 17:20
create table block
(
    id                bigint auto_increment primary key,
    block_member_id   bigint      not null,
    request_member_id bigint      not null,
    created_at        datetime(6) null,
    updated_at        datetime(6) null
);

-- 2023-08-08 23:00
alter table event
    add column apply_start_date datetime(6) not null;
alter table event
    add column apply_end_date datetime(6) not null;
