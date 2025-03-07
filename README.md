# MyPrivateNote

**MyPrivateNote** est une application web permettant aux utilisateurs d'écrire, modifier et gérer leurs propres notes de manière sécurisée. L'application offre un système d'authentification via Google et dispose d'une interface de rédaction de texte riche grâce à Quill.js.

## Fonctionnalités

- **Connexion via Google** : Inscrivez-vous ou connectez-vous en utilisant votre compte Google pour un accès rapide et sécurisé.
- **Création de notes** : Rédigez, modifiez et sauvegardez vos notes personnelles.
- **Éditeur de texte riche avec Quill.js** : Utilisez un éditeur de texte WYSIWYG pour formater vos notes avec des options telles que gras, italique, souligné, liste, etc.
- **Sécurité avec Spring Security** : Protection des données et des sessions utilisateur.
- **UI réactive avec Tailwind CSS** : Interface moderne et responsive adaptée à tous les appareils.

## Technologies utilisées

- **Backend** : Spring Boot, Spring Security, Spring Data JPA, OAuth2
- **Frontend** : Thymeleaf, JavaScript, Tailwind CSS, Quill.js
- **Base de données** : MySQL
- **Sécurisation** : JSON Web Tokens (JWT)
- **API d'authentification** : OAuth2 avec Google

## Dépendances Maven

Voici une liste des dépendances principales utilisées dans le projet :

- **Spring Boot Starter Data JPA** : Permet l'intégration de JPA pour la gestion de la persistance des données.
- **Jsoup** : Utilisé pour nettoyer et traiter le contenu HTML dans les notes.
- **JSON Web Tokens (JWT)** : Utilisé pour sécuriser les sessions des utilisateurs et gérer l'authentification.
- **Spring Boot Starter Validation** : Pour effectuer des validations côté serveur.
- **Spring Boot Starter OAuth2 Client** : Pour l'intégration avec le service OAuth2 de Google pour la connexion.
- **Spring Boot Starter Security** : Pour ajouter la gestion de la sécurité dans l'application.
- **Spring Boot Starter Web** : Pour le développement de l'API REST et l'intégration avec le frontend.
- **Spring Boot Starter Thymeleaf** : Pour le rendu de vues côté serveur avec Thymeleaf.
- **MySQL Connector** : Pour connecter l'application à une base de données MySQL.
- **Lombok** : Pour réduire le code boilerplate (ex : getter, setter, constructeur).
- **Spring Boot Starter Test** : Pour l'écriture de tests unitaires et d'intégration dans le projet.
- **Spring Security Test** : Pour tester les composants de sécurité de l'application.

## Prérequis

Avant de démarrer, assurez-vous d'avoir les éléments suivants installés sur votre machine :

- JDK 17+ (recommandé pour Spring Boot)
- Maven ou Gradle
- Node.js et npm (pour gérer les dépendances JavaScript)
- Une base de données compatible avec Spring Data (par exemple MySQL)

## Installation

### Clonez le projet

```bash
git clone https://github.com/JonatanNs/myprivatenote.git
cd myprivatenote
````
### Lancer l'application

1. **Backend** :
    - Si vous utilisez Maven, lancez le serveur backend avec la commande suivante :
      ```bash
      mvn spring-boot:run
      ```

2. **Frontend** (si vous avez un dossier séparé pour le frontend) :
    - Allez dans le dossier `frontend` et installez les dépendances JavaScript :
      ```bash
      cd frontend
      npm install
      ```

    - Lancez ensuite le serveur frontend :
      ```bash
      npm start
      ```

   L'application sera maintenant accessible à l'adresse `http://localhost:8080` (ou une autre URL si vous avez configuré un autre port).

### Licence

Ce projet est sous la licence MIT. 

