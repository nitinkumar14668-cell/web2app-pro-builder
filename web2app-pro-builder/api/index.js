const express = require("express");
const cors = require("cors");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const { nanoid } = require("nanoid");
const { spawn } = require("child_process");

const app = express();
app.use(cors());
app.use(express.json());

const ROOT = path.join(__dirname, "..");
const STORAGE = path.join(ROOT, "storage");
const UPLOADS = path.join(STORAGE, "uploads");
const BUILDS = path.join(STORAGE, "builds");

for (const p of [STORAGE, UPLOADS, BUILDS]) fs.mkdirSync(p, { recursive: true });

const upload = multer({ dest: UPLOADS });

const PORT = 5050;
const PUBLIC_BASE_URL = `http://localhost:${PORT}`;
const ANDROID_PROJECT_PATH = path.join(ROOT, "android");

app.use("/downloads", express.static(BUILDS));

function run(cmd, args, cwd) {
  return new Promise((resolve, reject) => {
    const p = spawn(cmd, args, { cwd, shell: true });
    let out = "";
    let err = "";
    p.stdout.on("data", d => (out += d.toString()));
    p.stderr.on("data", d => (err += d.toString()));
    p.on("close", code => {
      if (code === 0) resolve({ out, err });
      else reject(new Error(`❌ ${cmd} ${args.join(" ")}\n\n${err}\n${out}`));
    });
  });
}

app.get("/", (req, res) => {
  res.json({ ok: true, name: "Web2App Pro API", androidPath: ANDROID_PROJECT_PATH });
});

app.post("/build", upload.single("websiteZip"), async (req, res) => {
  try {
    const jobId = nanoid(10);

    const {
      appName = "Web2App Pro",
      packageName = "com.web2apppro",
      versionName = "1.0",
      versionCode = "1",
      buildType = "apk", // apk | aab | both
      urlMode = "zip",   // zip | url
      websiteUrl = "",
      enableAdmob = "true",
      enableHoneygain = "false"
    } = req.body;

    const jobDir = path.join(BUILDS, jobId);
    fs.mkdirSync(jobDir, { recursive: true });

    const zipPath = req.file ? req.file.path : null;

    if (urlMode === "zip" && !zipPath) {
      return res.status(400).json({ ok: false, error: "websiteZip required for ZIP mode" });
    }

    // Save job info
    fs.writeFileSync(
      path.join(jobDir, "job.json"),
      JSON.stringify(
        {
          jobId,
          appName,
          packageName,
          versionName,
          versionCode,
          buildType,
          urlMode,
          websiteUrl,
          plugins: { admob: enableAdmob === "true", honeygain: enableHoneygain === "true" }
        },
        null,
        2
      )
    );

    // Patch
    const patchScript = path.join(ROOT, "scripts", "patch.py");
    const args = [
      patchScript,
      "--android", ANDROID_PROJECT_PATH,
      "--appName", appName,
      "--packageName", packageName,
      "--versionName", versionName,
      "--versionCode", versionCode,
      "--enableAdmob", enableAdmob,
      "--enableHoneygain", enableHoneygain,
      "--urlMode", urlMode,
      "--websiteUrl", websiteUrl
    ];
    if (zipPath) args.push("--zip", zipPath);

    await run("python", args, ROOT);

    // Build
    const gradlew = process.platform === "win32" ? "gradlew.bat" : "./gradlew";

    await run(gradlew, ["clean"], ANDROID_PROJECT_PATH);

    let outputs = [];

    if (buildType === "apk" || buildType === "both") {
      await run(gradlew, ["assembleRelease"], ANDROID_PROJECT_PATH);

      const apk = path.join(
        ANDROID_PROJECT_PATH,
        "app/build/outputs/apk/release/app-release.apk"
      );

      const dest = path.join(jobDir, `${jobId}.apk`);
      fs.copyFileSync(apk, dest);

      outputs.push({ type: "apk", url: `${PUBLIC_BASE_URL}/downloads/${jobId}/${jobId}.apk` });
    }

    if (buildType === "aab" || buildType === "both") {
      await run(gradlew, ["bundleRelease"], ANDROID_PROJECT_PATH);

      const aab = path.join(
        ANDROID_PROJECT_PATH,
        "app/build/outputs/bundle/release/app-release.aab"
      );

      const dest = path.join(jobDir, `${jobId}.aab`);
      fs.copyFileSync(aab, dest);

      outputs.push({ type: "aab", url: `${PUBLIC_BASE_URL}/downloads/${jobId}/${jobId}.aab` });
    }

    res.json({ ok: true, jobId, outputs });
  } catch (e) {
    console.error(e);
    res.status(500).json({ ok: false, error: String(e.message || e) });
  }
});

app.listen(PORT, () => {
  console.log(`✅ API running: http://localhost:${PORT}`);
});
