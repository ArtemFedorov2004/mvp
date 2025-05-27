create schema if not exists online_store;

create table online_store.t_product
(
    id      bigserial primary key,
    c_title varchar(50) not null check (length(trim(c_title)) >= 1),
    c_price numeric     not null check ( c_price > 0 )
);

create table online_store.t_customer
(
    id         uuid not null primary key default gen_random_uuid(),
    c_username varchar(256) not null unique
);

create table online_store.t_customer_oidcuser
(
    id_customer uuid not null unique,
    id_oidcuser uuid not null unique,
    constraint t_customer_oidcuser_customer_fk foreign key (id_customer) references online_store.t_customer (id)
);