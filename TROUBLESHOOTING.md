# Guide de Dépannage - Connexion à la Base de Données

## Problèmes Courants et Solutions

### 1. Erreur : "Login failed for user"

**Cause** : Nom d'utilisateur ou mot de passe incorrect

**Solution** :
1. Vérifiez le fichier `database.properties`
2. Assurez-vous que `db.username` et `db.password` sont corrects
3. Testez la connexion avec SQL Server Management Studio

**Exemple de configuration** :
```properties
db.server=ATLAS\\SQLEXPRESS
db.port=1433
db.name=ChatDB
db.username=sa
db.password=VotreMotDePasse
```

### 2. Erreur : "The TCP/IP connection to the host has failed"

**Cause** : SQL Server n'est pas accessible ou le port est incorrect

**Solutions** :
1. Vérifiez que SQL Server est démarré :
   - Services Windows → SQL Server (MSSQLSERVER) ou SQL Server (SQLEXPRESS)
   - Ou via SQL Server Configuration Manager

2. Vérifiez que TCP/IP est activé :
   - SQL Server Configuration Manager
   - Configuration du réseau SQL Server
   - Protocoles pour [INSTANCE]
   - Activez TCP/IP et redémarrez SQL Server

3. Vérifiez le port :
   - Par défaut : 1433
   - Pour SQLEXPRESS : peut être un port dynamique
   - Vérifiez dans SQL Server Configuration Manager → TCP/IP → Propriétés → Port IP

### 3. Erreur : "Cannot open database"

**Cause** : La base de données n'existe pas encore

**Solution** :
- C'est normal au premier démarrage
- L'application créera automatiquement la base de données
- Ou exécutez manuellement le script `init-database.sql`

### 4. Erreur avec l'instance (ATLAS\SQLEXPRESS)

**Cause** : Format incorrect ou instance introuvable

**Solutions** :
1. Vérifiez le format dans `database.properties` :
   ```properties
   db.server=ATLAS\\SQLEXPRESS
   ```
   Note : Double backslash `\\` dans le fichier properties

2. Vérifiez que l'instance existe :
   ```sql
   -- Dans SQL Server Management Studio
   SELECT @@SERVERNAME;
   ```

3. Si vous utilisez une instance nommée, vous pouvez ne pas spécifier le port :
   ```properties
   db.server=ATLAS\\SQLEXPRESS
   # Pas besoin de db.port pour une instance nommée
   ```

### 5. Test de Connexion

Utilisez l'utilitaire de test fourni :

**Dans PowerShell :**
```powershell
cd server
.\test-connection.bat
```

**Dans CMD :**
```cmd
cd server
test-connection.bat
```

Ou manuellement :
```bash
mvn exec:java -Dexec.mainClass="org.example.socketproject.server.TestDatabaseConnection"
```

### 6. Vérification Manuelle avec SQL Server Management Studio

1. Ouvrez SQL Server Management Studio
2. Connectez-vous avec :
   - Type de serveur : Moteur de base de données
   - Nom du serveur : `ATLAS\SQLEXPRESS` (ou votre instance)
   - Authentification : SQL Server Authentication
   - Login : `sa` (ou votre utilisateur)
   - Password : votre mot de passe

3. Si la connexion réussit, les paramètres sont corrects

### 7. Configuration Recommandée

Pour une instance SQL Server Express locale :

```properties
# Configuration de la base de données SQL Server
db.server=localhost\\SQLEXPRESS
# ou
db.server=ATLAS\\SQLEXPRESS
db.port=1433
db.name=ChatDB
db.username=sa
db.password=VotreMotDePasse
```

### 8. Vérifier les Services SQL Server

```powershell
# Vérifier si SQL Server est démarré
Get-Service | Where-Object {$_.Name -like "*SQL*"}

# Démarrer SQL Server Express
Start-Service "MSSQL$SQLEXPRESS"
```

### 9. Ports et Firewall

Si vous avez des problèmes de connexion réseau :

1. Vérifiez que le port SQL Server n'est pas bloqué par le firewall
2. Par défaut : port 1433
3. Pour SQLEXPRESS, vérifiez le port dans SQL Server Configuration Manager

### 10. Logs d'Erreur

Les erreurs détaillées sont affichées dans la console au démarrage du serveur.
Recherchez les messages commençant par :
- `❌ Erreur lors de l'initialisation de la base de données`
- `💡 AIDE :`

## Support

Si le problème persiste :
1. Exécutez `test-connection.bat` et copiez la sortie complète
2. Vérifiez les logs dans la console
3. Vérifiez la configuration dans `database.properties`
