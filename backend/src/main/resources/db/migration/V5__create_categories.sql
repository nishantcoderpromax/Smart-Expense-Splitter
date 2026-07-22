CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE,
                            icon VARCHAR(20)
);

INSERT INTO categories (name, icon) VALUES
                                        ('Food', '🍔'),
                                        ('Travel', '✈️'),
                                        ('Rent', '🏠'),
                                        ('Utilities', '💡'),
                                        ('Entertainment', '🎬'),
                                        ('Shopping', '🛍️'),
                                        ('Groceries', '🛒'),
                                        ('Other', '📦');
