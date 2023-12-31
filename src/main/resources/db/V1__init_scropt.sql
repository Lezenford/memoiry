create table hint
(
    id      int          not null generated by default as identity primary key,
    user_id bigint       not null,
    key     varchar(255) not null,
    value   text         not null,
    type    varchar(255) not null
);

create unique index ui_hint_user_id_key on hint (user_id, key);