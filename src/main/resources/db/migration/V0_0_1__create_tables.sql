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
    id uuid not null
        constraint mst_question_pk
            primary key,
    pattern_id integer not null,
    order_id integer not null,
    title varchar(255) not null,
    detail varchar(255),
    example varchar(255) not null,
    step varchar(1) not null,
    answer_type varchar(1) not null,
    required varchar(1) default '0'::character varying not null
);

create table answer_head
(
    id uuid not null
        constraint answer_head_pk
            primary key,
    book_id varchar(13),
    user_id uuid not null,
    publish_flg varchar(1) default '1'::character varying not null,
    inserted timestamp default CURRENT_TIMESTAMP not null,
    modified timestamp default CURRENT_TIMESTAMP
);

create table answer
(
    id uuid not null
        constraint answer_pk
            primary key,
    order_id integer not null,
    answer_head_id uuid not null
        constraint answer_answer_head_id_fk
            references answer_head,
    question_id uuid not null
        constraint answer_mst_question_id_fk
            references mst_question,
    picture_url varchar(255),
    answer varchar(512),
    inserted timestamp default CURRENT_TIMESTAMP not null,
    modified timestamp default CURRENT_TIMESTAMP
);

create table likes
(
    id uuid not null
        constraint likes_pk
            primary key,
    user_id uuid,
    type varchar(5) not null,
    foreign_id uuid not null,
    inserted timestamp default CURRENT_TIMESTAMP not null
);
