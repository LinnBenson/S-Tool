#!/usr/bin/env python3
"""Fail if an APK retains AndroidSVG logging calls. Usage: script APK /path/to/dexdump."""
import pathlib
import re
import subprocess
import sys
import tempfile
import zipfile

apk, dexdump = sys.argv[1:]
classes = 0
violations = []
with tempfile.TemporaryDirectory() as directory, zipfile.ZipFile(apk) as archive:
    for entry in archive.namelist():
        if not re.fullmatch(r"classes\d*\.dex", entry):
            continue
        dex = pathlib.Path(directory) / entry
        dex.write_bytes(archive.read(entry))
        process = subprocess.Popen([dexdump, "-d", str(dex)], stdout=subprocess.PIPE, text=True, errors="replace")
        in_svg = False
        current_class = ""
        for line in process.stdout:
            if line.startswith("Class #"):
                in_svg = False
            if "Class descriptor" in line:
                current_class = line.strip()
                in_svg = "Lcom/caverock/androidsvg/" in line
                classes += int(in_svg)
            if in_svg and "invoke-" in line and (
                "Landroid/util/Log;" in line or "printStackTrace:" in line or "Ljava/io/PrintStream;" in line
            ):
                violations.append(current_class)
        if process.wait():
            raise SystemExit("dexdump failed")
if not classes:
    raise SystemExit("No AndroidSVG classes found; review class naming and the verification scope")
if violations:
    raise SystemExit("AndroidSVG logging calls remain in: " + ", ".join(sorted(set(violations))))
print(f"PASS: {classes} AndroidSVG classes, no logging calls in {apk}")
