-- data.sql (manual import)
INSERT INTO roles(name) VALUES ('ADMINISTRADOR'), ('USUARIO'), ('USUARIO_AVANZADO');
INSERT INTO permissions(name) VALUES ('CATALOG_READ'), ('CATALOG_WRITE'), ('INVENTORY_ASSIGN'), ('USER_MANAGE'), ('ROLE_MANAGE');
-- Note: password hashes must be generated with BCrypt. It's recommended to let the application seed user 'ADMIN' automatically.
-- sample location and people
INSERT IGNORE INTO location(nombre) VALUES('Almacén A'),('Almacén B');
INSERT IGNORE INTO people(codigo, full_name) VALUES('E001','Juan Perez'),('E002','María Gómez');
