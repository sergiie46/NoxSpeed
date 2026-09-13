# NoxSpeed — GPS Speedometer & HUD

Android native app by **NoxForge Studios**.

## What is already wired
- Kotlin + Jetpack Compose, compileSdk 37 and targetSdk 36.
- GPS speed, heading, altitude, accuracy and GNSS satellite count.
- Conservative GPS smoothing and spike rejection.
- 13 visual dashboard styles, 6 mirrored landscape HUD styles, trip computer, local history, weekly stats and records.
- Speed alert, debug GPS simulator, acceleration timer, JSON export/import.
- Room + DataStore persistence.
- Google UMP consent, AdMob test ads, Billing 9.1 lifetime Premium, restore purchases and in-app review.
- Spanish + English resources.
- Privacy/Terms/Play Store listing assets in `LEGAL/`, `STORE/` and `GRAPHICS/`.

## Before release — only external IDs/data
Edit **`monetization.properties`** only:
1. Replace `ADMOB_APP_ID`.
2. Replace `ADMOB_BANNER_ID`.
3. Replace `ADMOB_INTERSTITIAL_ID`.
4. Replace `ADMOB_REWARDED_ID`.
5. Keep/create Play Console one-time product `premium_lifetime`, or replace `PREMIUM_PRODUCT_ID`.
6. The bundled web/legal pages target `https://noxforgestudios.netlify.app/`; deploy the supplied `WEB/` pages there (or change the central URLs).

The included AdMob values are Google's official test IDs. Never ship the test values as your production monetization configuration.

## Build
- Android Studio: use a current build capable of installing Android SDK Platform 37.
- JDK: **17+** (AGP 9.4 requires JDK 17; Android Studio's bundled JDK is recommended).
- Open the root `NoxSpeed` folder, Sync Gradle, then Build > Generate Signed App Bundle.
- CLI: `gradlew.bat :app:bundleRelease` on Windows or `./gradlew :app:bundleRelease` on macOS/Linux.

The wrapper bootstrap included in this generated project downloads Gradle 9.6 on first use.

## Google Play
Use the files in `STORE/` for the listing and declarations. Upload the privacy/terms HTML in `LEGAL/` to your NoxForge website and use the final public URLs in Play Console and `monetization.properties`.

## Important behavior
This first release deliberately records location **only while the app is foregrounded**. It does not request background-location permission and does not run a hidden location service. That keeps permissions, battery use and Play policy scope smaller. GPS coordinates are used for local trip calculations and are not included in the saved trip database; only summary statistics are stored.
