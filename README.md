# Wemmies

Wemmies is an Android application providing a safe, anonymous space to share difficult emotions ("Wemmies") and receive community empathy. When a Wemmie receives 5 empathy responses, it transforms into a positive state representing healing.

---

## Key Features & Architecture

1. **Onboarding (`OnboardingActivity`)**: Google & Anonymous Authentication with auto-generated random nicknames and server-side privacy guarantees.
2. **Community Feed (`FeedActivity`)**: Real-time stream of public Wemmies sorted by empathy count, powered by `DiffUtil` for smooth rendering.
3. **Spill (`SpillActivity`)**: Creation screen for writing thoughts and tagging emotions.
4. **Empathy & Transformation (`WemmieDetailActivity`)**: Send empathy responses with a strict limit of one per user per Wemmie.
5. **Profile & Personal Details (`ProfileActivity` & `MyWemmieDetailActivity`)**: Protected by AndroidX Biometric/PIN lock. Displays personal stats, customizable avatar/nickname, and real-time received support messages.

---

## Technical Stack

- **Core**: Java, Android SDK (API 24+), Material Design 3, RecyclerView (`DiffUtil`)
- **Security**: AndroidX Biometric (`androidx.biometric`), Device Credentials
- **Firebase**: Authentication, Cloud Firestore, Analytics, Crashlytics

---

## Authors

**Brenna Hymowitz** & **Matthew Borlak**