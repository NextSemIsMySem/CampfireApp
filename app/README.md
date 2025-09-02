# Campfire - Self-Destructing Group Chat App

A Kotlin Android app that creates group chats with automatic self-destruct capabilities based on configurable conditions.

## Features

### 🔥 Self-Destruct Rules
- **Message Limit**: Groups auto-delete after reaching maximum messages
- **Time Duration**: Groups expire after a set time (e.g., 24 hours)
- **Inactivity Timeout**: Groups disappear after periods of inactivity

### 👥 User Management (Module 1)
- User registration and authentication
- Profile management
- Account deletion
- Firebase Authentication integration

### 📱 Group Management (Module 2)
- Create groups with custom self-destruct rules
- Edit group settings
- Join/leave groups
- View group details and statistics

### 💬 Message Management (Module 3)
- Real-time messaging with Firestore
- Edit and delete own messages
- Live message updates
- Message history

### ⏰ Self-Destruct System (Module 4)
- Automatic group cleanup
- Rule evaluation and enforcement
- Client-side destruction logic

## Tech Stack

- **Language**: 100% Kotlin
- **Architecture**: MVVM with Repository pattern
- **UI**: Jetpack Compose
- **Navigation**: Jetpack Navigation Compose
- **Backend**: Firebase Firestore
- **Authentication**: Firebase Auth
- **Dependency Injection**: Hilt
- **Async**: Coroutines + Flow

## Setup Instructions

### 1. Project Setup
This project builds on top of an existing Android Studio project. Make sure you have:
- Android Studio Hedgehog or later
- Kotlin 1.9.0 or later
- Minimum SDK 24, Target SDK 36

### 2. Firebase Setup
1. Create a new Firebase project at https://console.firebase.google.com
2. Add an Android app to your Firebase project
3. Use package name: `com.example.campfireapp`
4. Download the `google-services.json` file
5. Place it in the `app/` directory
6. Enable Authentication (Email/Password) in Firebase Console
7. Enable Firestore Database in Firebase Console

### 3. Project-Level build.gradle.kts
Add to your **project-level** `build.gradle.kts`:

```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51.1")
    }
}
```

### 4. Build and Run
1. Sync the project in Android Studio
2. Connect a physical device or start an emulator (API 24+)
3. Build and run the app

### 5. Demo Account (Optional)
The app includes demo data seeding. You can use these credentials:
- Email: `demo@campfire.com`
- Password: `demo123`

Or create a new account through the registration screen.

## Project Structure

```
app/
├── src/main/java/com/example/campfireapp/
│   ├── data/
│   │   ├── model/               # Data classes (User, Group, Message)
│   │   └── repository/          # Repository interfaces and implementations
│   ├── di/                      # Hilt dependency injection modules
│   ├── presentation/
│   │   ├── navigation/          # Navigation graph and routes
│   │   ├── screen/             # Compose UI screens
│   │   └── viewmodel/          # ViewModels for each module
│   ├── util/                   # Utility classes (dummy data seeder)
│   └── MainActivity.kt         # Main activity with navigation setup
├── AndroidManifest.xml         # Updated with Hilt application
└── build.gradle.kts           # Dependencies and plugins
```

## Module Implementation

### Module 1: User Management
- **Files**: `UserRepository`, `UserViewModel`, `LoginScreen`, `RegisterScreen`, `ProfileScreen`
- **Features**: Registration, login, profile updates, account deletion
- **Firebase Integration**: Auth + Firestore users collection

### Module 2: Group Management  
- **Files**: `GroupRepository`, `GroupViewModel`, `GroupListScreen`, `CreateGroupScreen`, `EditGroupScreen`
- **Features**: CRUD operations, membership management, self-destruct rule configuration
- **Firebase Integration**: Firestore groups collection

### Module 3: Message Management
- **Files**: `MessageRepository`, `MessageViewModel`, `ChatScreen`
- **Features**: Send/edit/delete messages, real-time updates, message history
- **Firebase Integration**: Firestore messages collection with live listeners

### Module 4: Self-Destruct System
- **Files**: `SelfDestructRepository`, `SelfDestructService`
- **Features**: Rule evaluation, automatic cleanup, group destruction
- **Logic**: Client-side evaluation with Firestore batch operations

## Key Features Demo

1. **Create Account**: Register with email/password
2. **Create Group**: Set self-destruct rules (message limit, duration, inactivity)
3. **Chat**: Send messages and watch real-time updates
4. **Auto-Destruction**: Groups disappear when conditions are met
5. **Profile Management**: Update info, logout, delete account

## Firebase Collections

### users
```kotlin
{
  id: String,
  email: String,
  displayName: String,
  profileImageUrl: String,
  createdAt: Long,
  lastActive: Long
}
```

### groups
```kotlin
{
  id: String,
  name: String,
  description: String,
  createdBy: String,
  memberIds: List<String>,
  createdAt: Long,
  lastActivity: Long,
  messageCount: Int,
  selfDestructRule: {
    maxMessages: Int?,
    durationMinutes: Long?,
    inactivityTimeoutMinutes: Long?
  },
  isActive: Boolean
}
```

### messages
```kotlin
{
  id: String,
  groupId: String,
  senderId: String,
  senderName: String,
  content: String,
  timestamp: Long,
  isEdited: Boolean,
  editedAt: Long?
}
```

## Development Notes

- All code is well-commented for student understanding
- Follows MVVM architecture consistently
- Uses Compose for modern UI development
- Implements proper error handling and loading states
- Includes real-time updates with Firestore listeners
- Self-destruct logic runs on client-side for simplicity
- Minimal abstractions to keep code beginner-friendly

## Troubleshooting

1. **Build Errors**: Make sure `google-services.json` is in the `app/` directory
2. **Firebase Connection**: Check internet connection and Firebase configuration
3. **Authentication Issues**: Verify Email/Password is enabled in Firebase Console
4. **Firestore Errors**: Ensure Firestore Database is created and rules allow read/write

## Future Enhancements

- Push notifications for new messages
- Image/file sharing in messages
- Group invitations via links
- Advanced self-destruct rules (custom schedules)
- Message encryption for privacy
- Cloud Functions for server-side rule evaluation
