create table online_store.t_review
(
    id              bigserial primary key,
    c_rating        integer     not null check (c_rating > 0 and c_rating < 6),
    c_created_at    timestamp not null default current_date,
    c_advantages    text,
    c_disadvantages text,
    c_comment       text
);

create table online_store.t_product_review
(
    id_product bigint not null,
    id_review  bigint not null,
    constraint t_product_review_unique unique (id_product, id_review),
    constraint t_product_review_product_fk foreign key (id_product) references online_store.t_product (id),
    constraint t_product_review_review_fk foreign key (id_review) references online_store.t_review (id)
);

create table online_store.t_customer_review
(
    id_customer uuid   not null,
    id_review   bigint not null,
    constraint t_customer_review_unique unique (id_customer, id_review),
    constraint t_customer_review_customer_fk foreign key (id_customer) references online_store.t_customer (id),
    constraint t_customer_review_review_fk foreign key (id_review) references online_store.t_review (id)
);


