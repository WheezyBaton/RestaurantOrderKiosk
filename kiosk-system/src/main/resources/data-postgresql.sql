CREATE TABLE IF NOT EXISTS order_status_history (id SERIAL PRIMARY KEY, order_id BIGINT NOT NULL, old_status VARCHAR(50), new_status VARCHAR(50) NOT NULL, changed_at TIMESTAMP NOT NULL);

INSERT INTO category (id, name, image_url) VALUES (1, 'Burgery', 'burger.png') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, image_url) VALUES (2, 'Dodatki', 'fries.png') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, name, image_url) VALUES (3, 'Napoje', 'cola.png') ON CONFLICT (id) DO NOTHING;

INSERT INTO ingredient (id, name, price) VALUES (1, 'Bułka Brioche', 0.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (2, 'Wołowina 100%', 5.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (3, 'Kurczak w panierce', 4.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (4, 'Kotlet Roślinny', 6.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (5, 'Ser Cheddar', 2.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (6, 'Bekon', 3.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (7, 'Cebula', 0.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (8, 'Sałata', 0.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (9, 'Pomidor', 0.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (10, 'Ogórek kiszony', 0.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (11, 'Sos Ostry', 1.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (12, 'Majonez', 0.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (13, 'Ketchup', 0.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (14, 'Ziemniaki', 0.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO ingredient (id, name, price) VALUES (15, 'Syrop Cola', 0.00) ON CONFLICT (id) DO NOTHING;

INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (1, 'Classic Burger', 25.00, 'Klasyczna wołowina z warzywami', 'burger.png', 1, true, false) ON CONFLICT (id) DO NOTHING;
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (2, 'Bacon BBQ Master', 32.00, 'Podwójny bekon i ostry sos', 'bbq-bacon-burger.png', 1, true, false) ON CONFLICT (id) DO NOTHING;
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (3, 'Chicken Crunch', 22.00, 'Chrupiący kurczak z majonezem', 'chicken-crunch.png', 1, true, false) ON CONFLICT (id) DO NOTHING;
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (4, 'Vege Delight', 28.00, '100% roślinny, 100% smaku', 'vege-delight.png', 1, true, false) ON CONFLICT (id) DO NOTHING;
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (5, 'Frytki Belgijskie', 12.00, 'Grubo krojone, chrupiące', 'fries.png', 2, true, false) ON CONFLICT (id) DO NOTHING;
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (6, 'Coca-Cola 0.5L', 8.00, 'Zimna i orzeźwiająca', 'cola.png', 3, true, false) ON CONFLICT (id) DO NOTHING;

INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 1, true, 1, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 2, true, 2, 2) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 5, true, 3, 2) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 7, true, 4, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 8, true, 5, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 9, true, 6, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 6, false, 7, 3) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 1, true, 1, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 2, true, 2, 3) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 5, true, 3, 2) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 6, true, 4, 5) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 11, true, 5, 2) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 7, true, 6, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 1, true, 1, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 3, true, 2, 2) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 8, true, 3, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 12, true, 4, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 9, false, 5, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 1, true, 1, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 4, true, 2, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 8, true, 3, 2) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 9, true, 4, 2) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 5, false, 5, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (5, 14, true, 1, 1) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (5, 11, false, 2, 2) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (5, 12, false, 3, 2) ON CONFLICT DO NOTHING;
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (6, 15, true, 1, 1) ON CONFLICT DO NOTHING;

INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (1, 1, CURRENT_DATE + TIME '08:30', 'COMPLETED', 'EAT_IN', 25.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (2, 2, CURRENT_DATE + TIME '09:15', 'COMPLETED', 'TAKE_AWAY', 33.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (3, 3, CURRENT_DATE + TIME '10:45', 'READY', 'EAT_IN', 22.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (4, 4, CURRENT_DATE + TIME '11:20', 'IN_PROGRESS', 'EAT_IN', 12.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (5, 5, CURRENT_DATE + TIME '12:05', 'NEW', 'TAKE_AWAY', 9.00) ON CONFLICT (id) DO NOTHING;

INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (6, 10, CURRENT_DATE - INTERVAL '1 day', 'COMPLETED', 'EAT_IN', 50.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (7, 11, CURRENT_DATE - INTERVAL '2 days', 'COMPLETED', 'TAKE_AWAY', 26.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (8, 12, CURRENT_DATE - INTERVAL '3 days', 'COMPLETED', 'EAT_IN', 25.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (9, 13, CURRENT_DATE - INTERVAL '5 days', 'CANCELLED', 'EAT_IN', 32.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (10, 14, CURRENT_DATE - INTERVAL '7 days', 'COMPLETED', 'EAT_IN', 64.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (11, 15, CURRENT_DATE - INTERVAL '10 days', 'COMPLETED', 'TAKE_AWAY', 13.00) ON CONFLICT (id) DO NOTHING;

INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (12, 40, DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '5 days', 'COMPLETED', 'EAT_IN', 25.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (13, 41, DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '10 days', 'COMPLETED', 'TAKE_AWAY', 45.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (14, 42, DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '15 days', 'COMPLETED', 'EAT_IN', 22.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (15, 43, DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '20 days', 'COMPLETED', 'EAT_IN', 12.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (16, 44, DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '25 days', 'COMPLETED', 'TAKE_AWAY', 33.00) ON CONFLICT (id) DO NOTHING;

INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (17, 1, CURRENT_DATE - INTERVAL '60 days', 'COMPLETED', 'EAT_IN', 100.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (18, 2, CURRENT_DATE - INTERVAL '62 days', 'COMPLETED', 'EAT_IN', 25.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (19, 3, CURRENT_DATE - INTERVAL '65 days', 'COMPLETED', 'TAKE_AWAY', 28.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (20, 4, CURRENT_DATE - INTERVAL '70 days', 'COMPLETED', 'EAT_IN', 50.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (21, 5, CURRENT_DATE - INTERVAL '75 days', 'COMPLETED', 'EAT_IN', 22.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (22, 6, CURRENT_DATE - INTERVAL '80 days', 'COMPLETED', 'TAKE_AWAY', 13.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (23, 7, CURRENT_DATE - INTERVAL '85 days', 'COMPLETED', 'EAT_IN', 25.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (24, 8, CURRENT_DATE - INTERVAL '90 days', 'COMPLETED', 'EAT_IN', 32.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO orders (id, daily_number, created_at, status, type, total_amount) VALUES (25, 9, CURRENT_DATE - INTERVAL '95 days', 'COMPLETED', 'TAKE_AWAY', 8.00) ON CONFLICT (id) DO NOTHING;

INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (1, 1, 1, 1, 25.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (2, 2, 2, 1, 32.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (3, 3, 3, 1, 22.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (4, 4, 5, 1, 12.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (5, 5, 6, 1, 8.00) ON CONFLICT (id) DO NOTHING;
INSERT INTO order_item (id, order_id, product_id, quantity, price_at_purchase) VALUES (6, 6, 1, 2, 25.00) ON CONFLICT (id) DO NOTHING;

SELECT setval('order_item_id_seq', (SELECT MAX(id) FROM order_item));

INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (7, 5, 2, 12.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (8, 1, 1, 25.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (10, 2, 2, 32.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (11, 5, 1, 13.00);

INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (12, 1, 1, 25.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (13, 2, 1, 32.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (13, 5, 1, 13.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (14, 3, 1, 22.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (15, 6, 1, 8.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (15, 5, 1, 4.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (16, 4, 1, 28.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (16, 5, 1, 5.00);

INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (17, 1, 4, 25.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (18, 1, 1, 25.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (19, 4, 1, 28.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (20, 2, 1, 32.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (20, 6, 2, 9.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (21, 3, 1, 22.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (22, 5, 1, 13.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (23, 1, 1, 25.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (24, 2, 1, 32.00);
INSERT INTO order_item (order_id, product_id, quantity, price_at_purchase) VALUES (25, 6, 1, 8.00);

INSERT INTO order_item_modifier (id, order_item_id, ingredient_id, action) VALUES (1, 1, 6, 'ADDED') ON CONFLICT (id) DO NOTHING;

SELECT setval('category_id_seq', (SELECT MAX(id) FROM category));
SELECT setval('ingredient_id_seq', (SELECT MAX(id) FROM ingredient));
SELECT setval('product_id_seq', (SELECT MAX(id) FROM product));
SELECT setval('orders_id_seq', (SELECT MAX(id) FROM orders));
SELECT setval('order_item_id_seq', (SELECT MAX(id) FROM order_item));
SELECT setval('order_item_modifier_id_seq', (SELECT MAX(id) FROM order_item_modifier));
SELECT setval('order_status_history_id_seq', (SELECT COALESCE(MAX(id), 1) FROM order_status_history));