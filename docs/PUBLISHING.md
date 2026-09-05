# Publishing BabyBloom

Destination organization: [BabyBloom-Team](https://github.com/BabyBloom-Team).

This checklist separates publishing the project's source and profile from distributing a production Android app. Creating GitHub repositories does not publish an app to Google Play.

## 1. Organization profile

Create a public repository named `.github` owned by `BabyBloom-Team`.

Copy the contents of [organization-profile/README.md](organization-profile/README.md) into `profile/README.md` in that repository. The file in this Android project is a staging copy; GitHub only uses the file in the organization's special `.github` repository for its public profile.

Suggested organization display name: **BabyBloom**.

Suggested description: **An Android graduation project for interactive early learning, adaptive activities, and parent progress insights.**

Add the team's approved logo and contributor memberships when available. Do not add personal contact details without the person's agreement.

Official instructions: [Customize your organization's profile](https://docs.github.com/en/organizations/collaborating-with-groups-in-organizations/customizing-your-organizations-profile).

## 2. Application repository

Create `BabyBloom-Team/BabyBloom`. Start privately while the source and history review is unfinished. Leave the new repository empty: do not initialize it with a README, license, or `.gitignore`, because those files or history will come from the existing local project.

The existing local `origin` points to `naghamagha5/BabyBloom`. Publishing a copy into the new organization should retain the existing Git history and authors. It does not transfer ownership of the original repository.

After the destination exists, a separate local remote can be added:

```powershell
git remote add organization https://github.com/BabyBloom-Team/BabyBloom.git
git remote -v
```

Review and commit the intended local changes before pushing the chosen branch. Do not automatically stage all untracked files or mirror every branch and tag. Confirm which project changes belong in the publication, and inspect all history that will be uploaded.

The setup above does not provide authentication. The account used to push must have write access to the organization repository. Sign in through an official GitHub or Git Credential Manager flow; do not paste passwords or tokens into chat or source files.

## 3. Before making source public

- [ ] Review the working tree and the Git history being published for credentials, personal information, local database files, and private documents.
- [ ] Revoke and replace any previously exposed credentials; deleting a secret from the latest file does not remove it from history.
- [ ] Keep `local.properties`, signing keys, signing passwords, and generated app bundles out of Git.
- [ ] Agree on a project license with the owners and contributors, and add the approved license text.
- [ ] Verify redistribution rights and required credit for bundled learning media and other assets.
- [ ] Complete team acknowledgments without removing existing authorship.
- [ ] Build from a clean checkout using the documented setup.
- [ ] Add screenshots and a short demo using fictional data, then link them from the README.
- [ ] Make the reviewed repository public and pin it on the organization profile.

The added ignore patterns prevent matching untracked files from being staged normally. They do not remove already tracked files or clean Git history. No complete history or asset-rights audit is implied by this checklist.

## 4. Before distributing the Android app

The initial source inspection identified the following outstanding items:

- [ ] Replace direct production Gemini calls with a protected backend that keeps the API key server-side, verifies callers, and limits usage, or explicitly disable live AI in the demo build. The current Gradle configuration embeds the configured key in `BuildConfig`.
- [ ] Replace plain SHA-256 password hashing and plan compatibility for existing local users. Decide whether the release uses local profiles or introduces cloud accounts.
- [ ] Provide and test database migrations for supported upgrade paths before removing `fallbackToDestructiveMigration()`. Missing paths currently permit database erasure.
- [ ] Define backup, restore, and deletion behavior for account and child data; review the current permissive/sample backup configuration.
- [ ] Review AI request content, camera and microphone behavior, permissions, and child-data handling. Prepare a privacy policy based on actual behavior.
- [ ] Review seed/demo data and make sure real users start with the intended onboarding state.
- [ ] Test a fresh installation, an upgrade containing existing progress, denied permissions, missing speech services, and connectivity loss on supported devices.
- [ ] Run unit tests and Android lint, and resolve release-blocking failures.

Room can remain the local database. Cross-device accounts, recovery, and shared parent access are separate features requiring a backend and synchronization design.

## 5. Signed releases

1. In Android Studio, choose **Build > Generate Signed Bundle / APK**.
2. Choose **APK** for a downloadable release or **Android App Bundle** for Google Play.
3. Create or select the intended signing credentials. Keep keys and passwords private and back them up securely.
4. Build and test the release variant. Do not distribute a build containing a private Gemini key.
5. For each subsequent release, increase `versionCode`, update `versionName`, and preserve the signing identity required for updates.
6. For direct distribution, attach the signed APK to a versioned GitHub Release with installation steps, supported Android versions, known limitations, and changes.
7. For Google Play, complete account verification, store assets, privacy and Data safety disclosures, target audience, content rating, reviewer access, and applicable testing requirements before requesting production access.

Current local configuration: Android API 28 minimum, API 36 target, application ID `com.babybloom`, version code `1`, version name `1.0`. Check package-name availability and current Play requirements before the first release.

## References

- [Android app signing](https://developer.android.com/studio/publish/app-signing)
- [Room migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Gemini API key security](https://ai.google.dev/gemini-api/docs/api-key)
- [GitHub repository licensing](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/licensing-a-repository)
- [Google Play testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465)
- [Google Play Families policies](https://support.google.com/googleplay/android-developer/answer/9893335)
