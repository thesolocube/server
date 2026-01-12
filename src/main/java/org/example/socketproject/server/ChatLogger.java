package org.example.socketproject.server;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Système de logging pour enregistrer tous les événements et messages du serveur.
 * Les logs sont sauvegardés via Hibernate.
 */
public class ChatLogger {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static ChatLogger instance;
    
    private ChatLogger() {
    }
    
    public static synchronized ChatLogger getInstance() {
        if (instance == null) {
            instance = new ChatLogger();
        }
        return instance;
    }
    
    /**
     * Enregistre un événement du serveur (démarrage, connexions, déconnexions, erreurs)
     */
    public void logServerEvent(String event) {
        logServerEvent("EVENT", event);
    }
    
    /**
     * Enregistre un événement du serveur avec un type spécifique
     */
    private void logServerEvent(String logType, String event) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format("[%s] %s", timestamp, event);
        
        // Afficher dans la console
        System.out.println(logEntry);
        
        // Enregistrer dans la base de données
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ServerLog log = new ServerLog(logType, event);
            session.persist(log);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Erreur lors de l'enregistrement du log : " + e.getMessage());
        }
    }
    
    /**
     * Enregistre un message échangé entre utilisateurs
     */
    public void logMessage(String username, String message) {
        logMessage(username, message, "PUBLIC", null);
    }
    
    /**
     * Enregistre un message (public ou privé)
     */
    public void logMessage(String username, String message, String messageType, String recipientUsername) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format("[%s] [%s] %s", timestamp, username, message);
        
        // Enregistrer dans la base de données
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Message msg = new Message(username, message, 
                    messageType != null ? messageType : "PUBLIC", 
                    recipientUsername);
            session.persist(msg);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Erreur lors de l'enregistrement du message : " + e.getMessage());
        }
    }
    
    /**
     * Enregistre une erreur
     */
    public void logError(String error) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logEntry = String.format("[%s] [ERROR] %s", timestamp, error);
        
        // Afficher dans la console
        System.err.println(logEntry);
        
        // Enregistrer dans la base de données
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ServerLog log = new ServerLog("ERROR", error);
            session.persist(log);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Erreur lors de l'enregistrement de l'erreur : " + e.getMessage());
        }
    }
    
    public void logConnection(String username, String ipAddress) {
        logServerEvent("CONNECTION", String.format("Connexion : %s depuis %s", username, ipAddress));
    }

    public void logDisconnection(String username) {
        logServerEvent("DISCONNECTION", String.format("Déconnexion : %s", username));
    }

    public void logServerStart(int port) {
        logServerEvent("SERVER_START", String.format("Serveur démarré sur le port %d", port));
    }

    public void logServerStop() {
        logServerEvent("SERVER_STOP", "Serveur arrêté");
    }

    public void close() {
        // HibernateUtil.shutdown(); // On pourrait fermer ici, mais attention si le logger est utilisé lors de la fermeture globale
    }
}