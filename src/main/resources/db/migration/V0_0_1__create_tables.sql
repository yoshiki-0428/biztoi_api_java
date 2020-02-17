create table book
(
	isbn varchar(13) not null
		constraint book_pkey
			primary key,
	title varchar(255),
	detail varchar(512),
	picture_url varchar(255),
	link_url varchar(255),
	authors varchar(255),
	categories varchar(255)
);

create table mst_toi
(
	title varchar(255) not null,
	detail varchar(255) not null,
	picture_url varchar(255),
	constraint mst_toi_pk
		primary key (title, detail)
);

create table mst_question
(
    pattern_id int not null,
    order_id int not null,
    title varchar(255) not null,
    detail varchar(255) not null,
    step varchar(1) not null,
    required varchar(1) default '0' not null,
    constraint mst_question_pk
        primary key (pattern_id, order_id)
);

-- create table ANSWER_HEAD
-- (
--     ID int auto_increment comment '回答ID'
--         primary key,
--     USER_ID varchar(128) not null comment '所有者',
--     TOI_ID int not null comment '問題集と紐づくID',
--     PUBLISH_FLG bit(1) not null default 1 comment '公開フラグ',
--     INSERTED timestamp default current_timestamp,
--     MODIFIED timestamp default current_timestamp not null,
--     constraint ANSWER_USER_ID_fk
--         foreign key (USER_ID) references USER (ID),
--     constraint ANSWER_HEAD_TOI_ID_fk
--         foreign key (TOI_ID) references TOI (ID)
-- );
--
-- create table ANSWER
-- (
-- 	ID int auto_increment comment '回答ID'
-- 		primary key,
--     ANSWER_HEAD_ID int not null,
-- 	QUESTION_ID int not null comment '質問と紐づくID',
--     PICTURE_URL varchar(255) null comment '問題に画像を付ける場合',
-- 	ANSWER text null comment '回答内容',
--     INSERTED timestamp default current_timestamp,
--     MODIFIED timestamp default current_timestamp not null,
--     constraint ANSWER_HEAD_QUESTION_ID_fk
--         foreign key (ANSWER_HEAD_ID) references ANSWER_HEAD (ID),
--     constraint ANSWER_QUESTION_ID_fk
-- 		foreign key (QUESTION_ID) references QUESTION (ID)
-- );
--
-- create table TALK
-- (
-- 	ID int auto_increment comment '本ID'
-- 		primary key,
-- 	TOI_ID int not null comment '問題集と紐づくID',
-- 	USER_ID varchar(128) not null comment '所有者',
-- 	TALK_ID int null comment '返信した場合、紐付けをする',
-- 	COMMENT varchar(255) null comment 'コメント',
--     INSERTED timestamp default current_timestamp,
--     MODIFIED timestamp default current_timestamp not null,
-- 	constraint TALK_TALK_ID_fk
-- 		foreign key (TALK_ID) references TALK (ID),
-- 	constraint TALK_TOI_ID_fk
-- 		foreign key (TOI_ID) references TOI (ID)
-- );

-- create table LIKES
-- (
--     ID int auto_increment comment 'いいね' primary key,
--     TOI_ID int null comment '問題集と紐づくID',
--     QUESTION_ID int null comment '質問と紐づくID',
--     ANSWER_HEAD_ID int null comment '回答まとめと紐づくID',
--     ANSWER_ID int null comment '回答まとめと紐づくID',
--     TALK_ID int null comment 'トークと紐づくID',
--     INSERTED timestamp default current_timestamp
-- );


-- create table user
-- (
-- 	ID varchar(128) not null comment 'USER ID'
-- 		primary key,
-- 	ID_TOKEN text not null comment '一時的アクセスのためのトークン',
-- 	ACCESS_TOKEN text not null comment '一時的アクセスのためのトークン',
-- 	REFRESH_TOKEN text not null comment 'アクセストークン更新のためのトークン',
-- 	INSERTED timestamp default current_timestamp,
--     MODIFIED timestamp default current_timestamp not null
-- );
