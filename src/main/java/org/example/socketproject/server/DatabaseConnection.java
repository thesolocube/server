package org.example.socketproject.server;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @deprecated Cette classe est dépréciée. Utilisez HibernateUtil et les Entités JPA à la place.
 * Elle est conservée pour éviter les erreurs de compilation si d'anciennes références existent,
 * mais elle ne fait plus rien.
 */
@Deprecated
public class DatabaseConnection {
    private static DatabaseConnection instance;

    private DatabaseConnection() {
        // Ne plus charger la configuration ni initialiser la base de données SQL Server
        System.out.println("⚠️ DatabaseConnection est dépréciée. Hibernate est maintenant utilisé.");
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        throw new SQLException("DatabaseConnection est dépréciée. Utilisez HibernateUtil.");
    }
    
    public void close() {
        // Rien à faire
    }
}
