-- data.sql (manual import)
INSERT INTO roles(name) VALUES ('ADMINISTRATOR'), ('USER'), ('ADVANCED_USER');
INSERT INTO permissions(name) VALUES ('CATALOG_READ'), ('CATALOG_WRITE'), ('INVENTORY_ASSIGN'), ('USER_MANAGE'), ('ROLE_MANAGE');
INSERT INTO people_type (nombre) VALUES ('Employee'), ('Tourist'), ('Guest');