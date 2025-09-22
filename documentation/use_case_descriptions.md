# Descriptions Textuelles des Cas d'Utilisation

**Auteurs:** Équipe AP4  
**Date de création:** 03/11/2024  
**Dernière mise à jour:** 03/11/2024

## Sommaire
1. [Introduction](#introduction)
2. [Gestion des utilisateurs](#gestion-des-utilisateurs)
3. [Gestion des conversations](#gestion-des-conversations)
4. [Messagerie](#messagerie)
5. [Administration](#administration)

## Introduction

Ce document détaille les principaux cas d'utilisation du système de chat AP4. Pour chaque cas d'utilisation, nous présentons les préconditions, le scénario nominal, les enchaînements alternatifs et/ou d'erreur, ainsi que les postconditions.

## Gestion des utilisateurs

### Cas d'utilisation : S'inscrire

**Acteurs** : Utilisateur non authentifié

**Préconditions** : 
- L'utilisateur n'a pas de compte dans le système
- L'utilisateur a accès à l'application cliente

**Scénario nominal** :
1. L'utilisateur lance l'application client
2. L'utilisateur sélectionne "S'inscrire"
3. L'utilisateur saisit ses informations (nom d'utilisateur, mot de passe, email)
4. Le système vérifie la validité des informations :
   - Le nom d'utilisateur n'est pas déjà utilisé
   - Le mot de passe respecte les critères de sécurité
   - L'email a un format valide
5. Le système crée un nouveau compte utilisateur
6. Le système redirige l'utilisateur vers l'écran de connexion

**Enchaînements alternatifs** :
- A4.1 : Le nom d'utilisateur est déjà pris
  1. Le système affiche un message d'erreur
  2. Retour à l'étape 3 du scénario nominal
- A4.2 : Le mot de passe ne respecte pas les critères de sécurité
  1. Le système affiche les critères de sécurité non respectés
  2. Retour à l'étape 3 du scénario nominal
- A4.3 : L'email a un format invalide
  1. Le système affiche un message d'erreur
  2. Retour à l'étape 3 du scénario nominal

**Postconditions** : 
- Un nouveau compte utilisateur est créé dans la base de données
- Le mot de passe est stocké de façon sécurisée (hashé)

### Cas d'utilisation : Se connecter

**Acteurs** : Utilisateur inscrit

**Préconditions** : 
- L'utilisateur possède un compte dans le système
- L'utilisateur n'est pas déjà connecté

**Scénario nominal** :
1. L'utilisateur lance l'application client
2. L'utilisateur entre son nom d'utilisateur et son mot de passe
3. Le système vérifie les informations d'identification
4. Le système authentifie l'utilisateur
5. Le système charge l'interface principale avec les conversations de l'utilisateur

**Enchaînements alternatifs** :
- A3.1 : Nom d'utilisateur ou mot de passe incorrect
  1. Le système affiche un message d'erreur
  2. L'utilisateur peut réessayer ou sélectionner "Mot de passe oublié"
- A3.2 : Le compte utilisateur est bloqué
  1. Le système affiche un message indiquant que le compte est bloqué
  2. L'utilisateur est invité à contacter l'administrateur

**Postconditions** : 
- L'utilisateur est authentifié
- Une session est créée pour l'utilisateur
- L'utilisateur accède à l'interface principale de l'application

### Cas d'utilisation : Gérer son profil

**Acteurs** : Utilisateur standard

**Préconditions** : 
- L'utilisateur est authentifié

**Scénario nominal** :
1. L'utilisateur accède à la section "Profil"
2. L'utilisateur modifie ses informations personnelles (nom d'affichage, email, mot de passe)
3. L'utilisateur confirme les modifications
4. Le système vérifie la validité des nouvelles informations
5. Le système enregistre les modifications

**Enchaînements alternatifs** :
- A4.1 : Les informations saisies ne sont pas valides
  1. Le système affiche un message d'erreur spécifique au problème rencontré
  2. Retour à l'étape 2 du scénario nominal

**Postconditions** : 
- Les informations du profil utilisateur sont mises à jour dans la base de données

## Gestion des conversations

### Cas d'utilisation : Créer une conversation individuelle

**Acteurs** : Utilisateur standard

**Préconditions** : 
- L'utilisateur est authentifié
- L'utilisateur connaît le nom d'utilisateur du destinataire

**Scénario nominal** :
1. L'utilisateur sélectionne l'option "Nouvelle conversation"
2. L'utilisateur choisit "Conversation individuelle"
3. L'utilisateur recherche et sélectionne un contact
4. Le système vérifie que le contact existe
5. Le système crée une nouvelle conversation entre les deux utilisateurs
6. Le système affiche l'interface de conversation

**Enchaînements alternatifs** :
- A4.1 : Le contact n'existe pas
  1. Le système affiche un message indiquant que l'utilisateur n'existe pas
  2. Retour à l'étape 3 du scénario nominal
- A4.2 : Une conversation individuelle existe déjà avec ce contact
  1. Le système redirige vers la conversation existante

**Postconditions** : 
- Une nouvelle conversation individuelle est créée
- Les deux utilisateurs sont ajoutés comme participants
- L'interface de conversation est affichée

### Cas d'utilisation : Créer un groupe

**Acteurs** : Utilisateur standard

**Préconditions** : 
- L'utilisateur est authentifié

**Scénario nominal** :
1. L'utilisateur sélectionne l'option "Nouvelle conversation"
2. L'utilisateur choisit "Nouveau groupe"
3. L'utilisateur saisit un nom et une description pour le groupe
4. L'utilisateur recherche et ajoute des participants
5. L'utilisateur confirme la création du groupe
6. Le système crée le groupe et attribue le rôle d'administrateur au créateur
7. Le système affiche l'interface du groupe

**Enchaînements alternatifs** :
- A3.1 : Le nom du groupe est invalide ou déjà utilisé
  1. Le système affiche un message d'erreur
  2. Retour à l'étape 3 du scénario nominal
- A4.1 : Un utilisateur recherché n'existe pas
  1. Le système affiche un message d'erreur
  2. L'utilisateur peut continuer à ajouter d'autres participants

**Postconditions** : 
- Un nouveau groupe est créé
- Le créateur est défini comme administrateur
- Les participants sélectionnés sont ajoutés au groupe
- L'interface du groupe est affichée

### Cas d'utilisation : Rejoindre un canal

**Acteurs** : Utilisateur standard

**Préconditions** : 
- L'utilisateur est authentifié
- Le canal existe et est accessible à l'utilisateur

**Scénario nominal** :
1. L'utilisateur sélectionne l'option "Rejoindre un canal"
2. Le système affiche la liste des canaux disponibles
3. L'utilisateur sélectionne un canal
4. Le système vérifie les autorisations d'accès
5. Le système ajoute l'utilisateur comme participant au canal
6. Le système affiche l'interface du canal

**Enchaînements alternatifs** :
- A4.1 : L'utilisateur n'a pas les autorisations nécessaires
  1. Le système affiche un message indiquant que l'accès est restreint
  2. Retour à l'étape 2 du scénario nominal

**Postconditions** : 
- L'utilisateur est ajouté comme participant au canal
- L'utilisateur a accès aux messages du canal
- L'interface du canal est affichée

## Messagerie

### Cas d'utilisation : Envoyer un message

**Acteurs** : Utilisateur standard

**Préconditions** : 
- L'utilisateur est authentifié
- L'utilisateur a accès à une conversation

**Scénario nominal** :
1. L'utilisateur sélectionne une conversation
2. L'utilisateur saisit un message dans la zone de texte
3. L'utilisateur envoie le message
4. Le système vérifie les droits d'accès à la conversation
5. Le système transmet le message au serveur
6. Le serveur distribue le message à tous les participants
7. Le message apparaît dans la conversation pour tous les participants connectés

**Enchaînements alternatifs** :
- A4.1 : L'utilisateur n'a plus accès à la conversation
  1. Le système affiche un message d'erreur
  2. Le message n'est pas envoyé

**Postconditions** : 
- Le message est enregistré dans la base de données
- Le message est visible pour tous les participants de la conversation
- L'historique de la conversation est mis à jour

### Cas d'utilisation : Envoyer un fichier

**Acteurs** : Utilisateur standard

**Préconditions** : 
- L'utilisateur est authentifié
- L'utilisateur a accès à une conversation
- L'utilisateur dispose d'un fichier à partager

**Scénario nominal** :
1. L'utilisateur sélectionne une conversation
2. L'utilisateur choisit l'option "Joindre un fichier"
3. L'utilisateur sélectionne un fichier depuis son système
4. Le système vérifie la taille et le type du fichier
5. L'utilisateur confirme l'envoi
6. Le système transmet le fichier au serveur
7. Le serveur distribue une notification à tous les participants
8. Le fichier apparaît dans la conversation pour tous les participants

**Enchaînements alternatifs** :
- A4.1 : Le fichier dépasse la taille maximale autorisée
  1. Le système affiche un message d'erreur
  2. L'utilisateur est invité à sélectionner un fichier plus petit
- A4.2 : Le type de fichier n'est pas autorisé
  1. Le système affiche un message d'erreur
  2. L'utilisateur est invité à sélectionner un autre type de fichier

**Postconditions** : 
- Le fichier est enregistré dans le système de stockage
- Une référence au fichier est ajoutée à la conversation
- Le fichier est disponible pour téléchargement par les participants

### Cas d'utilisation : Épingler un message

**Acteurs** : Utilisateur standard

**Préconditions** : 
- L'utilisateur est authentifié
- L'utilisateur a accès à une conversation
- La conversation contient au moins un message

**Scénario nominal** :
1. L'utilisateur sélectionne une conversation
2. L'utilisateur identifie le message à épingler
3. L'utilisateur utilise la fonction "Épingler" sur ce message
4. Le système vérifie les droits d'épinglage
5. Le système marque le message comme épinglé
6. Le système notifie tous les participants de la conversation
7. Le message épinglé est mis en évidence pour tous les participants

**Enchaînements alternatifs** :
- A4.1 : L'utilisateur n'a pas les droits pour épingler un message
  1. Le système affiche un message d'erreur
  2. L'action est annulée

**Postconditions** : 
- Le message est marqué comme épinglé dans la base de données
- Le message est mis en évidence dans l'interface de tous les participants
- Les participants sont notifiés du changement

## Administration

### Cas d'utilisation : Gérer les membres d'un canal

**Acteurs** : Administrateur

**Préconditions** : 
- L'utilisateur est authentifié
- L'utilisateur a le rôle d'administrateur dans le canal

**Scénario nominal** :
1. L'administrateur accède à un canal
2. L'administrateur sélectionne "Gérer les membres"
3. Le système affiche la liste des membres actuels
4. L'administrateur peut :
   - Supprimer des membres existants
   - Modifier les rôles des membres
5. L'administrateur confirme les modifications
6. Le système applique les changements
7. Le système notifie les membres concernés

**Enchaînements alternatifs** :
- A4.1 : L'administrateur tente de supprimer le dernier administrateur
  1. Le système affiche un message d'erreur
  2. L'action est refusée
- A4.2 : Un utilisateur ajouté n'existe pas
  1. Le système affiche un message d'erreur
  2. L'utilisateur n'est pas ajouté

**Postconditions** : 
- La liste des membres du canal est mise à jour
- Les rôles des membres sont ajustés selon les modifications
- Les membres concernés sont notifiés des changements