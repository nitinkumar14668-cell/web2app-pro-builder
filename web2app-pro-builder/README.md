# Web2App Pro Builder

## What it does
- Upload website.zip OR enter Website URL
- Select plugins: AdMob + Honeygain(dummy)
- Build APK/AAB locally using Gradle
- Download output from API

## Run (Local)
### 1) Install Node deps
cd api
npm install
node index.js

API runs at: http://localhost:5050

### 2) Open builder UI
Open `web/index.html` in browser
Set API Base URL = http://localhost:5050
Upload ZIP or URL -> Build

## Deploy Web UI to Vercel
Deploy `web/` as static site.
In UI set API Base URL to your VPS/PC public URL.

## Notes
- Honeygain plugin is DUMMY placeholder. Real SDK can be added later.
- AdMob uses TEST IDs by default.
