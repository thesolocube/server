package org.example.socketproject.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private boolean registered = false;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // Initialiser les flux de communication
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Lire les identifiants (format: username:password)
            String authData = reader.readLine();
            
            if (authData == null || authData.trim().isEmpty()) {
                writer.println("AUTH_FAILED:Données d'authentification vides");
                ChatLogger.getInstance().logError("Tentative de connexion avec des données vides");
                return;
            }
            
            // Séparer username et password
            String[] authParts = authData.split(":", 2);
            if (authParts.length != 2) {
                writer.println("AUTH_FAILED:Format d'authentification invalide. Format attendu: username:password");
                ChatLogger.getInstance().logError("Format d'authentification invalide reçu");
                return;
            }
            
            username = authParts[0].trim();
            String password = authParts[1].trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                writer.println("AUTH_FAILED:Nom d'utilisateur ou mot de passe vide");
                ChatLogger.getInstance().logError("Tentative de connexion avec username ou password vide");
                return;
            }
            
            // Authentifier l'utilisateur
            UserManager userManager = UserManager.getInstance();
            if (!userManager.authenticate(username, password)) {
                writer.println("AUTH_FAILED:Nom d'utilisateur ou mot de passe incorrect");
                ChatLogger.getInstance().logError("Échec d'authentification pour : " + username);
                return;
            }
            
            // Vérifier l'unicité du pseudo (déjà connecté)
            if (!server.registerUsername(username)) {
                writer.println("AUTH_FAILED:Ce nom d'utilisateur est déjà connecté");
                ChatLogger.getInstance().logError("Tentative de connexion avec un utilisateur déjà connecté : " + username);
                return;
            }
            
            // Authentification réussie
            writer.println("AUTH_SUCCESS");
            registered = true;

            String clientIP = socket.getInetAddress().getHostAddress();
            ChatLogger.getInstance().logConnection(username, clientIP);
            String joinMessage = "👤 " + username + " a rejoint le chat";
            System.out.println(joinMessage);
            server.notifyUserJoined(username);
            server.broadcast(" " + username + " a rejoint le chat", this);
            
            // Envoyer la liste des utilisateurs connectés à tous les clients
            server.broadcastUserList();
            
            // Boucle de réception des messages
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.trim().isEmpty()) {
                    continue;
                }

                // Commandes spéciales
                if (message.equalsIgnoreCase("/quit")) {
                    break;
                }
                
                // Message privé : format /msg username message
                if (message.startsWith("/msg ")) {
                    String[] parts = message.substring(5).split(" ", 2);
                    if (parts.length == 2) {
                        String targetUser = parts[0];
                        String privateMsg = parts[1];
                        if (server.sendPrivateMessage(username, targetUser, privateMsg)) {
                            // Confirmer à l'expéditeur que le message a été envoyé
                            writer.println("✅ Message privé envoyé à " + targetUser);
                        } else {
                            writer.println("❌ Utilisateur '" + targetUser + "' introuvable ou déconnecté");
                        }
                        continue;
                    }
                }

                String logMessage = "[" + username + "] " + message;
                System.out.println(logMessage);
                server.notifyUserMessage(username, message);
                // Le logger sera appelé dans broadcast()
                server.broadcast(username + ": " + message, this);
            }

        } catch (IOException e) {
            ChatLogger.getInstance().logError("Erreur avec le client " + username + " : " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    // Envoyer un message à ce client
    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    // Déconnecter proprement le client
    public void disconnect() {
        try {
            if (registered && username != null) {
                ChatLogger.getInstance().logDisconnection(username);
                String disconnectMsg = "👋 " + username + " s'est déconnecté";
                System.out.println(disconnectMsg);
                server.notifyUserLeft(username);
                server.broadcast(" " + username + " a quitté le chat", this);
                server.unregisterUsername(username);
                // Mettre à jour la liste des utilisateurs
                server.broadcastUserList();
            }

            server.removeClient(this);

            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();

        } catch (IOException e) {
            ChatLogger.getInstance().logError("Erreur lors de la déconnexion de " + username + " : " + e.getMessage());
        }
    }
    
    // Getter pour le username (utilisé par ChatServer pour les logs)
    public String getUsername() {
        return username;
    }
}