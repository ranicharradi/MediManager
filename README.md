# MediManager

**Auteurs :** Iyed Kadri, Rani Charradi, Youssef Zaghouni (TP3)

## Description

MediManager est une application Android native développée pour faciliter la gestion quotidienne des patients dans un cabinet médical. Elle permet aux médecins de gérer efficacement les informations des patients, les consultations et les rendez-vous via une interface moderne et intuitive.

## Objectifs

- Centraliser les informations médicales des patients.
- Suivre l'historique des consultations.
- Gérer les rendez-vous avec un système de notifications.
- Offrir une interface utilisateur simple et rapide.
- Garantir la persistance des données via SQLite.
- Authentification sécurisée avec rôles (Médecin/Patient).

## Cahier des Charges Fonctionnel

### Gestion des Patients (CRUD Complet)

- **Fonctionnalités :** Ajouter, Modifier, Supprimer (avec confirmation), Rechercher par nom, Voir les détails.
- **Infos Patient :** Nom, Prénom, Date de naissance (calcul automatique de l'âge), Genre, Téléphone, Email, Adresse, Groupe Sanguin, Allergies, Date de création.

### Gestion des Consultations

- **Fonctionnalités :** Ajouter, Voir l'historique, Modifier, Supprimer, Filtrer par date.
- **Infos Consultation :** Lien Patient, Date, Diagnostic, Traitement, Prescription, Notes.

### Gestion des Rendez-vous

- **Fonctionnalités :** Planifier, Modifier, Annuler, Marquer comme terminé, Voir (Jour/Semaine/Tous), Notifications.
- **Infos Rendez-vous :** Lien Patient, Date, Heure, Motif, Statut (Programmé/Terminé/Annulé), Notes.

### Authentification & Rôles

- **Connexion :** Sélection du rôle (Médecin/Patient), validation email/mot de passe.
- **Inscription :** Création de compte avec rôle, prénom, nom, email, téléphone, mot de passe.
- **Comptes de test :**
  - Médecin : `doctor@medimanager.tn` / `doctor123`
  - Patient : `patient@medimanager.tn` / `patient123`

### Tableau de Bord & Statistiques

- Nombre total de patients enregistrés.
- Nombre de consultations du mois en cours.
- Nombre de rendez-vous à venir.
- Liste des patients récemment ajoutés.

## Spécifications Techniques

- **Langage :** Java 21
- **SDK :** Min API 23 (Target API 36)
- **Base de données :** SQLite (Implémentation manuelle, sans ORM)
- **Architecture :** MVC (Modèle-Vue-Contrôleur)
- **UI :** Material Design 3, ViewBinding

## Schéma de la Base de Données

**Table `patients`**

```sql
CREATE TABLE patients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    date_of_birth TEXT,
    gender TEXT,
    phone TEXT,
    email TEXT,
    address TEXT,
    blood_group TEXT,
    allergies TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);
```

**Table `consultations`**

```sql
CREATE TABLE consultations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    consultation_date TEXT NOT NULL,
    diagnosis TEXT,
    treatment TEXT,
    prescription TEXT,
    notes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
```

**Table `appointments`**

```sql
CREATE TABLE appointments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id INTEGER NOT NULL,
    appointment_date TEXT NOT NULL,
    appointment_time TEXT NOT NULL,
    reason TEXT,
    status TEXT DEFAULT 'scheduled',
    notes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
```

**Table `users`**

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL,
    phone TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);
```

## Structure du Projet

```text
app/
  src/main/java/com/example/medimanager/
    activities/           # Écrans principaux (MainActivity, AddPatientActivity, etc.)
    fragments/            # Fragments de tableau de bord et listes
    adapters/             # Adapters RecyclerView
    database/             # Helper SQLite et classes DAO (PatientDAO, ConsultationDAO, AppointmentDAO, UserDAO)
    models/               # Modèles de données (Patient, Consultation, Appointment, User)
    utils/                # Constantes et utilitaires
  src/main/res/           # Layouts, drawables, valeurs
```

## Démarrage

**Prérequis** : Android Studio, JDK Java 21, Android SDK 23+.

**Cloner le dépôt :**

```bash
git clone https://github.com/Treshaun/MediManager.git
```

**Compiler :**

```bash
./gradlew assembleDebug
```

`local.properties` doit pointer vers votre chemin SDK ; Android Studio le générera automatiquement à l'ouverture du projet.

## Notes sur la Base de Données

- `DatabaseHelper` insère des données exemples (patients/rendez-vous) au premier lancement et applique les clés étrangères.
- Les changements de schéma nécessitent la mise à jour des constantes de table, des opérations CRUD DAO et des champs/formulaires de modèle correspondants.
- Les valeurs de statut des rendez-vous doivent rester en minuscules (`scheduled`, `in_progress`, `completed`, `cancelled`) pour garder la synchronisation avec les chips et les adapters.

## Contributing

1. Create a feature branch from `master`.
2. Follow the existing patterns (view binding, DAO filtering, `Constants` extras) when adding screens or fields.
3. Run `./gradlew lint` / builds before opening a pull request.
4. Submit a PR describing changes and any database migrations performed.
