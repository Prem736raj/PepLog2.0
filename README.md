What is PepLog and Why Does It Exist?
PepLog is a comprehensive, medical-grade Peptide Therapy & Protocol Management App designed for individuals undergoing peptide treatments, hormone therapy (TRT/HRT), GLP-1 weight-loss protocols (like Semaglutide/Tirzepatide), injury recovery (BPC-157, TB-500), and biohacking/wellness regimens.

The Real-World Problem PepLog Solves
Peptides are not like standard daily pills. Managing them in real life is complex, risky, and error-prone for several reasons:

Dangerous Dosage Math: Peptides come as freeze-dried powder in vials. Users have to add bacteriostatic water (reconstitution) and calculate tiny microgram ($\mu\text{g}$) doses onto insulin syringe tick marks. One wrong decimal point can cause a 10x overdose.
Short Half-Lives: Some peptides last only 4 hours in the blood; others last 7 days. Missing doses or dosing at the wrong time ruins treatment efficacy.
Scar Tissue & Injection Fatigue: Repeated injections in the same area cause lumps, scar tissue (lipohypertrophy), and poor absorption.
Perishable Vials: Reconstituted peptides spoil inside the fridge within 28–60 days.
Complex Multi-Peptide Stacks: Taking 3–4 compounds with different cycling rules (e.g., 5 days on / 2 days off, morning fasted vs. bedtime).

 
 Feature-by-Feature Breakdown: Why Every Part is Necessary
Here is what every major module in PepLog does and why it was built:

                  ┌─────────────────────────────────────┐
                  │               PepLog                │
                  │     (Peptide Optimization Hub)      │
                  └──────────────────┬──────────────────┘
                                     │
      ┌────────────────┬─────────────┼───────────────┬────────────────┐
      ▼                ▼             ▼               ▼                ▼
 🧪 Calculator    💉 Protocols    📈 PK Curves   📍 Body Map     📦 Inventory
 (Reconstitution  (Schedules &    (Blood Serum    (Site Rotation  (Vial Shelf Life
 & Syringe Math)  Multi-Stacks)   Half-Life)      & Scar Care)    & Dosages Left)

 
1. 🧪 Peptide Reconstitution & Syringe Calculator
What it does: You input vial size (e.g., $5\text{ mg}$), water added (e.g., $2\text{ mL}$), and target dose (e.g., $250\text{ }\mu\text{g}$). The app renders an interactive syringe showing the exact tick mark to pull back to.
Why it’s necessary: Prevents dangerous math mistakes and accidental overdoses.

2. 💉 Protocol & Multi-Stack Scheduler
What it does: Lets users create schedules for single compounds or complex "stacks" (e.g., BPC-157 + TB-500 Healing Stack), with reminders, cycling rules (5 days on / 2 days off), and daily dose checklists.
Why it’s necessary: Ensures treatment consistency and prevents users from forgetting doses or losing track of cycle days.

3. 📈 Pharmacokinetic (PK) Curves Visualizer
What it does: Computes and graphs blood serum concentration levels over time based on scientific half-life equations ($C(t) = C_0 \cdot e^{-kt}$).
Why it’s necessary: Allows users to see peak concentration times and avoid concentration troughs, showing whether levels in their body are stable or fluctuating wildly.

4. 📍 Interactive Body Map & Injection Site Tracker
What it does: An interactive diagram of the human body (abdomen, deltoids, glutes, thighs). It logs each injection location and color-codes areas (e.g., Red = Injected $<48\text{h}$ ago, Yellow = Healing, Green = Ready).
Why it’s necessary: Prevents injecting into the same tissue repeatedly, avoiding lipohypertrophy, scar tissue buildup, and infection.

5. 📦 Vial Inventory & Expiration Tracker
What it does: Tracks vials in storage and in the fridge. Calculates remaining volume, doses left, and warns when a reconstituted vial is nearing its 28-day expiration date.
Why it’s necessary: Reconstituted peptides degrade rapidly. This prevents users from injecting expired, ineffective, or bacteria-contaminated solutions and lets them know when to reorder.

7. 📖 Built-in 100+ Peptide Encyclopedia
What it does: A searchable medical library containing detailed profiles for over 100 peptides (GLP-1s, Growth Hormone Secretagogues, Healing/Tissue Repair, Nootropics, Longevity). Shows typical dosage ranges, reconstitution advice, storage rules, and research tiers.
Why it’s necessary: Users have instant access to safety and dosing information without needing to browse unverified forums.

8. 📊 Daily Wellness Logs & Health Connect Integration
What it does: Syncs with Android Health Connect to track biometric data (Weight, Heart Rate, Blood Pressure) alongside subjective logs (Mood, Energy, Pain, Sleep quality).
Why it’s necessary: Helps users and doctors evaluate whether a peptide protocol is producing desired results (e.g., weight loss progress on Semaglutide or pain reduction on BPC-157).

10. 🔒 Security & Privacy (Biometrics, SQLCipher, Offline-First)
What it does: 100% offline-first architecture with biometric authentication (Fingerprint/Face Unlock), local encrypted Room database (SQLCipher), and tamper detection.
Why it’s necessary: Health and peptide usage data is sensitive personal medical information. It should never leak or be stored on unencrypted remote servers without user consent.

