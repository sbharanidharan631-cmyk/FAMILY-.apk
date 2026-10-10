# Family Memories — Android Project & APK Build Guide

A personal, device-local family photo organiser with a luxury black-and-gold design.

## Features
- Categories: Father, Mother, Brother, Sister, Me, Family, Grandparents, Friends
- Custom categories support (e.g. Cousins, Pets, Birthdays, Holidays)
- Pick single or multiple photos via Android Photo Picker
- Group & filter photos by category
- Remove photos from category
- 100% device-local storage (No remote servers or cloud tracking)
- High-contrast black and gold luxury UI

## How to Build the APK File

### Method 1: Using Android Studio (Local Computer)
1. Download and extract this ZIP folder.
2. Download Android Studio from https://developer.android.com/studio.
3. Open Android Studio and click **Open**, then select the extracted folder.
4. Allow Gradle to sync. If prompted, install Android SDK Platform 35.
5. In the top menu, click **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. Once compiled, Android Studio will display a popup: "APK(s) generated successfully".
7. Click "locate" to find your file at:
   `app/build/outputs/apk/debug/app-debug.apk`
8. Send this .apk file to your phone (via USB, Google Drive, WhatsApp, or email) and tap to install!

### Method 2: Cloud 1-Click Build via GitHub Actions (No Android Studio required!)
1. Create a free new repository on GitHub (e.g. `family-memories-apk`).
2. Upload the contents of this ZIP file into that repository.
3. GitHub Actions will automatically run the included `.github/workflows/build-apk.yml` file!
4. Go to the **Actions** tab on GitHub, click the latest workflow run, and download the compiled `app-debug.apk` under **Artifacts**!
