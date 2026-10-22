# MessMate

**Know what's on the menu.**

MessMate is a native Android app for hostel dining. It gives students a calm, glanceable view of today's meals, the week ahead, and dining notices. A local admin preview can update meal items and publish notices from the same device.

## Run the app

1. Open this `MessMate` folder in Android Studio.
2. Use JDK 17 and install Android SDK platform 37 and Build Tools 36.0.0.
3. Let Android Studio sync the Gradle project, then run the `app` configuration on an Android 8.0+ emulator or device. From a terminal, `gradlew.bat :app:assembleDebug` builds the debug APK.

This project uses AGP 9.4.0, Gradle 9.6, Kotlin 2.4.10's Compose compiler plugin, and the Jetpack Compose BOM. Its included Gradle wrapper resolves the pinned dependencies from Google Maven and Maven Central. The current workspace does not include an Android SDK or a Gradle installation, so the APK has not been built here.

## Student and admin access

Students enter directly from the landing screen without an ID or password. Admins sign in with a 10-digit ID from the first sheet of `ID.xlsx` and the initial password `1234567890`. On a fresh install, the first approved ID to sign in becomes the local admin owner. Admins can change their password in **Profile → Account settings**.

The current `ID.xlsx` roster is bundled as SHA-256 ID hashes in `app/src/main/assets/admin_id_hashes.txt`. To add an admin, first add their 10-digit ID to column A of the workbook's first sheet, then have an admin choose **Profile → Import ID.xlsx** and select the updated workbook. They can then add that ID under **Admin access**. Passwords, active admin accounts, menu edits, favorites, and notices are stored on the device.

This prototype keeps accounts and data on one device. It does not provide shared accounts across devices or server-backed authentication.

## App store listing

**MessMate — Know what's on the menu.** See what's cooking today and plan the week ahead. Browse meal times, save the dishes you love, and get dining hall updates in one thoughtful place. MessMate keeps hostel dining simple for students and makes menu updates easy for the people who serve them.

## Production integration

The included student/admin experience is an offline product prototype. Before publishing a campus deployment, connect authentication and role authorization to a trusted service, sync menus and notices through a shared backend, and configure the institution's dining halls and notification policy.
