create schema if not exists online_store;

create table online_store.t_product
(
    id      bigserial primary key,
    c_title varchar(50) not null check (length(trim(c_title)) >= 1),
    c_price numeric     not null check ( c_price > 0 )
);