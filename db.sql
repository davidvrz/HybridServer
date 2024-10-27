-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS HybridServer;

-- Usar la base de datos
USE HybridServer;

-- Crear la tabla de páginas (ejemplo de tabla)
CREATE TABLE Pages (
    uuid INT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL
);

-- Crear un usuario y darle permisos
CREATE USER 'hybridserver'@'localhost' IDENTIFIED BY 'hspass';

-- Conceder permisos al usuario para acceder a la base de datos
GRANT ALL PRIVILEGES ON HybridServerDB.* TO 'hybridserver'@'localhost';

-- Aplicar los cambios
FLUSH PRIVILEGES;
