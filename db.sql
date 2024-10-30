-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS hstestdb;

-- Usar la base de datos
USE hstestdb;

-- Crear la tabla de páginas (ejemplo de tabla)
CREATE TABLE HTML  (
    uuid CHAR(36) PRIMARY KEY,
    content TEXT NOT NULL
);

-- Crear un usuario y darle permisos
CREATE USER 'hsdb '@'localhost' IDENTIFIED BY 'hsdbpass';

-- Conceder permisos al usuario para acceder a la base de datos
GRANT ALL PRIVILEGES ON HybridServerDB.* TO 'hsdb'@'localhost';

-- Aplicar los cambios
FLUSH PRIVILEGES;
