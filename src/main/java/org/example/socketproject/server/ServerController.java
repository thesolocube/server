package org.example.socketproject.server;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Set;

/**
 * Contrôleur pour la partie serveur du chat
 * Gère l'interface utilisateur et les interactions avec le serveur
 */
public class ServerController {

    @FXML private TextArea serverLogArea;
    @FXML private Label statusLabel;
    @FXML private TextField portField;
    @FXML private ListView<String> connectedUsersListView;
    @FXML private Label clientCountLabel;
    @FXML private Label userCountLabel;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private ListView<String> allUsersListView;

    private ChatServer chatServer;
    private volatile boolean isRunning = false;
    private ObservableList<String> connectedUsersList = FXCollections.observableArrayList();
    private ObservableList<String> allUsersList = FXCollections.observableArrayList();
    private UserManager userManager;

    @FXML
    public void initialize() {
        statusLabel.setText("● Serveur arrêté");
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        if (portField != null) {
            portField.setText("55555");
        }
        
        if (connectedUsersListView != null) {
            connectedUsersListView.setItems(connectedUsersList);
        }
        
        if (allUsersListView != null) {
            allUsersListView.setItems(allUsersList);
        }
        
        if (clientCountLabel != null) {
            clientCountLabel.setText("Clients: 0");
        }
        
        if (userCountLabel != null) {
            userCountLabel.setText("Total: 0 utilisateur(s)");
        }
        
        // Initialiser le gestionnaire d'utilisateurs
        userManager = UserManager.getInstance();
        refreshAllUsersList();
        
        // Message de bienvenue dans les logs
        appendLog("════════════════════════════════════════");
        appendLog("  Serveur chat Lan ");
        appendLog("════════════════════════════════════════");
        appendLog("Prêt à démarrer. Entrez un port et cliquez sur 'Démarrer'.");
        appendLog("");
    }

    /**
     * Démarre le serveur sur le port spécifié
     */
    @FXML
    public void startServer() {
        if (isRunning) {
            appendLog("Le serveur est déjà en cours d'exécution");
            return;
        }

        int portValue = 55555;
        if (portField != null && !portField.getText().trim().isEmpty()) {
            try {
                portValue = Integer.parseInt(portField.getText().trim());
            } catch (NumberFormatException e) {
                appendLog("Port invalide, utilisation du port 55555");
            }
        }

        final int port = portValue; // Make it final for lambda usage

        statusLabel.setText("● Serveur en cours de démarrage...");
        statusLabel.setStyle("-fx-text-fill: orange;");

        // Démarrer le serveur dans un thread séparé pour ne pas bloquer l'interface
        new Thread(() -> {
            try {
                chatServer = new ChatServer(port);
                chatServer.setServerController(this);
                isRunning = true;
                
                Platform.runLater(() -> {
                    statusLabel.setText("● Serveur actif sur le port " + port);
                    statusLabel.setStyle("-fx-text-fill: #27ae60;");
                    updateClientCount(0);
                });

                chatServer.start();
            } catch (Exception e) {
                isRunning = false;
                Platform.runLater(() -> {
                    statusLabel.setText("● Erreur au démarrage");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    appendLog("❌ Erreur lors du démarrage du serveur : " + e.getMessage());
                });
            }
        }, "server-thread").start();
    }

    /**
     * Arrête le serveur
     */
    @FXML
    public void stopServer() {
        if (!isRunning || chatServer == null) {
            appendLog("⚠ Le serveur n'est pas en cours d'exécution");
            return;
        }

        new Thread(() -> {
            chatServer.stop();
            isRunning = false;
            Platform.runLater(() -> {
                statusLabel.setText("● Serveur arrêté");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                appendLog("🛑 Serveur arrêté");
                connectedUsersList.clear();
                updateClientCount(0);
                if (userCountLabel != null) {
                    userCountLabel.setText("Total: 0 utilisateur(s)");
                }
            });
        }, "server-stop-thread").start();
    }

    /**
     * Ajoute un message dans la zone de logs
     */
    public void appendLog(String message) {
        Platform.runLater(() -> {
            if (serverLogArea != null) {
                String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                serverLogArea.appendText("[" + timestamp + "] " + message + "\n");
                // Auto-scroll vers le bas
                serverLogArea.setScrollTop(Double.MAX_VALUE);
                // Alternative pour assurer le scroll
                serverLogArea.selectEnd();
                serverLogArea.deselect();
            }
        });
    }
    
    /**
     * Efface tous les logs
     */
    @FXML
    public void clearLogs() {
        if (serverLogArea != null) {
            serverLogArea.clear();
            appendLog("Logs effacés.");
        }
    }

    /**
     * Met à jour la liste des utilisateurs connectés
     */
    public void updateConnectedUsers(Set<String> usernames) {
        Platform.runLater(() -> {
            connectedUsersList.clear();
            if (usernames != null) {
                connectedUsersList.addAll(usernames);
            }
            // Mettre à jour le compteur d'utilisateurs
            if (userCountLabel != null) {
                int count = usernames != null ? usernames.size() : 0;
                userCountLabel.setText("Total: " + count + (count <= 1 ? " utilisateur" : " utilisateurs"));
            }
        });
    }
    
    /**
     * Met à jour le compteur de clients connectés
     */
    public void updateClientCount(int count) {
        Platform.runLater(() -> {
            if (clientCountLabel != null) {
                clientCountLabel.setText("Clients: " + count);
            }
        });
    }

    /**
     * Méthode appelée lors de la fermeture de l'application
     */
    public void shutdown() {
        if (isRunning && chatServer != null) {
            stopServer();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Crée un nouveau compte utilisateur
     */
    @FXML
    public void createUserAccount() {
        if (newUsernameField == null || newPasswordField == null) {
            return;
        }
        
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            appendLog("⚠ Veuillez remplir tous les champs pour créer un compte");
            return;
        }
        
        if (userManager.createUser(username, password)) {
            appendLog("✅ Compte créé avec succès : " + username);
            newUsernameField.clear();
            newPasswordField.clear();
            refreshAllUsersList();
        } else {
            if (userManager.userExists(username)) {
                appendLog("❌ Le nom d'utilisateur '" + username + "' existe déjà");
            } else {
                appendLog("❌ Erreur lors de la création du compte");
            }
        }
    }
    
    /**
     * Supprime le compte utilisateur sélectionné
     */
    @FXML
    public void deleteSelectedUser() {
        if (allUsersListView == null) {
            return;
        }
        
        String selectedUser = allUsersListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            appendLog("⚠ Veuillez sélectionner un utilisateur à supprimer");
            return;
        }
        
        if (selectedUser.equals("admin")) {
            appendLog("❌ Impossible de supprimer le compte administrateur");
            return;
        }
        
        if (userManager.deleteUser(selectedUser)) {
            appendLog("✅ Compte supprimé : " + selectedUser);
            refreshAllUsersList();
        } else {
            appendLog("❌ Erreur lors de la suppression du compte");
        }
    }
    
    /**
     * Rafraîchit la liste de tous les utilisateurs
     */
    private void refreshAllUsersList() {
        Platform.runLater(() -> {
            allUsersList.clear();
            allUsersList.addAll(userManager.getAllUsers().keySet());
        });
    }
}

