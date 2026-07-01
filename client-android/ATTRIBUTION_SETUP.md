# Attribution Setup

This project now supports a unified attribution flow with these sources:

- launcher intent / custom scheme deep links
- Android App Links
- startup clipboard attribution
- Facebook Deferred App Links
- Facebook Install Referrer
- Google Play Install Referrer
- AppsFlyer Unified Deep Linking
- AppsFlyer install conversion data

## Required local configuration

Add the following keys to `gradle.properties` or your user-level Gradle properties:

```properties
APPSFLYER_DEV_KEY=your_appsflyer_dev_key
FACEBOOK_APP_ID=fb1234567890
APP_LINK_HOST=your.domain.com
```

`APPSFLYER_DEV_KEY` is optional. If it is blank, AppsFlyer remains disabled.

`FACEBOOK_APP_ID` is optional. It can be provided either as `fb123...` or plain numeric id.

`APP_LINK_HOST` defaults to `link.simplewebview.local` if you do not override it.

## Facebook Attribution

Facebook support is split into two providers:

- deferred app links through the Meta `facebook-applinks` SDK
- Facebook-classified install referrer through Google Play Install Referrer

The install referrer branch intentionally only emits when the raw referrer looks like Facebook traffic.
Non-Facebook referrers continue down the Google install referrer branch.

When `FACEBOOK_APP_ID` is blank, both Facebook providers stay dormant.

## Clipboard Attribution

The app now inspects clipboard text once on startup through a dedicated runtime provider.

Current behavior:

- read the primary clipboard text on app startup
- only continue when the text looks like an attribution payload or deep link
- skip duplicates that have already been consumed before
- report clipboard traffic through the clipboard upload bucket

The debug page will show this source as `clipboard`.

## Android App Links

The launcher activity declares a verified App Links intent filter for both `http` and `https`.

To make it work in production:

1. Set `APP_LINK_HOST` to your real domain.
2. Publish `https://<your-domain>/.well-known/assetlinks.json`.
3. Add your release signing fingerprint from Play App Signing or your keystore.

Official Android guidance:

- [About App Links](https://developer.android.com/training/app-links/about)
- [Add intent filters for App Links](https://developer.android.com/training/app-links/add-applinks)
- [Configure website associations](https://developer.android.com/training/app-links/configure-assetlinks)

## Firebase Dynamic Links

Firebase Dynamic Links should not be added to this project.

According to Firebase's official documentation, Dynamic Links is deprecated, and the deprecation FAQ states the service shut down on August 25, 2025:

- [Receive Firebase Dynamic Links on Android](https://firebase.google.com/docs/dynamic-links/android/receive)
- [Dynamic Links Deprecation FAQ](https://firebase.google.com/support/dynamic-links-faq)

Use Android App Links instead.

## Quick test commands

```bash
adb shell am start -W -a android.intent.action.VIEW -d "simplewebview://booking" com.example.simplewebview
adb shell am start -W -a android.intent.action.VIEW -d "simplewebview://route?screen=activity" com.example.simplewebview
adb shell am start -W -a android.intent.action.VIEW -d "simplewebview://openwith?bookid=123&needOpen=1" com.example.simplewebview
adb shell am start -W -a android.intent.action.VIEW -d "https://your.domain.com/openwith?bookid=123&needOpen=0" com.example.simplewebview
adb shell am start -W -a android.intent.action.VIEW -d "simplewebview://route?screen=debug_attribution" com.example.simplewebview
```

You can also open the in-app debug page by long-pressing the app name on the login screen.

When Facebook is configured, the debug page will also show `facebook`, `facebook-deferred`, and `facebook-referrer` stages.
Clipboard inspection appears as `clipboard`.

## Local smoke test script

This repo also includes a reusable PowerShell helper:

```powershell
.\tools\attribution-smoke.ps1
.\tools\attribution-smoke.ps1 -Scenario applink -Host your.domain.com
.\tools\attribution-smoke.ps1 -Scenario debug
```

Useful parameters:

- `-Scenario all|booking|activity|openwith|applink|debug`
- `-Host your.domain.com`
- `-PackageName com.example.simplewebview`
- `-Serial emulator-5554`
