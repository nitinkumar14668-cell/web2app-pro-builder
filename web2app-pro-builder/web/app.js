const logEl = document.getElementById("log");
const downloadsEl = document.getElementById("downloads");

function log(msg) {
  logEl.textContent += msg + "\n";
}

const urlModeEl = document.getElementById("urlMode");
const zipBox = document.getElementById("zipBox");
const urlBox = document.getElementById("urlBox");

urlModeEl.addEventListener("change", () => {
  const mode = urlModeEl.value;
  zipBox.style.display = mode === "zip" ? "block" : "none";
  urlBox.style.display = mode === "url" ? "block" : "none";
});

document.getElementById("btnBuild").addEventListener("click", async () => {
  logEl.textContent = "";
  downloadsEl.innerHTML = "";

  const apiBase = document.getElementById("apiBase").value.trim();
  const urlMode = document.getElementById("urlMode").value;
  const buildType = document.getElementById("buildType").value;

  const data = new FormData();
  data.append("appName", document.getElementById("appName").value.trim());
  data.append("packageName", document.getElementById("packageName").value.trim());
  data.append("versionName", document.getElementById("versionName").value.trim());
  data.append("versionCode", document.getElementById("versionCode").value.trim());
  data.append("buildType", buildType);
  data.append("urlMode", urlMode);

  data.append("enableAdmob", document.getElementById("enableAdmob").checked ? "true" : "false");
  data.append("enableHoneygain", document.getElementById("enableHoneygain").checked ? "true" : "false");

  if (urlMode === "zip") {
    const f = document.getElementById("zipFile").files[0];
    if (!f) return log("❌ Please select website.zip");
    data.append("websiteZip", f);
  } else {
    const url = document.getElementById("websiteUrl").value.trim();
    if (!url) return log("❌ Please enter Website URL");
    data.append("websiteUrl", url);
  }

  try {
    log("⏳ Uploading + Building... (wait)");
    const res = await fetch(`${apiBase}/build`, { method: "POST", body: data });
    const json = await res.json();

    if (!json.ok) {
      log("❌ Build failed:");
      log(json.error || "Unknown error");
      return;
    }

    log("✅ Build done!");
    log(`Job ID: ${json.jobId}`);

    downloadsEl.innerHTML = `<h3>Downloads</h3>`;
    json.outputs.forEach(o => {
      const a = document.createElement("a");
      a.href = o.url;
      a.textContent = `Download ${o.type.toUpperCase()}`;
      a.target = "_blank";
      downloadsEl.appendChild(a);
      downloadsEl.appendChild(document.createElement("br"));
    });
  } catch (e) {
    log("❌ Error:");
    log(String(e));
  }
}); 
