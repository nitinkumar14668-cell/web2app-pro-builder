import argparse
import os
import shutil
import zipfile
import json

def write_file(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

def replace_in_file(path, old, new):
    with open(path, "r", encoding="utf-8") as f:
        data = f.read()
    data = data.replace(old, new)
    with open(path, "w", encoding="utf-8") as f:
        f.write(data)

def unzip_website(zip_path, dest_www):
    if os.path.exists(dest_www):
        shutil.rmtree(dest_www)
    os.makedirs(dest_www, exist_ok=True)

    with zipfile.ZipFile(zip_path, "r") as z:
        z.extractall(dest_www)

    # if zip contains nested folder, try to fix
    index1 = os.path.join(dest_www, "index.html")
    if not os.path.exists(index1):
        # search index.html
        found = None
        for root, dirs, files in os.walk(dest_www):
            if "index.html" in files:
                found = root
                break
        if found:
            # move found folder content to dest_www root
            tmp = dest_www + "_tmp"
            os.makedirs(tmp, exist_ok=True)
            for name in os.listdir(found):
                shutil.move(os.path.join(found, name), os.path.join(tmp, name))
            shutil.rmtree(dest_www)
            shutil.move(tmp, dest_www)

    if not os.path.exists(os.path.join(dest_www, "index.html")):
        raise Exception("index.html not found inside website.zip")

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--android", required=True)
    ap.add_argument("--zip")
    ap.add_argument("--appName", required=True)
    ap.add_argument("--packageName", required=True)
    ap.add_argument("--versionName", required=True)
    ap.add_argument("--versionCode", required=True)
    ap.add_argument("--enableAdmob", required=True)
    ap.add_argument("--enableHoneygain", required=True)
    ap.add_argument("--urlMode", required=True)  # zip|url
    ap.add_argument("--websiteUrl", default="")
    args = ap.parse_args()

    android = args.android
    app_gradle = os.path.join(android, "app", "build.gradle.kts")
    manifest = os.path.join(android, "app", "src", "main", "AndroidManifest.xml")
    strings = os.path.join(android, "app", "src", "main", "res", "values", "strings.xml")

    # Update app name
    replace_in_file(strings, "Web2App Pro", args.appName)

    # Update version
    replace_in_file(app_gradle, "versionCode = 1", f"versionCode = {args.versionCode}")
    replace_in_file(app_gradle, 'versionName = "1.0"', f'versionName = "{args.versionName}"')

    # URL mode config
    config_path = os.path.join(android, "app", "src", "main", "assets", "app_config.json")
    cfg = {
        "urlMode": args.urlMode,
        "websiteUrl": args.websiteUrl,
        "plugins": {
            "admob": args.enableAdmob == "true",
            "honeygain": args.enableHoneygain == "true"
        }
    }
    write_file(config_path, json.dumps(cfg, indent=2))

    # ZIP mode: unzip website
    if args.urlMode == "zip":
        if not args.zip:
            raise Exception("zip required for zip mode")
        dest_www = os.path.join(android, "app", "src", "main", "assets", "www")
        unzip_website(args.zip, dest_www)

    # AdMob enable/disable in Manifest
    if args.enableAdmob == "true":
        # ensure meta-data exists (already in template)
        pass
    else:
        # disable by setting dummy id
        replace_in_file(manifest, "ca-app-pub-3940256099942544~3347511713", "DISABLED")

    print("✅ Patch complete")

if __name__ == "__main__":
    main()
