
create schema if not exists user_management;

create type user_management.authority as enum ('ROLE_USER', 'ROLE_ADMIN');

create table user_management.t_users
(
    id       bigserial primary key,
    username varchar(255) not null,
    password varchar(255) not null,
    enabled boolean not null default true,
    created_at timestamptz not null default now()
);

create unique index username_users_idx on user_management.t_users(lower(username));

create table user_management.t_authorities
(
    id    smallserial primary key,
    name user_management.authority not null unique
);

create table user_management.t_user_authorities
(
    id bigserial primary key,
    user_id bigint not null references user_management.t_users(id) on delete cascade,
    authority_id smallint not null references user_management.t_authorities(id) on delete cascade,
    constraint user_authority_unique_constraint unique (user_id, authority_id)
);