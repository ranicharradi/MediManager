# MediManager

MediManager is a single-module Android app that helps small clinics manage patients, consultations, and appointments. The app is written in Java 21, targets Android SDK 36 (min 23), and uses Material 1.13 with view binding.

## Features

- **Home dashboard** with patient totals, today/upcoming appointment counts, and quick actions.
- **Patients module** for browsing/searching patients, editing profiles, and viewing consultation histories.
- **Appointments module** that lists all visits, filters by status chips, and toggles appointment states in-line.
- **Consultations module** (via patient details) for logging diagnoses, treatments, and notes.
- **Profile Management** for updating clinic/doctor details and managing notification preferences.
- **Localization** support for Tunisia (fr-TN), including phone number validation (+216) and sample data.
- **Appointment reminders** scheduled via `AlarmManager` + `AppointmentNotificationReceiver` one hour before each visit.

## Tech Stack

- Android Gradle Plugin 8.13
- Java 21 with AndroidX AppCompat, RecyclerView, ConstraintLayout
- Manual SQLite layer via `DatabaseHelper` and DAO classes (no Room/ORM)
- View binding across all activities/fragments and RecyclerView adapters

## Project Structure

```text
app/
  build.gradle.kts        Module configuration and dependencies
  src/main/java/com/example/medimanager/
    activities/           Screen-level flows (AddPatient, AddAppointment, MainActivity, etc.)
    fragments/            Dashboards (Home, Appointments, Patients, Profile)
    adapters/             RecyclerView adapters with listener interfaces
    database/             SQLite helper + DAO classes
    models/               POJOs mirrored with DB schema
    utils/                Constants, DateUtils, Validation helpers
  src/main/res/           Layouts, drawables, menus, values
```

## Getting Started

1. **Requirements**: Android Studio Ladybug+ (or command-line tools), Java 21-compatible JDK, Android SDK 23–36.
2. **Clone** the repo:

   ```bash
   git clone https://github.com/Treshaun/MediManager.git
   cd MediManager
   ```

3. **Open in Android Studio** or use Gradle Wrapper commands:
   - Build debug APK: `./gradlew assembleDebug`
   - Clean build artifacts: `./gradlew clean`
   - Unit tests: `./gradlew testDebugUnitTest`
   - Instrumented tests: `./gradlew connectedDebugAndroidTest`

`local.properties` should point to your SDK path; Android Studio will generate it automatically when you open the project.

## Database Notes

- `DatabaseHelper` seeds sample patients/appointments on first run and enforces foreign keys.
- Schema changes require updating table constants, DAO CRUD operations, and corresponding model fields/forms.
- Appointment status values must remain lowercase (`scheduled`, `in_progress`, `completed`, `cancelled`) to keep chips and adapters in sync.

## Contributing

1. Create a feature branch from `master`.
2. Follow the existing patterns (view binding, DAO filtering, `Constants` extras) when adding screens or fields.
3. Run `./gradlew lint` / builds before opening a pull request.
4. Submit a PR describing changes and any database migrations performed.
