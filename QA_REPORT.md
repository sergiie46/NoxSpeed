# NoxSpeed v1.0 — QA report

Generated: 2026-09-13

## Passed in this environment
- XML/resource parsing: PASS.
- Kotlin source syntax parse: no parser/syntax errors detected (platform/dependency symbols cannot resolve without Android SDK classpath).
- `R.string` reference coverage: PASS.
- English/Spanish string parity: 149/149.
- Play Store title/short-description length checks: PASS.
- Store graphics dimensions: PASS (512×512 icon, 1024×500 feature graphic, 8× ES + 8× EN screenshots at 1080×1920).
- Background-location permission scan: PASS — none declared.
- GPS filter + unit-conversion pure Kotlin checks: PASS.
- Production config centralization: PASS — AdMob/Billing IDs live in `monetization.properties`.

## Android Gradle build status
A real `assembleDebug`/`bundleRelease` could not be completed inside this execution environment because it has no Android SDK installed and outbound DNS is blocked. The Gradle wrapper bootstrap attempted to reach `services.gradle.org` and failed with `UnknownHostException` before Gradle/dependencies could be downloaded.

This is an environment limitation, not a successful compile result. Do not interpret this report as claiming that an Android Gradle build was executed successfully.

## Current verified build matrix
- AGP 9.4.0
- Gradle 9.6.0
- JDK 17+
- Kotlin / Compose compiler plugin 2.3.21
- KSP 2.3.12
- compileSdk 37
- targetSdk 36
- minSdk 23
- Compose UI 1.12.1
- Material 3 1.4.0
- Room 2.8.5
- DataStore 1.2.1
- Google Play Billing 9.1.0
- Google Mobile Ads 25.4.0
- Google UMP 4.0.0

## Release blockers intentionally remaining
1. Replace the four Google AdMob test IDs with production IDs.
2. Create/confirm the Play one-time product ID (`premium_lifetime` by default).
3. Host the supplied legal/web pages at the configured NoxForge Studios URLs.
4. Generate the release signing key/AAB in Android Studio or CI; private signing keys are intentionally not bundled.
