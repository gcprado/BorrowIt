# BorrowIt

> Community-powered borrowing app built as part of the PDIGS course at ULPGC. BorrowIt helps neighbors share the items they already own—tools, appliances, camping gear, etc.—instead of buying new ones.

BorrowIt is a 100% Jetpack Compose Android application backed by Firebase (Auth, Firestore, Storage, Play Services). Users can form invite-only communities, publish inventories, request loans, and keep track of everything from a single mobile experience.

## Table of contents

1. [Overview](#overview)
2. [Feature highlights](#feature-highlights)
3. [Tech stack](#tech-stack)
4. [Architecture](#architecture)
5. [Firebase data model](#firebase-data-model)
6. [Project structure](#project-structure)
7. [Getting started](#getting-started)
8. [Run, build & test](#run-build--test)
9. [Troubleshooting](#troubleshooting)
10. [Roadmap & contributions](#roadmap--contributions)
11. [License](#license)

## Overview

BorrowIt focuses on three core flows:

- **Community spaces** – create or join curated lending groups via invitation codes.
- **Inventory & availability** – upload items with photos, conditions, and time windows, then manage them through dedicated owner/borrower dashboards.
- **Borrowing lifecycle** – submit loan requests, receive notifications, accept/decline, and close the loop by marking items as returned.

The UI is optimized for handheld devices and relies on Material 3 styling, custom typography, and edge-to-edge layouts.

## Feature highlights

1. **Community-first sharing**
    - Compose-based `CommsScreen` lets users sort, search, and browse their communities.
    - Invitation codes are Base64-encoded Firestore IDs; members can paste a code to join instantly.
    - Community owners can upload banners/avatars via `ImageUtils` (images are compressed client-side before landing in Firebase Storage).

2. **Smart inventory management**
    - `ItemsScreen` surfaces the owner's catalog with quick actions (history dialog, upload dialog, item details) and integrates with `ItemRepository` streams.
    - `ManageItemsScreen` merges Firestore item snapshots with live borrow requests to display "My items" versus "Borrowed items" tabs, status badges, and return dialogs.

3. **Borrowing workflow & notifications**
    - `BorrowRepository` normalizes Firestore IDs and exposes Flows for pending requests (owners) and active borrows (requesters).
    - The `HomeScreen` shows profile info, recommended items (based on previous borrows), sponsored cards, and a notification center powered by dialogs.
    - Request statuses cover `pending → accepted/declined → finished`, and closing an item triggers `finishRequestsForItem` to keep Firestore consistent.

4. **Authentication & profile management**
    - Email/password and Google Sign-In (Play Services Auth + Firebase Auth) are handled in `AuthViewModel`/`AuthRepository`.
    - `ProfileViewModel` lets people change usernames, upload avatars (with automatic cleanup of old storage objects), sign out from Firebase + Google, or delete their account entirely.

5. **Modern Compose UI toolkit**
    - Material 3 components, Navigation Compose, Coil image loading, and custom theming in `ui/theme` keep the experience cohesive.
    - `MainActivity` enables edge-to-edge, hosts the `AppNavGraph`, and runs one-off migrations (e.g., normalizing legacy availability arrays).

## Tech stack

| Layer | Details |
| --- | --- |
| Language & tooling | Kotlin 2.2.x, AGP 9.1.0, Gradle Kotlin DSL, minimum SDK 25 / target SDK 35 |
| UI | Jetpack Compose, Material 3, Navigation Compose, Coil |
| State & async | Kotlin Coroutines + Flow, ViewModel APIs |
| Backend services | Firebase Authentication, Cloud Firestore, Firebase Storage, Google Play Services Auth |
| Utilities | Image compression helpers (`ImageUtils`), SharedPreferences migrations, version-catalog-managed dependencies |

Dependency versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml) for easy upgrades.

## Architecture

BorrowIt follows a lightweight MVVM layering with explicit navigation graphs:

```
Jetpack Compose screens & components (Home, Communities, Items, Profile, dialogs)
            ↓ (state hoisted via ViewModels in /control)
ViewModels orchestrate flows and expose UI state (HomeViewModel, AuthViewModel, ProfileViewModel, ...)
            ↓
Repositories handle Firebase access (/data/repositories) + local helpers (SharedPreferences, Storage uploads)
            ↓
Firebase Auth · Firestore · Storage + Google Sign-In
```

Key navigation concepts:

- `GraphRoute.AUTH` hosts login & sign-up screens.
- `GraphRoute.MAIN` hosts the bottom-nav destinations (`Home`, `Communities`, `Items/ManageItems`, `Profile`) plus community detail routes.

Compose Flows are collected with lifecycle-aware scopes to keep UI real-time without manual listeners.

## Firebase data model

BorrowIt expects the following collections (names are hard-coded inside repositories):

| Collection | Purpose | Key fields / subcollections |
| --- | --- | --- |
| `users` | Profile data for each Firebase Auth UID | `username`, `profilePicture`, contact info |
| `communities` | Community metadata | `name`, `description`, `creatorId`, `memberCount`, `bannerUrl`, `profileUrl`, timestamps; subcollections: `members`, `communityItems` |
| `items` | Inventory items shared within communities | `name`, `description`, `owner`, `condition`, `pictures`, `availability { start, end }`, `communityId`, `status`, `currentUser` |
| `borrow_requests` | Lifecycle of every lending request | `communityId`, `itemId`, `ownerId`, `requesterId`, `status`, `requestDate`, `startDate`, `endDate` |

Make sure the Firebase project has:

1. **Authentication** – Email/Password **and** Google providers enabled. Register your debug keystore SHA-1/SHA-256 so Google Sign-In works locally.
2. **Firestore** – In production or test mode. Optional composite indexes may be required if you add more complex queries later.
3. **Storage** – Buckets for `item_images/`, `community_banners/`, `community_profiles/`, and `profile_pictures/` as uploaded by the repositories.
4. **Security rules** – Start with development-friendly rules, then tighten them before releasing (e.g., ensure users can only mutate their own data and community admins gate member actions).

`google-services.json` must be placed in `app/google-services.json` (the file is already ignored from source control). Update the package name (`com.pigs.borrowit`) inside the Firebase console if you fork the project.

## Project structure

```
borrowit/
├── app/
│   ├── build.gradle.kts
│   └── src/main/java/com/pigs/borrowit/
│       ├── MainActivity.kt
│       ├── control/               # ViewModels (Auth, Home, Profile, ...)
│       ├── data/
│       │   ├── model/             # Item, BorrowRequest, Community, User, etc.
│       │   └── repositories/      # Firebase data sources (AuthRepository, ItemRepository, ...)
│       ├── presentation/navigation# Nav graphs, routes, typed arguments
│       ├── screens/               # Compose UI screens + dialogs/components
│       ├── ui/theme/              # Material 3 palette, typography, shapes
│       └── utils/                 # Helpers (image compression, etc.)
├── gradle/libs.versions.toml      # Dependency versions
├── settings.gradle.kts
└── build.gradle.kts
```

## Getting started

1. **Prerequisites**
    - Android Studio Koala (or newer) with Gradle 8.9+ support.
    - JDK 17 (AGP 9.x requirement) installed and configured (`File > Settings > Build Tools > Gradle`).
    - A Firebase project with Auth, Firestore, and Storage enabled.

2. **Clone & open**
   ```bash
   git clone https://github.com/<your-org>/BorrowIt.git
   open the project in Android Studio
   ```

3. **Firebase configuration**
    - Create an Android app in Firebase using the package `com.pigs.borrowit`.
    - Download `google-services.json` and drop it into `app/`.
    - Add your debug SHA-1/256 fingerprints so Google Sign-In can exchange tokens.
    - (Optional) Seed Firestore with initial documents for `users`, `communities`, `items`, and `borrow_requests`.

4. **Set up Authentication providers**
    - Enable Email/Password and Google providers in **Authentication > Sign-in method**.
    - Configure an OAuth consent screen if Google Sign-In prompts you inside the Firebase console.

5. **Adjust Storage/Firestore rules for development**
    - During local development you can allow read/write to authenticated users. Before shipping, restrict writes to owners/admins.

## Run, build & test

| Action | Command / instructions |
| --- | --- |
| Sync dependencies | Android Studio ► *Sync Project with Gradle Files* |
| Launch the debug app | `./gradlew :app:installDebug` or press ▶️ on a connected device/emulator |
| Build an APK | `./gradlew :app:assembleDebug` (or `assembleRelease`) |
| Unit tests | `./gradlew test` (runs JVM tests) |
| Instrumented & Compose UI tests | `./gradlew :app:connectedAndroidTest` (requires device/emulator) |
| Static analysis / lint | `./gradlew lint` (AGP default lint report under `app/build/reports/`) |

### Debugging tips

- Use the "Profile or debug APK" tooling in Android Studio to inspect recompositions (`Layout Inspector`, `Animation Preview`).
- Real-time updates depend on Firebase listeners; verify your Firestore indexes and security rules if lists look empty.
- `BorrowRepository` logs mapping issues with the `BorrowRepoDebug` tag—check Logcat when testing borrow flows.

## Troubleshooting

| Symptom | Likely cause / fix |
| --- | --- |
| Google Sign-In closes immediately | Missing SHA-1 in Firebase console or `google-services.json` mismatch. Re-download the config file after adding fingerprints. |
| Item uploads fail silently | Storage rules may block writes or image compression returned `null`. Inspect Logcat for `ImageUtils` errors. |
| Communities list stays empty | Ensure the logged-in user exists in `users` AND is referenced either as `creatorId` or inside a `communities/{id}/members` document. |
| Requests never appear in notifications | Firestore security rules might prevent `borrow_requests` reads. Also verify that `ownerId`/`requesterId` fields store plain UIDs (the repository already cleans references, but old data might still have full paths). |

## Roadmap & contributions

Potential enhancements (open for contributions):

1. Push notifications via Firebase Cloud Messaging when borrow requests change state.
2. Offline caching + pagination for large communities using Room or Jetpack DataStore.
3. Fine-grained role management (co-admins, moderators) and audit logs inside communities.
4. Automated tests covering Compose screens with the Testing Manifest and Fake repositories.
5. Accessibility polish (TalkBack labels, larger touch targets, dynamic color).
