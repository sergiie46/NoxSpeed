# Google Play Data Safety — implementation guide

This guide matches the generated v1.0 implementation. Re-check Google/AdMob SDK disclosure pages when publishing because SDK behavior and Play questions can change.

## NoxSpeed first-party behavior
- Precise/coarse device location is read while the app is in use to calculate speed, distance, heading and altitude.
- Raw coordinates are not persisted in the trip database and are not uploaded to a NoxForge server.
- Trip summary data (time, distance, speed statistics, altitude range, smoothness score) is stored locally.
- No user account or NoxForge analytics backend is included.

## Third-party SDKs included
- Google Mobile Ads SDK (free users): may process IP address, device/advertising identifiers, user product interactions and diagnostic information according to Google's SDK disclosure and consent choices. The app does not pass GPS coordinates into ad requests.
- Google User Messaging Platform: manages privacy/consent choices.
- Google Play Billing: processes purchase and entitlement information through Google Play.
- Google Play In-App Review: opens Google's review flow.

## Play Console approach
When Play asks whether the app collects/shares data, account for data collected or transmitted off-device by bundled SDKs, not only data handled by your own code. Therefore do NOT simply declare "no data collected" while AdMob is enabled.

Likely categories to review against Google's current Mobile Ads disclosure:
- Device or other IDs.
- App interactions.
- Diagnostics.
- Approximate location inferred by network/IP where applicable to the SDK.

For precise location: NoxSpeed itself processes it on-device and does not transmit it off-device. Answer Play's question according to its current definition of "collected" at submission time.

Security practices:
- Data in transit used by Google SDKs uses their transport/security implementation.
- Users can erase local trip/stat data from Settings.
- No account deletion flow is necessary because NoxSpeed does not create accounts.
