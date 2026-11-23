# TaskFlow — Plateforme de gestion de tâches (Spring Boot sur PaaS)

Application web Java prête à être déployée sur l'offre **JavaPaas** de Cloud Clusters
(Java 17/19, Spring Boot 3.1). TP Cloud Computing & Virtualisation — INP-HB.

## Fonctionnalités

| Élément | Détail |
|---|---|
| Interface web | Liste des tâches, filtres par statut, création / modification / suppression, priorités, échéances (retards signalés) |
| API REST (JSON) | `GET/POST /api/tasks`, `GET/PUT/DELETE /api/tasks/{id}`, `PATCH /api/tasks/{id}/status?value=EN_COURS`, `GET /api/tasks/stats` |
| Page « Plateforme » | `/plateforme` : version Java, OS, nom d'hôte du conteneur, mémoire, base utilisée → preuve que l'app tourne sur le PaaS |
| Supervision | `/actuator/health` (état app + base), `/actuator/info` (build, Java, OS) |
| Base de données | H2 embarquée par défaut (aucune installation), **MySQL** ou **PostgreSQL** via un simple profil |

## Prérequis (sur ton PC)

- **JDK 17** (Temurin, Oracle ou OpenJDK). Ne pas compiler pour Java 21 : JavaPaas propose Java 17/19.
- **Maven 3.8+**, ou IntelliJ IDEA (Maven intégré).
- **FileZilla** (si le dépôt sur Cloud Clusters se fait en FTPS).

Vérifier : `java -version` → 17.x, `mvn -v`.

## 1. Construire et tester en local

```bash
cd taskflow
mvn clean package          # compile, lance les tests, produit target/taskflow.jar
java -jar target/taskflow.jar
```

Ouvrir http://localhost:8080. Des tâches de démonstration sont créées au premier démarrage.

Sous IntelliJ : *Open* → dossier `taskflow` → panneau Maven → `Lifecycle > package`.

## 2. Déployer sur Cloud Clusters (JavaPaas)

1. **Add New Service → Applications → JavaPaas → Deploy Now.** Choisir Java 17, la version Spring 3.1,
   le plus petit plan, et cocher la base de données relationnelle (MySQL) si proposée.
2. Attendre l'état *Running*, puis ouvrir le panneau de l'application et noter :
   accès FTPS / upload, informations de la base (hôte, port, nom, utilisateur, mot de passe), domaine par défaut
   (page *Site & SSL*).
3. **Téléverser `target/taskflow.jar`** à l'emplacement indiqué par le panneau (upload ou FTPS).
4. **Commande de démarrage** (si le panneau la demande) :
   ```bash
   java -jar taskflow.jar
   ```
5. **Variables d'environnement** (si tu utilises la base MySQL de Cloud Clusters) :

   | Variable | Valeur |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `mysql` (ou `postgres`) |
   | `DB_HOST` | hôte de la base (panneau) |
   | `DB_PORT` | `3306` (ou celui indiqué) |
   | `DB_NAME` | nom de la base |
   | `DB_USERNAME` | utilisateur |
   | `DB_PASSWORD` | mot de passe |

   Si le panneau ne permet pas de définir des variables, les passer dans la commande :
   ```bash
   java -Dspring.profiles.active=mysql -DDB_HOST=xxx -DDB_PORT=3306 -DDB_NAME=taskflow \
        -DDB_USERNAME=xxx -DDB_PASSWORD=xxx -jar taskflow.jar
   ```
   Sans base externe, l'app fonctionne directement avec H2 (fichier `./data/taskflow`).
6. Si la plateforme impose un port, il est lu automatiquement dans la variable `PORT`
   (sinon 8080). Ne rien coder en dur.
7. Redémarrer l'application depuis le panneau, puis ouvrir `https://<domaine-par-défaut>/`.

## 3. Vérifier le déploiement (captures pour le rapport)

- `/` : interface de gestion des tâches
- `/plateforme` : environnement fourni par le PaaS (Java, OS, conteneur, base)
- `/actuator/health` : doit renvoyer `"status":"UP"` avec la base `UP`
- `/api/tasks` : réponse JSON

Test de l'API avec curl :

```bash
curl https://<domaine>/api/tasks
curl -X POST https://<domaine>/api/tasks -H "Content-Type: application/json" \
     -d '{"title":"Tâche créée via l API","priority":"HAUTE"}'
```

## Dépannage

| Symptôme | Cause probable / solution |
|---|---|
| `UnsupportedClassVersionError` | JAR compilé avec un JDK trop récent → recompiler avec JDK 17 |
| `Communications link failure` | Mauvais `DB_HOST`/`DB_PORT`, ou base non démarrée |
| `Access denied for user` | `DB_USERNAME` / `DB_PASSWORD` erronés |
| Page 502 / injoignable | L'app n'écoute pas le bon port → vérifier la variable `PORT` dans le panneau et les logs |
| Données perdues après redémarrage | Normal avec H2 si le disque n'est pas persistant → utiliser le profil `mysql` |

## Structure du projet

```
taskflow/
├── pom.xml                          Spring Boot 3.1.12, Java 17
└── src/
    ├── main/java/ci/inphb/taskflow/
    │   ├── TaskflowApplication.java     point d'entrée (Tomcat embarqué)
    │   ├── model/                        Task, Status, Priority (entité JPA)
    │   ├── repository/TaskRepository     accès aux données (Spring Data JPA)
    │   ├── service/TaskService           logique métier
    │   ├── web/TaskApiController         API REST
    │   ├── web/PageController            pages web + page Plateforme
    │   └── config/DataInitializer        données de démonstration
    ├── main/resources/
    │   ├── application.properties        config par défaut (H2, port, actuator)
    │   ├── application-mysql.properties  profil MySQL
    │   ├── application-postgres.properties
    │   ├── templates/                    pages Thymeleaf
    │   └── static/css/style.css
    └── test/…                            tests d'intégration (API + pages)
```

## Architecture (pour le rapport)

```
 Navigateur / Postman
        │ HTTPS
        ▼
 ┌──────────────── Cloud Clusters — JavaPaas (géré par le fournisseur) ────────────────┐
 │  Proxy / domaine + SSL  →  Conteneur : OS + JVM 17  →  taskflow.jar (Tomcat embarqué) │
 │                                                          │ JDBC                       │
 │                                                          ▼                            │
 │                                              Base MySQL (option PaaS)                 │
 └──────────────────────────────────────────────────────────────────────────────────────┘
 Le développeur ne fournit que le JAR et la configuration (variables d'environnement).
```
