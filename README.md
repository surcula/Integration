# ERP RH médical

Application intranet de gestion des ressources humaines destinée à un environnement hospitalier.

## Prérequis

- Java JDK 8
- Apache Tomcat 9
- MySQL 8
- IntelliJ IDEA

## Installation et lancement

1. Créer une base de données MySQL nommée `erp_projet_integration`.
2. Exécuter le script SQL fourni avec le projet dans cette base de données.
3. Vérifier la connexion dans `src/main/resources/META-INF/persistence.xml`.
4. Ouvrir le projet dans IntelliJ IDEA et charger les dépendances Maven.
5. Configurer un serveur Tomcat 9 avec l'artefact `ERPProjetIntegration_1:war exploded`.
6. Lancer l'application.
7. Ouvrir l'adresse suivante :

```text
http://localhost:8080/ERPProjetIntegration_1_war_exploded/login.xhtml   -> Page de login
```

## Compte administrateur

| Champ | Valeur |
|---|---|
| Email | `admin@test.be` |
| Mot de passe | `Test1234` |

Le mot de passe respecte les majuscules et les minuscules.
