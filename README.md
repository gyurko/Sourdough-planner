# Sourdough Planner (Android)

The sourdough bake planner as an Android app, with a notification at each step of the bake.

- Pick a start time or a ready-by time; get the full schedule and a gram-weight recipe.
- **Set step reminders** schedules an exact alarm for every remaining step. The alarms survive a reboot.
- **Share** sends the plan to any app (Messages, Todoist, Keep, email). **Copy schedule** puts it on the clipboard.
- Works fully offline (fonts are bundled). No internet permission and no libraries.
- Follows the system light/dark theme.

Requires Android 9 (API 28) or newer. Built against API 35.

## Build option A: GitHub Actions (no Android Studio needed)

1. Create a new GitHub repository and push this folder to it.
2. The **Build APK** workflow runs on every push (or start it by hand from the Actions tab).
3. Open the finished run and download the `sourdough-planner-apk` artifact. Unzip it to get `app-release.apk`.

## Build option B: Android Studio

1. File > Open, choose this folder, and let Gradle sync.
2. Run on a connected phone, or Build > Build App Bundle(s) / APK(s) > Build APK(s).

Command line equivalent (needs the Android SDK and `ANDROID_HOME` set):

```
./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```

## Installing the APK

Copy the APK to the phone and open it. Android asks you to allow installs from that source (Files, Chrome, Drive) once.
The release build is signed with the debug key so it installs directly; set up your own signing config before distributing it.

On first use of **Set step reminders**, Android 13+ asks for notification permission. Allow it, or the reminders are scheduled but nothing appears.

## Project layout

```
web/planner.html                 The planner page (same as the web version)
tools/build_page.py              Regenerates app/src/main/assets/index.html from web/planner.html
app/src/main/assets/             index.html + bundled fonts (OFL)
app/src/main/java/.../
  MainActivity.java              Full-screen WebView, edge-to-edge insets
  Bridge.java                    window.AndroidBridge: reminders, share, copy, settings storage
  Reminders.java                 AlarmManager scheduling, cancel, reboot restore
  ReminderReceiver.java          Posts the step notification
  BootReceiver.java              Re-arms reminders after reboot
```

To change the planner itself, edit `web/planner.html`, run `python3 tools/build_page.py`, and rebuild.
The page detects `window.AndroidBridge`; in a normal browser the reminder and share buttons stay hidden.

## Notes

- Reminders fire at the scheduled time for each step. If you change the plan, tap **Update reminders** to replace them.
- Steps already in the past are skipped.
- Battery savers on some phones (Samsung, Xiaomi) can still delay alarms. If reminders arrive late,
  set the app's battery usage to Unrestricted.
