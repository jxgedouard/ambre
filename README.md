# Ambre

> Gestionnaire de tâches personnel avec authentification, notifications macOS et support FR/EN.

## Stack technique

| Couche | Technologie |
|--------|-------------|
| UI | JavaFX 23 |
| Langage | Java 25 |
| Persistance | Gson 2.10.1, JSON (`~/.ambre/`) |
| Auth | jBCrypt 0.4.1 |
| Build | Maven, javafx-maven-plugin |
| Distribution | jpackage (.dmg macOS) |

## Architecture

```
[Login / Register]
       ↓
[AuthService — session en mémoire]
       ↓
[MainController — ListView tâches]
  Filtres · CRUD · i18n FR/EN
       ↓
[TaskService]          [NotificationService]
       ↓                       ↓
[JsonStorage]           osascript (macOS)
  ~/.ambre/tasks.json
  ~/.ambre/users.json
```

## Lancement en une commande

```bash
git clone <repo>
cd ambre
mvn clean javafx:run
```

## Build distribution (.dmg macOS)

```bash
export JAVAFX_HOME=/path/to/javafx-sdk-23
mvn clean package verify -DskipTests
```

Le `.dmg` se trouve dans `target/dist/Ambre-1.0.0.dmg`.

## Installation (utilisateurs)

1. Télécharger `Ambre-1.0.0.dmg` depuis la page Releases
2. Ouvrir le `.dmg` et glisser Ambre dans Applications
3. Au premier lancement : clic droit → Ouvrir (Gatekeeper)

Aucune installation de Java requise.

## Structure du projet

```
ambre/
├── pom.xml
└── src/main/
    ├── java/com/ambre/
    │   ├── Main.java
    │   ├── model/          ← User, Task
    │   ├── service/        ← AuthService, TaskService, NotificationService
    │   ├── controller/     ← LoginController, RegisterController, MainController,
    │   │                      TaskFormController, TaskCell
    │   └── util/           ← JsonStorage, I18n
    └── resources/com/ambre/
        ├── fxml/           ← login.fxml, register.fxml, main.fxml, task_form.fxml
        ├── styles/         ← ambre.css
        └── i18n/           ← messages_fr.properties, messages_en.properties
```

## Données

Stockées dans `~/.ambre/` :
- `users.json` — comptes utilisateurs (passwords hashés BCrypt)
- `tasks.json` — tâches (titre, description, date, priorité, progression)

## Améliorations futures

- Synchronisation cloud (iCloud / Google Drive)
- Tags et catégories personnalisées
- Vue calendrier
- Export PDF des tâches
- Rappels récurrents
