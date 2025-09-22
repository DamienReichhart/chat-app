# Prérequis d'Installation

**Auteurs:** Équipe AP4  
**Date de création:** 03/11/2024  
**Dernière mise à jour:** 03/11/2024

## Sommaire
1. [Environnement de développement](#environnement-de-développement)
2. [Dépendances logicielles](#dépendances-logicielles)
3. [Configuration système requise](#configuration-système-requise)
4. [Installation](#installation)

## Environnement de développement

L'application de chat AP4 a été développée avec les technologies suivantes :

- **Java 21** : Langage de programmation principal
- **Maven 3.6+** : Outil de gestion de dépendances et de construction
- **Git** : Système de contrôle de version

## Dépendances logicielles

### Client (chat-client)
- **JavaFX 21.0.6** : Framework d'interface graphique
- **PostgreSQL 42.7.5** : Système de gestion de base de données relationnelle
- **Log4j 2.24.3** : Bibliothèque de journalisation
- **Jackson 2.16.2** : Bibliothèque de sérialisation/désérialisation JSON
- **Java-WebSocket 1.5.6** : Client WebSocket
- **Tyrus 2.1.5** : Implémentation de WebSocket
- **Reactor-Netty 1.1.18** : Framework réactif pour la communication réseau
- **Hibernate Validator 8.0.1** : Validation des données
- **JUnit/TestNG** : Frameworks de test

### Serveur (chat-server)
- **Spring Boot 3.2.4** : Framework d'application
- **Spring WebSocket** : Support WebSocket 
- **Spring Messaging** : Support pour la messagerie
- **Spring Boot Actuator** : Outils de monitoring et de gestion
- **Log4j 2.24.3** : Bibliothèque de journalisation

### Module commun (chat-common)
- **Spring Security Crypto 6.3.5** : Fonctionnalités de cryptographie
- **Jackson 2.16.2** : Bibliothèque de sérialisation/désérialisation JSON
- **Hibernate Validator 8.0.1** : Validation des données

## Configuration système requise

### Matériel recommandé
- Processeur : Dual-core 2GHz ou supérieur
- Mémoire : 4Go RAM minimum (8Go recommandé)
- Espace disque : 500Mo d'espace libre

### Logiciels requis
- **Système d'exploitation** : Windows 10/11, macOS 10.15+, ou Linux (distribution récente)
- **JDK 21** : Kit de développement Java
- **PostgreSQL 14+** : Serveur de base de données

## Installation

### Installation des prérequis

1. **JDK 21** :
   - Télécharger et installer depuis [Oracle](https://www.oracle.com/java/technologies/downloads/) ou [Adoptium](https://adoptium.net/)
   - Configurer la variable d'environnement JAVA_HOME

2. **PostgreSQL** :
   - Télécharger et installer depuis [PostgreSQL.org](https://www.postgresql.org/download/)
   - Créer une base de données nommée "ap4"
   - Noter les identifiants de connexion (utilisateur, mot de passe)

3. **Maven** (si compilation manuelle nécessaire) :
   - Télécharger depuis [Maven](https://maven.apache.org/download.cgi)
   - Configurer la variable d'environnement M2_HOME

### Configuration de l'application

1. **Configuration de la base de données** :
   - Modifier le fichier `chat-client/src/main/resources/database.properties` avec les paramètres de connexion PostgreSQL

2. **Configuration du serveur WebSocket** :
   - Modifier le fichier `chat-client/src/main/resources/websocket.properties` avec l'URL du serveur WebSocket

### Compilation et exécution

1. **Compiler le projet** :
   ```bash
   mvn clean package
   ```

2. **Lancer le serveur** :
   ```bash
   java -jar chat-server/target/chat-server-1.0-SNAPSHOT.jar
   ```

3. **Lancer le client** :
   ```bash
   java -jar chat-client/target/chat-client-1.0-SNAPSHOT-jar-with-dependencies.jar
   ``` 