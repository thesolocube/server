package org.example.socketproject.server;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire des comptes utilisateurs
 * Stocke les usernames et mots de passe via Hibernate
 */
public class UserManager {
    private static UserManager instance;
    private final String adminPassword = "admin123"; // Mot de passe admin par défaut

    private UserManager() {
        initializeAdminUser();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }
    
    /**
     * Initialise le compte administrateur par défaut
     */
    private void initializeAdminUser() {
        if (!userExists("admin")) {
            createUser("admin", adminPassword);
        }
    }

    /**
     * Crée un nouveau compte utilisateur
     */
    public boolean createUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        if (userExists(username)) {
            return false; // L'utilisateur existe déjà
        }
        
        String passwordHash = hashPassword(password);
        User user = new User(username, passwordHash);
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Erreur lors de la création de l'utilisateur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un compte utilisateur
     */
    public boolean deleteUser(String username) {
        if (username == null || username.trim().isEmpty() || username.equals("admin")) {
            return false; // On ne peut pas supprimer l'admin
        }
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Query<?> query = session.createQuery("delete from User where username = :username", null);
            query.setParameter("username", username);
            int result = query.executeUpdate();
            transaction.commit();
            return result > 0;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            System.err.println("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie les identifiants d'un utilisateur
     */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        
        String passwordHash = hashPassword(password);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery("select passwordHash from User where username = :username", String.class);
            query.setParameter("username", username);
            String storedHash = query.uniqueResult();
            
            return storedHash != null && storedHash.equals(passwordHash);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'authentification : " + e.getMessage());
        }
        return false;
    }

    /**
     * Vérifie si un utilisateur existe
     */
    public boolean userExists(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("select count(u) from User u where u.username = :username", Long.class);
            query.setParameter("username", username);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'utilisateur : " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtient tous les utilisateurs (pour l'affichage)
     */
    public Map<String, String> getAllUsers() {
        Map<String, String> users = new HashMap<>();
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> userList = session.createQuery("from User", User.class).list();
            for (User u : userList) {
                users.put(u.getUsername(), u.getPasswordHash());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }
        return users;
    }

    /**
     * Hash simple du mot de passe (pour la sécurité basique)
     * En production, utiliser BCrypt ou Argon2
     */
    private String hashPassword(String password) {
        // Hash simple - à remplacer par un hash sécurisé en production
        return String.valueOf(password.hashCode());
    }
}