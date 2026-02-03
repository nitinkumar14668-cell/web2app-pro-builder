import argparse
import os
import shutil
import zipfile
import json

# ---------- helpers ----------
def read_file(path):
    with open(path, "r", encoding="utf-8") as f:
        return f.read()

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

def replace_in_file(path, old, new):
    if not os.path.exists(path):
        return False
    data = read_file(path)
    if old not in data:
        return False
    data = data.replace(old, new)
    write_file(path, data)
    return True

def unzip_website(zip_path, dest_www):
    if os.path.exists(dest_www):
        shutil.rmtree(dest_www)
    os.makedirs(dest_www, exist_ok=True)

    with zipfile.ZipFile(zip_path, "r") as z:
        z.extractall(dest_www)

    # fix nested folder issue
    index1 = os.path.join(dest_www, "index.html")
    if not os.path.exists(index1):
        found = None
        for root, dirs, files in os.walk(dest_www):
            if "index.html" in files:
                found = root
                break

        if found:
            tmp = dest_www + "_tmp"
            os.makedirs(tmp, exist_ok=True)
            for name in os.listdir(found):
                shutil.move(os.path.join(found, name), os.path.join(tmp, name))
            shutil.rmtree(dest_www)
            shutil.move(tmp, dest_www)

    if not os.path.exists(os.path.join(dest_www, "index.html")):
        raise Exception("index.html not found inside website.zip")

# ---------- main ----------
def main():
    ap = argparse.ArgumentParser()

    ap.add_argument("--android", required=True)
    ap.add_argument("--zip")
    ap.add_argument("--appName", required=True)
    ap.add_argument("--packageName", required=True)
    ap.add_argument("--versionName", required=True)
    ap.add_argument("--versionCode", required=True)

    ap.add_argument("--enableAdmob", required=True)  # true/false
    ap.add_argument("--urlMode", required=True)      # zip|url
    ap.add_argument("--websiteUrl", default="")

    # AdMob IDs
    ap.add_argument("--admobAppId", default="pub-3546008790006961~3626414349")
    ap.add_argument("--admobBannerId", default="ca-app-pub-3546008790006961/4883947277")
    ap.add_argument("--admobInterstitialId", default="ca-app-pub-3546008790006961/2919837333")
    ap.add_argument("--admobRewardedId", default="ca-app-pub-3546008790006961/5224354917")

    args = ap.parse_args()

    android = args.android

    app_gradle = os.path.join(android, "app", "build.gradle.kts")
    manifest = os.path.join(android, "app", "src", "main", "AndroidManifest.xml")
    strings = os.path.join(android, "app", "src", "main", "res", "values", "strings.xml")
    main_activity = os.path.join(android, "app", "src", "main", "java", "com", "web2apppro", "MainActivity.kt")
    activity_main = os.path.join(android, "app", "src", "main", "res", "layout", "activity_main.xml")

    # 1) Update app name
    replace_in_file(strings, "Web2App Pro", args.appName)

    # 2) Update version
    replace_in_file(app_gradle, "versionCode = 1", f"versionCode = {args.versionCode}")
    replace_in_file(app_gradle, 'versionName = "1.0"', f'versionName = "{args.versionName}"')

    # 3) Save config
    config_path = os.path.join(android, "app", "src", "main", "assets", "app_config.json")
    cfg = {
        "urlMode": args.urlMode,
        "websiteUrl": args.websiteUrl,
        "plugins": {
            "admob": args.enableAdmob == "true"
        }
    }
    write_file(config_path, json.dumps(cfg, indent=2))

    # 4) ZIP mode -> unzip website
    if args.urlMode == "zip":
        if not args.zip:
            raise Exception("zip required for zip mode")
        dest_www = os.path.join(android, "app", "src", "main", "assets", "www")
        unzip_website(args.zip, dest_www)

    # 5) AdMob patching
    if args.enableAdmob == "true":
        # Manifest Application ID
        replace_in_file(manifest, "pub-3546008790006961~3626414349", args.admobAppId)

        # MainActivity: Interstitial & Rewarded
        replace_in_file(main_activity, "ca-app-pub-3546008790006961/2919837333", args.admobInterstitialId)
        replace_in_file(main_activity, "ca-app-pub-3546008790006961/5224354917", args.admobRewardedId)

        # activity_main.xml: Banner ID
        replace_in_file(activity_main, "ca-app-pub-3546008790006961/4883947277", args.admobBannerId)

    else:
        # Disable AdMob (set dummy IDs)
        replace_in_file(manifest, "pub-3546008790006961~3626414349", "DISABLED")
        replace_in_file(main_activity, "ca-app-pub-3546008790006961/2919837333", "DISABLED")
        replace_in_file(main_activity, "ca-app-pub-3546008790006961/5224354917", "DISABLED")
        replace_in_file(activity_main, "ca-app-pub-3546008790006961/4883947277", "DISABLED")

    print("✅ Patch complete (AdMob updated)")

if __name__ == "__main__":
    main()
