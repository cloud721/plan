# Antigravity Figma Swagger Demo

This repository archives a local AI-assisted demo described in a Changdu Confluence page. It contains:

- `client-android/`: Android client project with login, bottom navigation pages, and attribution demo modules.
- `server/`: local Node.js mock API service.
- `swagger/`: standalone OpenAPI definition for the mock login API.
- `docs/`: exported summary of the original Changdu Confluence document.

## Quick Start

### Start Mock Server

```powershell
cd server
node server.js
```

The server listens on port `3000`:

- Swagger UI: `http://localhost:3000/api-docs`
- Login API: `POST http://localhost:3000/login`

### Build Android Client

```powershell
cd client-android
.\gradlew.bat assembleDebug
```

The Android login page currently posts to `http://192.168.21.35:3000/login`, matching the original local debugging environment. Change the host in `LoginActivity.kt` when running on a different LAN or emulator setup.

## Repository Notes

Build outputs, IDE metadata, and local machine files are intentionally excluded. The original Confluence media attachments are listed in `docs/changdu-antigravity-figma-swagger.md`; the page link is preserved for authenticated access.

