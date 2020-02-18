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
    id uuid not null default uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
    pattern_id int not null,
    order_id int not null,
    title varchar(255) not null,
    detail varchar(255) not null,
    example varchar(255) not null,
    step varchar(1) not null,
    answer_type varchar(1) not null,
    required varchar(1) default '0' not null,
    constraint mst_question_pk
        primary key (id)
);

create table answer
(
    id uuid not null default uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
    orderId int not null,
    answer_head_id uuid not null,
	question_id uuid not null,
    picture_url varchar(255),
    answer varchar(512) null,
    inserted timestamp default current_timestamp not null,
    modified timestamp default current_timestamp
);

create table answer_head
(
    id uuid not null default uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
    user_id uuid not null,
    publish_flg varchar(1) not null default '1',
    inserted timestamp default current_timestamp not null,
    modified timestamp default current_timestamp
);

create table likes
(
    id uuid not null default uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
    type varchar(5) not null,
    foreign_id uuid not null,
    inserted timestamp default current_timestamp not null
);
