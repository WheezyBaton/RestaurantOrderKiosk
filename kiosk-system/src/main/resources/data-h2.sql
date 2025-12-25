-- KATEGORIE
INSERT INTO category (id, name, image_url) VALUES (1, 'Burgery', 'burger.png');
INSERT INTO category (id, name, image_url) VALUES (2, 'Dodatki', 'burger.png');
INSERT INTO category (id, name, image_url) VALUES (3, 'Napoje', 'burger.png');

-- SKŁADNIKI
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

-- PRODUKTY
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (1, 'Classic Burger', 25.00, 'Klasyczna wołowina z warzywami', 'burger.png', 1, true, false);
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (2, 'Bacon BBQ Master', 32.00, 'Podwójny bekon i ostry sos', 'burger.png', 1, true, false);
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (3, 'Chicken Crunch', 22.00, 'Chrupiący kurczak z majonezem', 'burger.png', 1, true, false);
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (4, 'Vege Delight', 28.00, '100% roślinny, 100% smaku', 'burger.png', 1, true, false);
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (5, 'Frytki Belgijskie', 12.00, 'Grubo krojone, chrupiące', 'burger.png', 2, true, false);
INSERT INTO product (id, name, base_price, description, image_url, category_id, available, deleted) VALUES (6, 'Coca-Cola 0.5L', 8.00, 'Zimna i orzeźwiająca', 'burger.png', 3, true, false);

-- KONFIGURACJA SKŁADNIKÓW PRODUKTÓW
-- Classic Burger
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 1, true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 2, true, 2, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 5, true, 3, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 7, true, 4, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 8, true, 5, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 9, true, 6, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (1, 6, false, 7, 3);

-- Bacon BBQ
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 1, true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 2, true, 2, 3);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 5, true, 3, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 6, true, 4, 5);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 11, true, 5, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (2, 7, true, 6, 1);

-- Chicken
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 1, true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 3, true, 2, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 8, true, 3, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 12, true, 4, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (3, 9, false, 5, 1);

-- Vege
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 1, true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 4, true, 2, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 8, true, 3, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 9, true, 4, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (4, 5, false, 5, 1);

-- Frytki
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (5, 14, true, 1, 1);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (5, 11, false, 2, 2);
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (5, 12, false, 3, 2);

-- Cola
INSERT INTO product_ingredient (product_id, ingredient_id, is_default, display_order, max_quantity) VALUES (6, 15, true, 1, 1);