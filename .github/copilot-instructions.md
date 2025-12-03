# MediManager – AI Guide

## Big Picture

- Single-module Android app (`app/`) targeting SDK 36, min 23, **Java 21**, Material 1.13, and view binding enabled (`app/build.gradle.kts`).
- `MainActivity` (`activities/MainActivity.java`) hosts `HomeFragment`, `AppointmentsFragment`, `PatientsFragment`, `ProfileFragment` inside `fragment_container` with a **standard `BottomNavigationView`** and FAB whose action swaps between Add Patient/Appointment screens.
- Fragments are mostly read-only dashboards; creation/edit flows live in dedicated activities under `activities/` and return to fragments via `ActivityResultLauncher` refresh hooks.
- **Authentication Activities:** `LoginActivity` (launcher) and `RegisterActivity` handle user authentication before accessing the main app.

## Authentication

- **Login Flow:** `LoginActivity` is the launcher; users select Doctor/Patient role and authenticate via `UserDAO.authenticateUser()`.
- **Registration:** `RegisterActivity` handles new account creation with role selection, form validation, and duplicate email checks.
- **Session Management:** Login state persists in `SharedPreferences` using constants from `utils/Constants` (`PREF_IS_LOGGED_IN`, `PREF_IS_DOCTOR`, `PREF_USER_ID`, `PREF_USER_EMAIL`, `PREF_USER_NAME`).
- **Sample Accounts:** Database seeds two test users on first launch:
  - **Doctor:** `doctor@medimanager.tn` / `doctor123`
  - **Patient:** `patient@medimanager.tn` / `patient123`

## Data Layer

- Local persistence is manual SQLite via `database/DatabaseHelper` (version 5) and DAO classes (`AppointmentDAO`, `PatientDAO`, `ConsultationDAO`, `UserDAO`). No Room/ORM—every schema change requires updating the CREATE_TABLE strings, downgrades (drop + recreate) and the corresponding `cursorTo*` mappers.
- **Strict Schema:** Tables (`patients`, `consultations`, `appointments`, `users`) must match the `cahier-de-charge.pdf` definitions exactly.
- **Doctor-Specific Data:** Patients and appointments have a `doctor_id` foreign key to `users` table. DAOs filter queries by `doctorId` for doctor users.
- **Patient-User Linking:** The `patients` table has an optional `user_id` column to link patient medical records to user accounts. Use `PatientDAO.getPatientByUserId(userId)` to find a patient's medical record from their login. Sample data links the patient user (id=2) to a patient record (Sarra Mejri).
- Shared field names live in `DatabaseHelper`; keep them in sync with `models/*` POJOs used for binding/serialization.

## UI & Interaction Patterns

- Activities/fragments rely on generated bindings (`ActivityAddPatientBinding`, `FragmentAppointmentsBinding`, etc.); never call `findViewById` in new code.
- **Navigation:** `MainActivity` uses a direct `BottomNavigationView` (no `BottomAppBar` wrapper). The FAB is positioned with a bottom margin of `120dp` to avoid overlap.
- **Theming:** Colors must strictly follow the medical theme defined in `colors.xml` (Primary: #1976D2, Status: Orange/Green/Red).
- Lists use RecyclerView adapters under `adapters/` (e.g., `AppointmentAdapter`) that expose explicit listener interfaces for row taps vs status toggles—re-use these instead of wiring new click handlers in fragments.
- `HomeFragment` aggregates metrics and displays a "Recent Patients" list as per requirements.
- `AppointmentsFragment` demonstrates the canonical filtering pattern: load via DAO, keep `appointmentList` + `filteredList`, drive UI state (empty view vs list) manually.
- Screen-to-screen data passes through `utils/Constants` intent extras (e.g., `EXTRA_PATIENT_ID`, `EXTRA_IS_EDIT_MODE`); never hardcode strings.

## Scheduling & Notifications

- `AddAppointmentActivity` schedules reminders an hour before the visit using `AlarmManager` + `AppointmentNotificationReceiver`. When editing appointment time you must cancel/reschedule by reusing the same `PendingIntent` key (`appointmentId`).
- Status chips/text map to the lowercase constants in `Constants` while adapters display friendly labels via `Appointment.getStatusDisplayName()`—always persist the lowercase value.

## Developer Workflow

- **Commit Often:** Always commit changes after completing a significant task, bug fix, or feature implementation. Keep the repo clean.
- **Update Instructions:** Continuously update this `copilot-instructions.md` file as the project evolves, architectural decisions change, or new patterns are established.
- Build APK: `pwsh> .\gradlew.bat assembleDebug`. Clean with `pwsh> .\gradlew.bat clean` if bindings/desugaring glitch.
- Instrumented tests (none yet) run via `pwsh> .\gradlew.bat connectedDebugAndroidTest`; unit tests via `pwsh> .\gradlew.bat testDebugUnitTest`.
- View binding classes regenerate on each build; if IDE warnings appear, rerun `assembleDebug` instead of manually editing `build/generated/` sources.

## Debugging & Database Export

- **DatabaseExporter** (`utils/DatabaseExporter.java`) exports the entire database to a text file whenever a patient is added or user registers.
- Export runs on a **background thread** to avoid UI lag.
- Exports are saved to `/storage/emulated/0/Android/data/com.example.medimanager/files/MediManager_DB_Export/latest_export.txt` on the device.
- Old exports are automatically deleted; only `latest_export.txt` is kept.
- **Pull export to desktop:** Run `pwsh> .\pull_db_export.ps1` from the project root to retrieve the latest export to your Windows desktop.
- **Clear app data:** Run `adb shell pm clear com.example.medimanager` to reset the database to default sample data.

## Phone Number Format

- All phone inputs use a **+216 prefix** (Tunisia country code) shown as a read-only `prefixText` in Material TextInputLayout.
- When saving, code prepends `"+216 "` to the user-entered number.
- When editing/displaying, the prefix is stripped from the stored value before showing in the input field.

## Extending the App

- Adding new patient/appointment fields requires: column + default in `DatabaseHelper`, DAO CRUD updates, model getters/setters, existing forms (layouts + activities) and adapters/bindings.
- New list UIs should live in fragments and reuse the DAO filtering patterns plus binding-driven empty states (`tvEmptyState`).
- Before introducing libraries (Room, Retrofit, etc.), ensure Gradle version catalogs (`gradle/libs.versions.toml`) include versions and wire aliases to `app/build.gradle.kts`.

## Gotchas

- `AppointmentNotificationReceiver` references `R.drawable.ic_notification`; confirm the asset exists or update when renaming drawables.
- Database upgrade currently wipes tables—if persistence matters, add migrations before raising `DATABASE_VERSION`.
- Many buttons currently show placeholder toasts (“coming soon”); keep UX consistent by reusing the same message or fully implementing the feature.
