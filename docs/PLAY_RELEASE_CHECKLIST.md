# PepLog Play release checklist

## Completed in the repository

- All PepLog features are free. There is no Play Billing dependency, subscription state, paywall, purchase flow, billing permission, or billing-only network configuration.
- Optional App lock delegates authentication to the Android device credential and enables secure-window protection.
- First-use setup creates a local protocol, compound, daily schedule, and reminder in one transaction.
- Local export/restore, notification scheduling, and the Room 4→5 removal of the obsolete device table have automated coverage or build validation.
- `test`, `:app:assembleDebug`, `:app:lintDebug`, and unsigned `:app:packageRelease` are the local gates to rerun before publishing.

## Owner actions before publishing

- Create or select the permanent release keystore. Copy `keystore.properties.template` to the ignored `keystore.properties`, fill it locally, and never commit the file or its passwords. Verify the final application ID and signing certificate in Play Console.
- Publish `app/src/main/assets/privacy_policy.html` at a stable public HTTPS URL. Replace the placeholder contact address with a monitored address and make the hosted copy match the shipped build.
- Prepare the Play listing assets, screenshots, support URL, app category, age/content rating, and store description. Lead with private local records, reminders, inventory, and export/restore; do not make treatment, efficacy, or dosing claims.
- Complete the Data Safety form from the shipped behavior: local health/record data, optional Health Connect access, local notifications, and user-requested exports/sharing. Confirm the answers again after any SDK or analytics change.
- Review Health Connect declarations and the notification permission rationale in Play Console. Explain why each is needed and remove anything not used by the release.
- Build a signed AAB and verify it with Play Console's internal testing track. Confirm the release contains no billing permission or payment UI.

## Physical-device gate

Test at least one low-end and one recent Android device across supported API levels. Cover fresh install, upgrade from a pre-change database, offline use, denied/granted notifications, reminder delivery, app lock cancel/success, screenshot protection, Health Connect unavailable/available, photo export/restore, rotation, process death, and back navigation. Keep the beta script in `docs/BETA_TEST_SCRIPT.md` with the tester's consent and no personal health data.
