# PepLog

PepLog is a local-first Android tracker for recording peptide or other injection protocols, scheduled doses, inventory, injection sites, subjective wellness, and personal health metrics.

It is a record-keeping and calculation aid, not medical advice. It does not diagnose, prescribe, prove treatment effectiveness, or replace a clinician, pharmacy, or the product instructions supplied by a licensed provider.

## What the app includes

- Protocols with real peptide/compound selection, dose units, administration route, schedule times, custom weekly days, and on/off cycles.
- A 365-day schedule preview with daily dose logging and inventory deduction.
- Idempotent dose completion so repeated taps do not consume the same inventory twice.
- Vial inventory, expiration reminders, lot notes, remaining-volume adjustments, and reconstitution records.
- Injection-site rotation history with pain, healing, and site-status tracking.
- A reconstitution calculator for concentration, draw volume, and syringe units.
- A local progress area for biomarkers, wellness entries, body metrics, and private progress-photo copies.
- A reference encyclopedia, pharmacokinetic visualization, and adherence/trend reports.
- Optional Health Connect import when the user grants permissions.
- Local CSV/JSON export. Google Drive backup is optional and requires the account/authentication connection to be configured for the build.

## Privacy and security posture

- PepLog has no PepLog-operated account or application server.
- App data is kept in private Android storage and excluded from Android backup extraction by default.
- The app requests notification and Health Connect permissions only for the corresponding features; Internet is used for Google Play billing and the optional Drive integration.
- Exports use Android `FileProvider` URIs rather than exposing filesystem paths.
- The app does not claim custom SQLCipher database encryption. Users should protect the device with Android security controls and treat shared exports as sensitive.
- Read the in-app policy in `app/src/main/assets/privacy_policy.html` and replace its contact/hosted-policy placeholders before release.

## Subscription model

Google Play Billing manages the subscription products. The free plan supports basic tracking and one protocol. Premium gates unlimited protocols, advanced insights, PK curves, Health Connect sync, and cloud backup. Local export remains available for data portability. Product IDs and trial terms must be configured and tested in Play Console before publishing.

## Build locally

```text
./gradlew test
./gradlew :app:assembleDebug
```

The app module targets API 36, with a minimum API level of 28. Release packaging intentionally fails unless an ignored `keystore.properties` file points to the real release keystore. Never commit that file or any signing credential.

Before a Play release, configure the release keystore, package name and Play products, the hosted privacy-policy URL and support contact, Google Drive OAuth/account flow if cloud backup is offered, Health Connect declarations, store assets, Data Safety answers, content rating, and real-device billing/Health Connect/backup journeys.

