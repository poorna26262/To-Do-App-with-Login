# To-Do App with Login — Android (Java, XML, SQLite)

A beginner-level Android task manager with account registration, login, per-user task storage, completion, deletion, and logout.

## Security choices

- Passwords are **not stored as plaintext**.
- Passwords use PBKDF2-HMAC-SHA256 with a unique random 16-byte salt and 210,000 iterations.
- The database stores the derived password hash and salt.
- SQL queries use selection arguments (`?`) rather than concatenating user input.
- Each task query/update/delete is scoped to the authenticated user's ID.
- Session state stores only the user ID in private app SharedPreferences and is cleared on logout.
- Android backup is disabled in the manifest for this simple local-data sample.

**Important:** MD5 and plain SHA-256(password) are not suitable password-storage algorithms. PBKDF2 is used here because it is available in Java/Android without adding a third-party library. For production apps, consider a maintained authentication service and current platform security guidance; local SQLite authentication is a learning exercise, not a substitute for server-side identity management.

## Features

- Login with email and password
- Registration with name, email, password, and confirmation
- Duplicate email protection
- Password hashing and per-account salt
- Persistent SQLite users and tasks
- Tasks linked to a user ID
- Add task with optional notes
- Mark complete/incomplete
- Completed task title displayed with strikethrough
- Permanently delete tasks with confirmation
- Friendly empty state
- Logout clears the session and returns to login

## Requirements

- Android Studio (recent stable release)
- JDK 17 (Android Studio bundled JDK recommended)
- Android SDK Platform 35
- Emulator/device API 23 or newer

## Run the app

1. Extract `TodoLoginAndroid.zip`.
2. Open the `TodoLoginAndroid` folder in Android Studio.
3. Wait for Gradle sync. Install Android SDK Platform 35 if prompted.
4. Start an emulator or connect a device with USB debugging enabled.
5. Press **Run**.
6. Register a new account, log in, and create tasks.

A Gradle wrapper is not included; Android Studio can use its configured Gradle installation or generate a wrapper from the project root.

## Project structure

```text
TodoLoginAndroid/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/todologin/
│       │   ├── LoginActivity.java
│       │   ├── RegisterActivity.java
│       │   ├── TaskActivity.java
│       │   ├── DatabaseHelper.java
│       │   ├── PasswordHasher.java
│       │   ├── SessionManager.java
│       │   └── TaskItem.java
│       └── res/layout/
│           ├── activity_login.xml
│           ├── activity_register.xml
│           └── activity_tasks.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Database schema

### users

| Column | Purpose |
|---|---|
| id | Primary key |
| name | Display name |
| email | Unique login identifier |
| password_hash | PBKDF2-derived hash |
| salt | Random per-user salt |

### tasks

| Column | Purpose |
|---|---|
| id | Primary key |
| user_id | Foreign key to users |
| title | Task title |
| notes | Optional notes |
| completed | 0 = active, 1 = completed |
| created_at | Creation timestamp |

## Test checklist

- [ ] Register with valid name, email, and password.
- [ ] Attempt duplicate email registration; expect an error.
- [ ] Log in with correct credentials.
- [ ] Attempt login with incorrect password; expect rejection.
- [ ] Add task with title and notes.
- [ ] Add task with title only.
- [ ] Mark a task complete and verify strikethrough.
- [ ] Mark a completed task incomplete.
- [ ] Delete a task and verify it disappears.
- [ ] Log out and verify login screen appears.
- [ ] Log in as a second user and verify the first user's tasks are not shown.
- [ ] Force-close and reopen app; verify tasks persist.

## Limitations

This is an educational single-device app. It does not provide server-side authentication, password reset, email verification, encrypted database storage, account recovery, rate limiting, or multi-device sync. Anyone with sufficient device/root/backup access may be able to inspect app data. Do not use real production credentials or sensitive personal task data in this demo.

## Android documentation

- SQLiteOpenHelper: https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper
- SQLiteDatabase: https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase
- SharedPreferences: https://developer.android.com/reference/android/content/SharedPreferences
- Android security best practices: https://developer.android.com/privacy-and-security/security-best-practices
