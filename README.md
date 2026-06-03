# Vidyarthi-Bus 🚌

**Crowdsourced bus crowd-alert app for rural college students in Maharashtra.**

Students on the bus report crowd status (Empty / Seated / Full). Students waiting at the next stop see the data in real-time to decide whether to wait or find an alternative.

---

## Project Structure

```
VidyarthiBus/
├── app/
│   ├── google-services.json        ← REPLACE with your Firebase file
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/vidyarthi/bus/
│       │   ├── activities/
│       │   │   ├── SplashActivity.java          Screen 1: Branding + anon sign-in
│       │   │   ├── MainActivity.java            Screen 2: Route list
│       │   │   ├── CrowdDetailActivity.java     Screen 3: Live crowd meter
│       │   │   ├── ReportActivity.java          Screen 4: Report crowd (with location check)
│       │   │   └── AlternativesActivity.java    Screen 5: Shared autos & alternatives
│       │   ├── adapters/
│       │   │   ├── RouteAdapter.java
│       │   │   ├── StopAdapter.java
│       │   │   └── AlternativeAdapter.java
│       │   ├── models/
│       │   │   ├── BusRoute.java
│       │   │   ├── BusStop.java
│       │   │   ├── CrowdReport.java             Holds EXPIRY_MS = 15 min
│       │   │   └── Alternative.java
│       │   └── utils/
│       │       ├── FirebaseHelper.java          All Realtime DB + Auth operations
│       │       ├── LocationHelper.java          GPS + geofence check (500m radius)
│       │       └── RouteDataProvider.java       Seed data: 3 routes, 4 alternatives
│       └── res/
│           ├── layout/                          All 5 activity + 3 item layouts
│           ├── drawable/                        Crowd dots, progress bar, pill bg
│           ├── anim/                            Slide/fade transitions
│           ├── values/                          colors, strings, themes, dimens
│           └── menu/                            Toolbar menu (Alternatives link)
├── firebase/
│   └── database.rules.json         ← Deploy to Firebase Console > Rules
└── README.md
```

---

## Setup Instructions

### Step 1 — Create a Firebase Project

1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Create a new project: `vidyarthi-bus`
3. Add an **Android app** with package name `com.vidyarthi.bus`
4. Download `google-services.json` and **replace** `app/google-services.json`

### Step 2 — Enable Firebase Services

In Firebase Console:
- **Realtime Database** → Create database → Start in **test mode** (then apply rules below)
- **Authentication** → Sign-in method → Enable **Anonymous**

### Step 3 — Deploy Database Rules

Copy `firebase/database.rules.json` content into:
Firebase Console → Realtime Database → Rules tab

```json
{
  "rules": {
    "routes": {
      "$routeId": {
        "reports": {
          "$uid": {
            ".read":  "auth != null",
            ".write": "auth != null && auth.uid === $uid"
          }
        }
      }
    }
  }
}
```

### Step 4 — Build in Android Studio

1. Open Android Studio → **Open** → select `VidyarthiBus/` folder
2. Let Gradle sync (may take 2–3 minutes on first run)
3. Connect a device or start an emulator (min API 24 = Android 7.0)
4. Press **Run ▶**

---

## How the Key Features Work

### Real-time Crowd Meter
- Firebase Realtime DB path: `/routes/{routeId}/reports/{uid}`
- `CrowdDetailActivity` attaches a `ValueEventListener` → fires on every change
- Reports older than **15 minutes** are filtered out client-side
- Crowd % is a weighted average: Empty=0, Seated=1, Full=2 → normalized to 0–100
- The `ProgressBar` is tinted green/amber/red based on thresholds (33% / 66%)

### Location-Locked Reporting (Success Criterion)
- `ReportActivity` calls `LocationHelper.getCurrentLocation()` on start
- `LocationHelper.isNearAnyStop()` checks distance to every stop on the route
- If the user is **> 500 metres** from all stops → cards are disabled, submit blocked
- This prevents off-route or fake reports

### 15-Minute Report Expiry
- Each `CrowdReport` stores a `timestamp` (System.currentTimeMillis())
- `FirebaseHelper.listenToCrowd()` filters: `now - timestamp < 900_000`
- Stale reports are invisible to all users automatically — no server-side job needed

### Anonymous Authentication
- Students don't need to register — `FirebaseAuth.signInAnonymously()` runs on splash
- Each user's report is keyed by their UID: one active report per user per route
- Re-reporting overwrites the previous one

---

## Customising Routes

Edit `RouteDataProvider.java` to add your college's actual routes and GPS coordinates.
In production, push route data to `/routes` in Firebase and load it at app start instead.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java (Android) |
| Min SDK | API 24 (Android 7.0) |
| Real-time DB | Firebase Realtime Database |
| Auth | Firebase Anonymous Auth |
| Location | Google Play Services FusedLocationProvider |
| UI | Material Components, ConstraintLayout, RecyclerView |
| Animations | Lottie (integrated, ready to use) |
