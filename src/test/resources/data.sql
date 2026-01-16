-- This file populates the H2 in-memory database for testing.
-- IDs are auto-generated, so we assume they start at 1.

-- 1. Create Users
-- Password for both is "password" (hashed with BCrypt)
INSERT INTO users (username, email, password, auth_provider)
VALUES ('testuser', 'user@example.com', '$2a$10$N.yCy05wOWAY3QjB1S.ORejrhd1uf4/aU6Fw4.i2.t.E.aL9.g.E.', 'LOCAL');

INSERT INTO users (username, email, password, auth_provider)
VALUES ('adminuser', 'admin@example.com', '$2a$10$N.yCy05wOWAY3QjB1S.ORejrhd1uf4/aU6Fw4.i2.t.E.aL9.g.E.', 'LOCAL');

-- 2. Assign Roles
-- User (ID 1) gets ROLE_USER
INSERT INTO user_roles (user_id, role)
VALUES (1, 'ROLE_USER');
-- Admin (ID 2) gets ROLE_ADMIN and ROLE_USER
INSERT INTO user_roles (user_id, role)
VALUES (2, 'ROLE_ADMIN');
INSERT INTO user_roles (user_id, role)
VALUES (2, 'ROLE_USER');

-- 3. Create a Movie
INSERT INTO movies (title, description, language, genre, duration, poster_url)
VALUES ('Inception', 'A mind-bending thriller.', 'English', 'Sci-Fi', 148, 'http://example.com/poster.jpg');

-- 4. Create a Theater
INSERT INTO theaters (name, address, city, latitude, longitude)
VALUES ('PVR Icon', '123 Main St', 'Mumbai', 19.0760, 72.8777);

-- 5. Create a Screen
-- (links to Theater with ID 1)
INSERT INTO screens (name, screen_type, theater_id)
VALUES ('Audi 1', 'IMAX', 1);

-- 6. Create Seat Templates
-- (links to Screen with ID 1)
INSERT INTO seats (seat_number, seat_type, screen_id)
VALUES ('A1', 'REGULAR', 1);
INSERT INTO seats (seat_number, seat_type, screen_id)
VALUES ('A2', 'REGULAR', 1);
INSERT INTO seats (seat_number, seat_type, screen_id)
VALUES ('B1', 'PREMIUM', 1);
INSERT INTO seats (seat_number, seat_type, screen_id)
VALUES ('B2', 'PREMIUM', 1);

-- 7. Create a Show
-- (links to Movie ID 1 and Screen ID 1)
INSERT INTO shows (start_time, end_time, movie_id, screen_id)
VALUES ('2025-12-01 19:00:00', '2025-12-01 21:30:00', 1, 1);

-- 8. Create the ShowSeat Inventory
-- (links to Show ID 1 and Seat IDs 1, 2, 3, 4)
INSERT INTO show_seats (status, price, show_id, seat_id, booking_id)
VALUES ('AVAILABLE', 150.00, 1, 1, NULL);
INSERT INTO show_seats (status, price, show_id, seat_id, booking_id)
VALUES ('AVAILABLE', 150.00, 1, 2, NULL);
INSERT INTO show_seats (status, price, show_id, seat_id, booking_id)
VALUES ('AVAILABLE', 250.00, 1, 3, NULL);
INSERT INTO show_seats (status, price, show_id, seat_id, booking_id)
VALUES ('AVAILABLE', 250.00, 1, 4, NULL);