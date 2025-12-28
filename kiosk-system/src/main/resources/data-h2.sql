INSERT INTO category (name, image_url) VALUES ('Burgery', 'burger.png');
INSERT INTO category (name, image_url) VALUES ('Dodatki', 'burger.png');
INSERT INTO category (name, image_url) VALUES ('Napoje', 'burger.png');

INSERT INTO ingredient (name, price) VALUES ('Bułka Brioche', 0.00);
INSERT INTO ingredient (name, price) VALUES ('Wołowina 100%', 5.00);
INSERT INTO ingredient (name, price) VALUES ('Kurczak w panierce', 4.00);
INSERT INTO ingredient (name, price) VALUES ('Kotlet Roślinny', 6.00);
INSERT INTO ingredient (name, price) VALUES ('Ser Cheddar', 2.00);
INSERT INTO ingredient (name, price) VALUES ('Bekon', 3.00);
INSERT INTO ingredient (name, price) VALUES ('Cebula', 0.00);
INSERT INTO ingredient (name, price) VALUES ('Sałata', 0.00);
INSERT INTO ingredient (name, price) VALUES ('Pomidor', 0.00);
INSERT INTO ingredient (name, price) VALUES ('Ogórek kiszony', 0.00);
INSERT INTO ingredient (name, price) VALUES ('Sos Ostry', 1.00);
INSERT INTO ingredient (name, price) VALUES ('Majonez', 0.00);
INSERT INTO ingredient (name, price) VALUES ('Ketchup', 0.00);
INSERT INTO ingredient (name, price) VALUES ('Ziemniaki', 0.00);
INSERT INTO ingredient (name, price) VALUES ('Syrop Cola', 0.00);

INSERT INTO product (name, base_price, description, image_url, category_id, available, deleted)
VALUES ('Classic Burger', 25.00, 'Klasyczna wołowina z warzywami', 'burger.png', (SELECT id FROM category WHERE name='Burgery'), true, false);
INSERT INTO product (name, base_price, description, image_url, category_id, available, deleted)
VALUES ('Bacon BBQ Master', 32.00, 'Podwójny bekon i ostry sos', 'burger.png', (SELECT id FROM category WHERE name='Burgery'), true, false);
INSERT INTO product (name, base_price, description, image_url, category_id, available, deleted)
VALUES ('Chicken Crunch', 22.00, 'Chrupiący kurczak z majonezem', 'burger.png', (SELECT id FROM category WHERE name='Burgery'), true, false);
INSERT INTO product (name, base_price, description, image_url, category_id, available, deleted)
VALUES ('Vege Delight', 28.00, '100% roślinny, 100% smaku', 'burger.png', (SELECT id FROM category WHERE name='Burgery'), true, false);
INSERT INTO product (name, base_price, description, image_url, category_id, available, deleted)
VALUES ('Frytki Belgijskie', 12.00, 'Grubo krojone, chrupiące', 'burger.png', (SELECT id FROM category WHERE name='Dodatki'), true, false);
INSERT INTO product (name, base_price, description, image_url, category_id, available, deleted)
VALUES ('Coca-Cola 0.5L', 8.00, 'Zimna i orzeźwiająca', 'burger.png', (SELECT id FROM category WHERE name='Napoje'), true, false);

INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES ((SELECT id FROM product WHERE name='Classic Burger'), (SELECT id FROM ingredient WHERE name='Bułka Brioche'), true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES ((SELECT id FROM product WHERE name='Classic Burger'), (SELECT id FROM ingredient WHERE name='Wołowina 100%'), true, 2, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES ((SELECT id FROM product WHERE name='Classic Burger'), (SELECT id FROM ingredient WHERE name='Ser Cheddar'), true, 3, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES ((SELECT id FROM product WHERE name='Classic Burger'), (SELECT id FROM ingredient WHERE name='Bekon'), false, 7, 3);

INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (101, CURRENT_TIMESTAMP(), 'COMPLETED', 'EAT_IN', 25.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (102, CURRENT_TIMESTAMP(), 'COMPLETED', 'TAKE_AWAY', 33.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (103, CURRENT_TIMESTAMP(), 'READY', 'EAT_IN', 22.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (104, CURRENT_TIMESTAMP(), 'IN_PROGRESS', 'EAT_IN', 12.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (105, CURRENT_TIMESTAMP(), 'NEW', 'TAKE_AWAY', 9.00);

INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (10, DATEADD('DAY', -1, CURRENT_DATE), 'COMPLETED', 'EAT_IN', 50.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (11, DATEADD('DAY', -2, CURRENT_DATE), 'COMPLETED', 'TAKE_AWAY', 26.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (12, DATEADD('DAY', -3, CURRENT_DATE), 'COMPLETED', 'EAT_IN', 25.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (13, DATEADD('DAY', -5, CURRENT_DATE), 'CANCELLED', 'EAT_IN', 32.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (14, DATEADD('DAY', -7, CURRENT_DATE), 'COMPLETED', 'EAT_IN', 64.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (15, DATEADD('DAY', -10, CURRENT_DATE), 'COMPLETED', 'TAKE_AWAY', 13.00);

INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (40, DATEADD('MONTH', -1, CURRENT_DATE), 'COMPLETED', 'EAT_IN', 25.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (41, DATEADD('DAY', -5, DATEADD('MONTH', -1, CURRENT_DATE)), 'COMPLETED', 'TAKE_AWAY', 45.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (42, DATEADD('DAY', -10, DATEADD('MONTH', -1, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 22.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (43, DATEADD('DAY', -15, DATEADD('MONTH', -1, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 12.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (44, DATEADD('DAY', -20, DATEADD('MONTH', -1, CURRENT_DATE)), 'COMPLETED', 'TAKE_AWAY', 33.00);

INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (1, DATEADD('MONTH', -2, CURRENT_DATE), 'COMPLETED', 'EAT_IN', 100.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (2, DATEADD('DAY', -2, DATEADD('MONTH', -2, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 25.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (3, DATEADD('MONTH', -3, CURRENT_DATE), 'COMPLETED', 'TAKE_AWAY', 28.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (4, DATEADD('DAY', -5, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 50.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (5, DATEADD('DAY', -10, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 22.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (6, DATEADD('DAY', -15, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'TAKE_AWAY', 13.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (7, DATEADD('DAY', -20, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 25.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (8, DATEADD('DAY', -25, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 32.00);
INSERT INTO orders (daily_number, created_at, status, type, total_amount) VALUES (9, DATEADD('DAY', -28, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'TAKE_AWAY', 8.00);

INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase)
VALUES ((SELECT id FROM orders WHERE daily_number=101 LIMIT 1), (SELECT id FROM product WHERE name='Classic Burger'), 1, 25.00);

INSERT INTO order_item_modifier (order_item_id, ingredient_id, action)
VALUES ((SELECT id FROM order_item WHERE price_at_purchase=25.00 LIMIT 1), (SELECT id FROM ingredient WHERE name='Bekon'), 'ADDED');