insert into online_store.t_review(id, c_rating, c_created_at, c_advantages, c_disadvantages, c_comment)
values (1, 1, '2024-05-16 11:22:00',
        'advantages 1', 'disadvantages 1', 'comment 1'),
       (2, 2, '2024-05-15 12:23:00',
        'advantages 2', 'disadvantages 2', 'comment 2'),
       (3, 3, '2024-05-16 13:24:00',
        'advantages 3', 'disadvantages 3', 'comment 3'),
       (4, 4, '2024-05-17 14:25:00',
        'advantages 4', 'disadvantages 4', 'comment 4');

alter sequence online_store.t_review_id_seq restart with 5;

insert into online_store.t_product (id, c_title, c_price)
values (1, 'Ананас', '100');

insert into online_store.t_product_review(id_product, id_review)
values (1, 1),
       (1, 2),
       (1, 3),
       (1, 4);

insert into online_store.t_customer(id, c_username)
values ('11dcb1eb-54a9-47e4-9fa0-c0cddbd62177', 'Artem');

insert into online_store.t_customer_oidcuser(id_customer, id_oidcuser)
values ('11dcb1eb-54a9-47e4-9fa0-c0cddbd62177', '3828cc4f-15b6-4438-815e-ac0f120c0db5');

insert into online_store.t_customer_review(id_customer, id_review)
values ('11dcb1eb-54a9-47e4-9fa0-c0cddbd62177', 1),
       ('11dcb1eb-54a9-47e4-9fa0-c0cddbd62177', 2),
       ('11dcb1eb-54a9-47e4-9fa0-c0cddbd62177', 3),
       ('11dcb1eb-54a9-47e4-9fa0-c0cddbd62177', 4);