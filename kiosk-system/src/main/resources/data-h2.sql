DROP TABLE IF EXISTS order_status_history;

CREATE TABLE IF NOT EXISTS order_status_history (id SERIAL PRIMARY KEY, order_id BIGINT NOT NULL, old_status VARCHAR(50), new_status VARCHAR(50) NOT NULL, changed_at TIMESTAMP NOT NULL);

INSERT INTO category (id, name, image_url) VALUES (1, 'Burgery', 'burger.png');
INSERT INTO category (id, name, image_url) VALUES (2, 'Dodatki', 'fries.png');
INSERT INTO category (id, name, image_url) VALUES (3, 'Napoje', 'cola.png');

INSERT INTO ingredient (id, name, price) VALUES (1, 'Bułka Brioche', 0.00);
INSERT INTO ingredient (id, name, price) VALUES (2, 'Wołowina 100%', 5.00);
INSERT INTO ingredient (id, name, price) VALUES (3, 'Kurczak w panierce', 4.00);
INSERT INTO ingredient (id, name, price) VALUES (4, 'Kotlet Roślinny', 6.00);
INSERT INTO ingredient (id, name, price) VALUES (5, 'Ser Cheddar', 2.00);
INSERT INTO ingredient (id, name, price) VALUES (6, 'Bekon', 3.00);
INSERT INTO ingredient (id, name, price) VALUES (7, 'Cebula', 0.00);
INSERT INTO ingredient (id, name, price) VALUES (8, 'Sałata', 0.00);
INSERT INTO ingredient (id, name, price) VALUES (9, 'Pomidor', 0.00);
INSERT INTO ingredient (id, name, price) VALUES (10, 'Ogórek kiszony', 0.00);
INSERT INTO ingredient (id, name, price) VALUES (11, 'Sos Ostry', 1.00);
INSERT INTO ingredient (id, name, price) VALUES (12, 'Majonez', 0.00);
INSERT INTO ingredient (id, name, price) VALUES (13, 'Ketchup', 0.00);
INSERT INTO ingredient (id, name, price) VALUES (14, 'Ziemniaki', 0.00);
INSERT INTO ingredient (id, name, price) VALUES (15, 'Syrop Cola', 0.00);

INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (1, 'Classic Burger', 25.00, 'Klasyczna wołowina z warzywami', 'burger.png', 1, true, false);
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (2, 'Bacon BBQ Master', 32.00, 'Podwójny bekon i ostry sos', 'bbq-bacon-burger.png', 1, true, false);
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (3, 'Chicken Crunch', 22.00, 'Chrupiący kurczak z majonezem', 'chicken-crunch.png', 1, true, false);
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (4, 'Vege Delight', 28.00, '100% roślinny, 100% smaku', 'vege-delight.png', 1, true, false);
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (5, 'Frytki Belgijskie', 12.00, 'Grubo krojone, chrupiące', 'fries.png', 2, true, false);
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (6, 'Coca-Cola 0.5L', 8.00, 'Zimna i orzeźwiająca', 'cola.png', 3, true, false);

INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 1, true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 2, true, 2, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 5, true, 3, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 7, true, 4, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 8, true, 5, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 9, true, 6, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 6, false, 7, 3);

INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 1, true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 2, true, 2, 3);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 5, true, 3, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 6, true, 4, 5);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 11, true, 5, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 7, true, 6, 1);

INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 1, true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 3, true, 2, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 8, true, 3, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 12, true, 4, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 9, false, 5, 1);

INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 1, true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 4, true, 2, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 8, true, 3, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 9, true, 4, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 5, false, 5, 1);

INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (5, 14, true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (5, 11, false, 2, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (5, 12, false, 3, 2);

INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (6, 15, true, 1, 1);

INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (1, 1, CURRENT_TIMESTAMP, 'COMPLETED', 'EAT_IN', 25.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (2, 2, CURRENT_TIMESTAMP, 'COMPLETED', 'TAKE_AWAY', 33.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (3, 3, CURRENT_TIMESTAMP, 'READY', 'EAT_IN', 22.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (4, 4, CURRENT_TIMESTAMP, 'IN_PROGRESS', 'EAT_IN', 12.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (5, 5, CURRENT_TIMESTAMP, 'NEW', 'TAKE_AWAY', 9.00);

INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (10, 10, DATEADD('DAY', -1, CURRENT_DATE), 'COMPLETED', 'EAT_IN', 50.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (11, 11, DATEADD('DAY', -2, CURRENT_DATE), 'COMPLETED', 'TAKE_AWAY', 26.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (12, 12, DATEADD('DAY', -3, CURRENT_DATE), 'COMPLETED', 'EAT_IN', 25.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (13, 13, DATEADD('DAY', -5, CURRENT_DATE), 'CANCELLED', 'EAT_IN', 32.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (14, 14, DATEADD('DAY', -7, CURRENT_DATE), 'COMPLETED', 'EAT_IN', 64.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (15, 15, DATEADD('DAY', -10, CURRENT_DATE), 'COMPLETED', 'TAKE_AWAY', 13.00);

INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (40, 40, DATEADD('DAY', -5, DATEADD('MONTH', -1, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 25.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (41, 41, DATEADD('DAY', -10, DATEADD('MONTH', -1, CURRENT_DATE)), 'COMPLETED', 'TAKE_AWAY', 45.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (42, 42, DATEADD('DAY', -15, DATEADD('MONTH', -1, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 22.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (43, 43, DATEADD('DAY', -20, DATEADD('MONTH', -1, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 12.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (44, 44, DATEADD('DAY', -25, DATEADD('MONTH', -1, CURRENT_DATE)), 'COMPLETED', 'TAKE_AWAY', 33.00);

INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (101, 1, DATEADD('DAY', -1, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 100.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (102, 2, DATEADD('DAY', -2, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 25.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (103, 3, DATEADD('DAY', -3, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'TAKE_AWAY', 28.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (104, 4, DATEADD('DAY', -5, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 50.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (105, 5, DATEADD('DAY', -10, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 22.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (106, 6, DATEADD('DAY', -15, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'TAKE_AWAY', 13.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (107, 7, DATEADD('DAY', -20, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 25.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (108, 8, DATEADD('DAY', -25, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'EAT_IN', 32.00);
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (109, 9, DATEADD('DAY', -28, DATEADD('MONTH', -3, CURRENT_DATE)), 'COMPLETED', 'TAKE_AWAY', 8.00);

INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (1, 1, 1, 1, 25.00);
INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (2, 2, 2, 1, 32.00);
INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (3, 3, 3, 1, 22.00);
INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (4, 4, 5, 1, 12.00);
INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (5, 5, 6, 1, 8.00);
INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (6, 101, 1, 2, 25.00);

ALTER TABLE order_item ALTER COLUMN id RESTART WITH 7;

INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (10, 1, 2, 25.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (11, 5, 1, 12.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (11, 6, 1, 8.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (12, 1, 1, 25.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (14, 2, 2, 32.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (15, 5, 1, 13.00);

INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (40, 1, 1, 25.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (41, 2, 1, 32.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (41, 5, 1, 13.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (42, 3, 1, 22.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (43, 6, 2, 8.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (44, 4, 1, 28.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (44, 5, 1, 5.00);

INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (102, 1, 1, 25.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (103, 4, 1, 28.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (104, 2, 1, 32.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (104, 6, 2, 9.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (105, 3, 1, 22.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (106, 5, 1, 13.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (107, 1, 1, 25.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (108, 2, 1, 32.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (109, 6, 1, 8.00);

INSERT INTO order_item_modifier (id, order_item_id, ingredient_id, action) VALUES (1, 1, 6, 'ADDED');

ALTER TABLE category ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM category);
ALTER TABLE ingredient ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM ingredient);
ALTER TABLE product ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM product);
ALTER TABLE orders ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM orders);
ALTER TABLE order_item ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM order_item);
ALTER TABLE order_item_modifier ALTER COLUMN id RESTART WITH (SELECT MAX(id) + 1 FROM order_item_modifier);
ALTER TABLE order_status_history ALTER COLUMN id RESTART WITH 1;