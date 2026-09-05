# PepLog

PepLog is a local-first Android tracker for recording peptide or other injection protocols, scheduled doses, inventory, injection sites, subjective wellness, and personal health metrics.

It is a record-keeping and calculation aid, not medical advice. It does not diagnose, prescribe, prove treatment effectiveness, or replace a clinician, pharmacy, or the product instructions supplied by a licensed provider.

## What the app includes

- Protocols with catalog or user-created compound selection, dose units, administration route, schedule times, titration steps, custom weekly days, and on/off cycles.
- A 365-day schedule preview with daily dose logging and inventory deduction.
- Idempotent dose completion so repeated taps do not consume the same inventory twice.
- Vial inventory, expiration reminders, lot notes, remaining-volume adjustments, and reconstitution records.
- Injection-site rotation history with pain, healing, and site-status tracking.
- A reconstitution calculator for concentration, draw volume, and syringe units.
- A local progress area for biomarkers, wellness entries, body metrics, and private progress-photo copies.
- A reference encyclopedia, an elimination-only relative-amount PK visualization with honest forecast labeling, and adherence/trend reports.
- Optional Health Connect import when the user grants permissions.
- Local CSV/JSON export, a clinician summary PDF, and a photo-inclusive restore-ready ZIP through Android's system share sheet. PepLog does not upload exports by itself.
- Optional local dose reminders and background inventory checks when notification permission is granted.

## Privacy and security posture

- PepLog has no PepLog-operated account or application server.
- App data is kept in private Android storage and excluded from Android backup extraction by default.
- The app requests notification and Health Connect permissions only for the corresponding features. This build makes no PepLog network requests and has no cloud-sync integration.
- Exports use Android `FileProvider` URIs rather than exposing filesystem paths.
- Optional App lock uses Android's existing device credential; PepLog never stores a PIN, password, or biometric template. It is an access gate, not a custom database-encryption layer. Treat shared exports as sensitive.
- Every feature is free to use: there are no subscriptions, in-app purchases, ads, or account requirement.
- Read the in-app policy in `app/src/main/assets/privacy_policy.html` and replace its contact/hosted-policy placeholders before release.

## Product positioning

PepLog should be presented as a private personal record and inventory workflow: capture what was logged, when it happened, what is on hand, and what the user wants to review. The PK visualizer is an educational, relative estimate with explicit limitations—not a treatment prediction or dosing recommendation.

## Build locally

```text
./gradlew test
./gradlew :app:assembleDebug
```

The app module targets API 36, with a minimum API level of 28. Signed release packaging intentionally fails unless an ignored `keystore.properties` file points to the real release keystore; `packageRelease` is useful for producing an unsigned local inspection APK. Never commit that file or any signing credential.

Before a Play release, configure the release keystore and package name, publish the privacy policy at a stable public URL with a verified support contact, prepare Health Connect declarations, store assets, Data Safety answers, content rating, and complete real-device permission/export/reminder/app-lock journeys.

