# ⚽ Football Wallpaper App — Complete Setup Guide

## What You're Building
A production-ready Android app with:
- **User Panel**: Browse categories, view & download wallpapers, set as home/lock screen, add to favorites, share
- **Admin Panel**: Upload wallpapers, manage categories/subcategories, edit/delete wallpapers
- **Role-based access**: Admin panel is invisible to regular users
- **Backend**: 100% Firebase (no custom server)

---

## PART 1: Firebase Project Setup

### Step 1 — Create Firebase Project
1. Go to **https://console.firebase.google.com**
2. Click **"Add project"**
3. Name it: `football-wallpaper` (or anything you like)
4. Disable Google Analytics (not needed)
5. Click **"Create project"** and wait

---

### Step 2 — Register Android App
1. In your Firebase project dashboard, click **"Add app"** → **Android icon**
2. Fill in:
   - **Android package name**: `com.meritshot.footballwallpaper`  
     _(must match exactly — this is in your AndroidManifest.xml)_
   - **App nickname**: Football Wallpaper
   - **Debug signing certificate SHA-1**: Leave blank for now (not needed for Auth/Firestore)
3. Click **"Register app"**
4. **Download `google-services.json`**
5. **IMPORTANT**: Place `google-services.json` inside `app/` folder  
   Replace the placeholder file already there: `FootballWallpaper/app/google-services.json`
6. Click "Next" → "Next" → "Continue to console" (skip the SDK steps)

---

### Step 3 — Enable Firebase Authentication
1. Left sidebar → **"Authentication"** → **"Get started"**
2. Click **"Sign-in method"** tab
3. Click **"Email/Password"**
4. Toggle **"Enable"** to ON
5. Click **"Save"**

---

### Step 4 — Enable Cloud Firestore
1. Left sidebar → **"Firestore Database"** → **"Create database"**
2. Select **"Start in test mode"** (we'll add rules later)
3. Choose server location closest to you (e.g., `asia-south1` for India)
4. Click **"Done"**

**Apply Security Rules:**
1. Click the **"Rules"** tab in Firestore
2. Delete all existing content
3. Paste the contents of `firestore.rules` file from the project
4. Click **"Publish"**

---

### Step 5 — Enable Firebase Storage
1. Left sidebar → **"Storage"** → **"Get started"**
2. Click **"Next"** → Select same region as Firestore → **"Done"**

**Apply Storage Rules:**
1. Click the **"Rules"** tab in Storage
2. Replace existing rules with:
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /wallpapers/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;  // Tighten this in production
    }
  }
}
```
3. Click **"Publish"**

> **Note**: The full Storage rules in `storage.rules` use custom claims. For quick setup, use the simplified rules above. See "Production Security" section below for hardening.

---

### Step 6 — Create Admin User

#### Method A: Via Firebase Console (Easiest)
1. Go to **Authentication** → **Users** tab
2. Click **"Add user"**
3. Enter: `admin@football.com` / `Admin@123`
4. Note the **User UID** shown in the table

Now create the admin document in Firestore:
1. Go to **Firestore Database** → **"Start collection"**
2. Collection ID: `users`
3. Document ID: paste the **UID** from step above
4. Add fields:
   - `email` (string): `admin@football.com`
   - `role` (string): `admin`
5. Click **"Save"**

#### Method B: Register in App + Update Role
1. Open the app, tap "Register"
2. Create account with any email/password
3. In Firestore Console, find the document in `users` collection
4. Change `role` from `"user"` to `"admin"`

---

### Step 7 — Add Default Categories (Optional but Recommended)
In Firestore, create a `categories` collection with these documents:

| Field | Value |
|-------|-------|
| name  | Clubs |
| icon_emoji | 🏟️ |

| Field | Value |
|-------|-------|
| name  | Players |
| icon_emoji | ⭐ |

| Field | Value |
|-------|-------|
| name  | Stadiums |
| icon_emoji | 🏟️ |

| Field | Value |
|-------|-------|
| name  | UCL |
| icon_emoji | 🏆 |

Or just use the Admin Panel in the app to add these after login.

---

## PART 2: Android Studio Setup

### Step 8 — Open the Project
1. Open **Android Studio** (Hedgehog 2023.1.1 or newer recommended)
2. **File → Open** → Navigate to the `FootballWallpaper` folder → **OK**
3. Wait for Gradle sync to complete (first time may take 3–5 minutes downloading dependencies)

### Step 9 — Verify google-services.json
Double-check: `app/google-services.json` should be the real file from Firebase (not the placeholder).

The placeholder will show `"YOUR_PROJECT_NUMBER"` — replace it with the real download.

### Step 10 — Run the App
1. Connect a physical Android device OR start an Android Emulator (API 26+)
2. Click the **▶ Run** button (Shift+F10)
3. Select your device
4. The app will build and install

---

## PART 3: First Use

### As Regular User
1. Open app → tap **"Register"**
2. Enter email + password → **"Create Account"**
3. You'll be redirected to the Home screen
4. Browse categories, tap wallpapers, add to favorites

### As Admin
1. Open app → **"Sign In"**
2. Use: `admin@football.com` / `Admin@123` (or credentials from Step 6)
3. You'll see the **⚙ Admin Panel** icon in the top bar of Home
4. Also redirected directly to Admin Dashboard after login
5. Add categories first → then upload wallpapers

### Admin Workflow
1. **Admin Dashboard** → **"📂 Categories"** → tap **"+"** to add a category (e.g., "Clubs" with emoji "🏟️")
2. In the category, tap **"+ Sub"** to add subcategories (e.g., "Real Madrid", "Barcelona")
3. Go back to **Admin Dashboard** → **"📤 Upload Wallpaper"**
4. Tap the image area → select a photo from your device
5. Enter title, select category/subcategory, add tags (comma-separated)
6. Tap **"Upload Wallpaper"** — it uploads to Firebase Storage and saves metadata to Firestore

---

## Project File Structure

```
FootballWallpaper/
├── app/
│   ├── google-services.json          ← REPLACE THIS with real Firebase file
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/meritshot/footballwallpaper/
│           ├── FootballWallpaperApp.kt       ← @HiltAndroidApp
│           ├── MainActivity.kt               ← Entry point
│           ├── data/
│           │   ├── model/
│           │   │   ├── Models.kt             ← User, Wallpaper, Category, etc.
│           │   │   └── Result.kt             ← Sealed Result class
│           │   └── repository/
│           │       ├── AuthRepository.kt     ← Firebase Auth + Firestore user
│           │       └── WallpaperRepository.kt ← All wallpaper CRUD + Storage
│           ├── di/
│           │   └── FirebaseModule.kt         ← Hilt providers for Firebase
│           └── presentation/
│               ├── components/
│               │   └── Components.kt         ← Reusable UI components
│               ├── navigation/
│               │   └── AppNavGraph.kt        ← Navigation + role routing
│               ├── screens/
│               │   ├── auth/
│               │   │   ├── LoginScreen.kt
│               │   │   └── RegisterScreen.kt
│               │   ├── user/
│               │   │   ├── HomeScreen.kt
│               │   │   ├── CategoryDetailScreen.kt
│               │   │   ├── SubcategoryScreen.kt
│               │   │   ├── WallpaperDetailScreen.kt
│               │   │   ├── FavoritesScreen.kt
│               │   │   └── ProfileScreen.kt
│               │   └── admin/
│               │       ├── AdminDashboardScreen.kt
│               │       ├── AdminUploadScreen.kt
│               │       ├── AdminManageScreen.kt
│               │       └── AdminCategoriesScreen.kt
│               ├── theme/
│               │   └── Theme.kt              ← Dark green football theme
│               └── viewmodel/
│                   ├── AuthViewModel.kt
│                   └── WallpaperViewModel.kt
├── gradle/
│   ├── libs.versions.toml            ← All dependency versions
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts                  ← Root build file
├── settings.gradle.kts
├── firestore.rules                   ← Paste into Firebase Console
└── storage.rules                     ← Reference for Storage rules
```

---

## Firestore Data Schema

```
users/
  {uid}/
    email: "user@example.com"
    role: "user" | "admin"

