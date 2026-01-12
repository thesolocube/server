-- Script SQL pour initialiser la base de données ChatDB
-- Exécutez ce script dans SQL Server Management Studio ou via sqlcmd

-- Créer la base de données si elle n'existe pas
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'ChatDB')
BEGIN
    CREATE DATABASE ChatDB;
END
GO

USE ChatDB;
GO

-- Table des utilisateurs
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND type in (N'U'))
BEGIN
    CREATE TABLE users (
        id INT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL UNIQUE,
        password_hash NVARCHAR(255) NOT NULL,
        created_at DATETIME DEFAULT GETDATE()
    );
    
    -- Créer un compte admin par défaut (mot de passe: admin123)
    INSERT INTO users (username, password_hash) 
    VALUES ('admin', '-1146435500'); -- Hash de "admin123"
END
GO

-- Table des logs serveur
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[server_logs]') AND type in (N'U'))
BEGIN
    CREATE TABLE server_logs (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        log_type NVARCHAR(50) NOT NULL, -- 'EVENT', 'ERROR', 'CONNECTION', 'DISCONNECTION', 'SERVER_START', 'SERVER_STOP'
        message NVARCHAR(MAX) NOT NULL,
        timestamp DATETIME DEFAULT GETDATE()
    );
    
    CREATE INDEX idx_server_logs_timestamp ON server_logs(timestamp);
END
GO

-- Table des messages
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[messages]') AND type in (N'U'))
BEGIN
    CREATE TABLE messages (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) NOT NULL,
        message NVARCHAR(MAX) NOT NULL,
        message_type NVARCHAR(20) DEFAULT 'PUBLIC', -- 'PUBLIC', 'PRIVATE'
        recipient_username NVARCHAR(50) NULL, -- NULL pour les messages publics
        timestamp DATETIME DEFAULT GETDATE()
    );
    
    CREATE INDEX idx_messages_timestamp ON messages(timestamp);
    CREATE INDEX idx_messages_username ON messages(username);
END
GO

PRINT 'Base de données ChatDB initialisée avec succès!';
GO

