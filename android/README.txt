╔══════════════════════════════════════════════════╗
║   English Master — Android APK Build Guide       ║
╚══════════════════════════════════════════════════╝

OPTION A: Build Online (No setup needed — EASIEST)
───────────────────────────────────────────────────
1. Go to: https://appetize.io or use GitHub Actions

For GitHub Actions (free):
1. Upload this 'android' folder to GitHub repo
2. Go to Actions tab → run the workflow
3. Download the APK from Artifacts

OPTION B: Build with Android Studio (Local)
────────────────────────────────────────────
Requirements:
• Android Studio (free): https://developer.android.com/studio
• Java JDK 17+

Steps:
1. Open Android Studio
2. File → Open → select the 'android' folder
3. Wait for Gradle sync (~5 minutes first time)
4. Build → Build APK(s)
5. APK is at: android/app/build/outputs/apk/debug/app-debug.apk

OPTION C: Build with Command Line
───────────────────────────────────
Requirements: Java JDK 17+, Android SDK

cd android
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk

INSTALL ON ANDROID:
───────────────────
1. Enable "Unknown sources" on your phone:
   Settings → Security → Install unknown apps → Allow
2. Copy app-debug.apk to phone
3. Tap to install

APP FEATURES:
─────────────
• Full English Master B1→C1 learning app
• Works OFFLINE (all content built-in)
• localStorage saves all progress
• Speech synthesis for pronunciation
• Dark theme optimized for mobile