categories/
  {id}/
    name: "Clubs"
    icon_emoji: "🏟️"

subcategories/
  {id}/
    category_id: "abc123"
    name: "Real Madrid"

wallpapers/
  {id}/
    title: "Ronaldo Celebration"
    image_url: "https://firebasestorage..."
    category_id: "abc123"
    subcategory_id: "def456"
    tags: ["ronaldo", "real madrid", "cr7"]
    created_at: Timestamp

favorites/
  {id}/
    user_id: "uid123"
    wallpaper_id: "wallpaper456"
```

---

## Firebase Storage Structure

```
wallpapers/
  clubs/
    uuid1.jpg
    uuid2.jpg
  players/
    uuid3.jpg
  stadiums/
    uuid4.jpg
  ucl/
    uuid5.jpg
```

---

## Production Security (Important)

### Harden Storage Rules
To enforce admin-only uploads in Storage, set a custom claim on your admin users:

Using Firebase Admin SDK (Node.js):
```javascript
const admin = require('firebase-admin');
admin.initializeApp();

admin.auth().setCustomUserClaims('ADMIN_UID_HERE', { role: 'admin' })
  .then(() => console.log('Admin claim set'));
```

After setting the claim, the admin must sign out and sign back in for it to take effect.

### Firestore Indexes
If you see "Missing index" errors in Logcat, Firestore will log a link to create the needed index automatically. Click the link in Logcat to create it.

Common indexes needed:
- `wallpapers` → `category_id ASC, created_at DESC`
- `wallpapers` → `subcategory_id ASC, created_at DESC`
- `favorites` → `user_id ASC, wallpaper_id ASC`

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `google-services.json` error on build | Download fresh copy from Firebase Console → App settings |
| "PERMISSION_DENIED" from Firestore | Check Firestore Rules are published correctly |
| Images not loading | Check Storage Rules allow authenticated reads |
| Admin panel not showing | Verify Firestore `users/{uid}` has `role: "admin"` |
| Gradle sync fails | File → Invalidate Caches → Restart |
| `ClassNotFoundException: Hilt` | Clean → Rebuild project |
| Upload fails | Check Storage Rules; ensure signed in as admin |

---

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 26+ (minSdk)
- Kotlin 2.0+
- Internet connection for Firebase

---

## Tech Stack Summary

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository pattern |
| DI | Hilt |
| Navigation | Compose Navigation |
| Auth | Firebase Authentication |
| Database | Cloud Firestore |
| File Storage | Firebase Storage |
| Image Loading | Coil |
| Async | Kotlin Coroutines + Flow |
