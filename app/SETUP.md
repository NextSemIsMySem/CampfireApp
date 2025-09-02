# Campfire App Setup Instructions

## Project-Level build.gradle.kts Setup

Since you need to modify the **project-level** `build.gradle.kts` file (located in the root directory, not in the app folder), add the following to the top of that file:

```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51.1")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

## Firebase Setup Steps

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project named "Campfire App"
3. Add an Android app with package name: `com.example.campfireapp`
4. Download the `google-services.json` file
5. Place it in the `app/` directory (same level as `build.gradle.kts`)
6. In Firebase Console, enable:
   - **Authentication** → Sign-in method → Email/Password
   - **Firestore Database** → Create database in production mode

## Build Process

1. After adding the project-level dependencies, sync the project
2. Clean and rebuild the project
3. If you get dependency resolution errors, try:
   - File → Invalidate Caches and Restart
   - Delete `.gradle` folder and rebuild

## Manual Alternative Setup (If Hilt Causes Issues)

If you want to simplify the setup and avoid Hilt complexity, you can create a simpler version by:

1. Remove all Hilt annotations (`@HiltViewModel`, `@Inject`, etc.)
2. Create manual dependency injection in MainActivity
3. Pass repositories directly to ViewModels

This would make the project easier to understand for beginners but less production-ready.

## Testing the App

### Quick Test Flow:
1. Launch app → Login screen appears
2. Tap "Register" → Create account with email/password
3. After registration → Group list screen (empty initially)
4. Tap "+" → Create group with self-destruct rules
5. Enter group → Send messages and test real-time updates
6. Create multiple groups to test different self-destruct rules

### Demo Credentials:
- Email: `demo@campfire.com`
- Password: `demo123`

The app includes logic to create this demo account automatically.

## Troubleshooting Common Issues

### Build Errors:
- **"google-services.json not found"**: Make sure the file is in the `app/` directory
- **"Unresolved reference: dagger"**: Check project-level build.gradle.kts has Hilt classpath
- **"Duplicate class"**: Clean project and rebuild

### Runtime Errors:
- **Firebase connection errors**: Check internet connection and Firebase config
- **Authentication failures**: Verify Firebase Auth is enabled with email/password
- **Firestore permission errors**: Use test mode rules initially

### Firebase Rules (For Testing):
Set Firestore rules to allow read/write for authenticated users:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## Features to Test

1. **User Registration/Login**
2. **Create Groups** with different self-destruct rules:
   - Max 10 messages
   - 1 hour duration  
   - 30 minutes inactivity timeout
3. **Real-time Chat** - open same group on multiple devices
4. **Profile Management** - edit display name
5. **Self-Destruct** - watch groups disappear when conditions are met

## Code Structure for Students

Each module is implemented as a separate CRUD feature:

- **Module 1 (User)**: `UserRepository` + `UserViewModel` + Auth screens
- **Module 2 (Group)**: `GroupRepository` + `GroupViewModel` + Group screens  
- **Module 3 (Message)**: `MessageRepository` + `MessageViewModel` + Chat screen
- **Module 4 (Self-Destruct)**: `SelfDestructService` + cleanup logic

Students can work on individual modules independently and integrate them together.
