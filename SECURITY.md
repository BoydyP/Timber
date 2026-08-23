# Security Policy

## Scope

Timber is an offline-first Android app. It has no backend, no accounts, and the manifest declares no
`INTERNET` permission — all workout data stays in a local Room database on the device. That rules out
whole classes of issue (no server to attack, no credentials to leak, no data in transit), so the
realistic attack surface is small:

- Local data exposure — anything that lets another app on the device read the workout database.
- The exported `MainActivity` and its intent filter.
- The `TimerService` foreground service.
- Backup and device-transfer behaviour (`backup_rules.xml`, `data_extraction_rules.xml`).
- Supply chain — a malicious dependency or a tampered Gradle wrapper.

## Supported versions

This is a personal side project, so only the current `main` branch is supported. There are no
backported fixes for older tags.

## Reporting a vulnerability

Please **do not open a public issue** for a security problem.

Use GitHub's private reporting instead: go to the **Security** tab → **Report a vulnerability**.
That opens a private advisory visible only to the maintainer.

Useful things to include:

- What the issue is and roughly how severe you think it is.
- Steps to reproduce, ideally against a debug build.
- Your device/emulator API level (`minSdk` is 34).
- Whether it needs a rooted device, a malicious app already installed, or physical access — this
  changes the severity a lot.

Expect an acknowledgement within a couple of weeks. This is maintained in spare time, so please be
patient; if it turns out to be a real issue I'll credit you in the fix unless you'd rather I didn't.

## Out of scope

- Anything requiring root, an unlocked bootloader, or ADB access to the device.
- Vulnerabilities in Android itself or in third-party dependencies with no Timber-specific impact —
  report those upstream (Dependabot already tracks dependency advisories here).
- Missing hardening that has no demonstrated impact (for example, release builds ship unobfuscated
  with `isMinifyEnabled = false`; that is a deliberate trade-off for an open-source app, not a
  vulnerability on its own).
