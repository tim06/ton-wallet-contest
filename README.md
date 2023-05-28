# ton-wallet-contest
Android app for TON Wallet Challenge

# Build

Used studio version:
Android Studio Flamingo 2022.2.1 Patch 1

With studio:
1. Clone project
2. Open Android Studio
3. Open project
4. Click run after sync

With cli:
1. Clone project
2. cd ton-wallet-contest
3. ./gradlew app:assembleAfatRelease
4. apk path: ton-wallet-contest/app/afat/release/app-afat-release.apk


# APK
APK file download url: https://github.com/tim06/ton-wallet-contest/releases/

# Troubleshooting on mac os
  1. error: "Bad CPU type in executable".
  command: ```softwareupdate --install-rosetta```
  
  2. error: "Unknown host CPU architecture: arm64".
  fix: https://stackoverflow.com/a/69555276
  
