# PepLog beta test script

Use this with 5–10 adults who already keep medication, supplement, or health records. Ask testers to use sample data unless they explicitly choose otherwise. Do not collect their health details in the feedback form.

## Tasks

1. Start from a clean install. From onboarding, tap **Set up a first reminder**, choose a compound, enter a known amount, choose a time, and finish. Record whether this takes less than one minute and where the tester hesitates.
2. Accept notifications once and deny them once on separate test installs. Confirm the app explains the outcome and remains usable either way.
3. From the empty dashboard, repeat the fast setup path. Then create a second, more detailed protocol through the advanced editor.
4. Log a scheduled dose, tap the same dose again, and confirm the history does not duplicate the event or deduct inventory twice.
5. Open Inventory, add sample vial data, review the remaining amount and expiry warning, then export JSON/CSV/PDF and the restore-ready ZIP. On a clean install, restore the ZIP and verify the records and photos are present.
6. On a device with a screen lock, enable **More → Privacy & data → App lock**. Background and reopen PepLog, cancel authentication once, then unlock it. Confirm recent-app screenshots are protected while the lock is enabled.
7. Open the reference library, calculator, progress, reports, PK visualizer, and optional Health Connect entry. Record unclear labels, dead ends, slow screens, and any wording that sounds like a treatment recommendation.
8. Repeat the core flow on the slowest available device and with connectivity disabled. PepLog should still create, view, log, and export local records.

## Feedback to capture

- Device model, Android version, and whether the tester uses a screen lock.
- Time to first reminder, completion or failure for each task, and the exact point of confusion.
- Any crash, lost data, duplicate dose, incorrect inventory change, blocked back action, or unreadable screen.
- Whether the app feels calm and trustworthy rather than clinical, sales-led, or overloaded.
- The one feature they would return for weekly and the one thing they would remove.

Do not describe the PK visualizer as a dosing or treatment tool during testing. It is an educational, relative estimate with limitations.
