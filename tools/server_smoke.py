"""Run an explicitly supplied Paper JAR in a fresh, isolated directory."""
import argparse
import hashlib
from pathlib import Path
import queue
import shutil
import subprocess
import tempfile
import threading
import time
from zipfile import ZipFile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--server-jar", type=Path, required=True)
    parser.add_argument("--dependency-jar", type=Path, action="append", default=[])
    parser.add_argument("--bootstrap-jar", type=Path, help="Pre-downloaded Mojang server JAR; hash checked against Paper")
    parser.add_argument("--java", default="java")
    parser.add_argument("--accept-eula", action="store_true")
    parser.add_argument("--timeout", type=int, default=240)
    args = parser.parse_args()
    if not args.accept_eula:
        parser.error("Read https://aka.ms/MinecraftEULA and explicitly pass --accept-eula")
    root = Path(__file__).resolve().parents[1]
    plugin = root / "shade/target/SopLib.jar"
    for file in [args.server_jar, plugin, *args.dependency_jar, *([args.bootstrap_jar] if args.bootstrap_jar else [])]:
        if not file.is_file():
            parser.error(f"Missing JAR: {file}")
    if args.bootstrap_jar:
        with ZipFile(args.server_jar) as paper:
            expected, _, cache_name = paper.read("META-INF/download-context").decode().strip().split("\t")
        if hashlib.sha256(args.bootstrap_jar.read_bytes()).hexdigest() != expected:
            parser.error("Mojang bootstrap JAR checksum does not match Paper")
    work = Path(tempfile.mkdtemp(prefix="soplib-smoke-"))
    (work / "plugins").mkdir()
    shutil.copy2(args.server_jar, work / "paper.jar")
    if args.bootstrap_jar:
        (work / "cache").mkdir()
        shutil.copy2(args.bootstrap_jar, work / "cache" / Path(cache_name).name)
    for file in [plugin, *args.dependency_jar]:
        shutil.copy2(file, work / "plugins" / file.name)
    (work / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    (work / "server.properties").write_text(
        "server-ip=127.0.0.1\nserver-port=0\nenable-rcon=false\nonline-mode=true\n"
        "level-type=minecraft:flat\nview-distance=2\nsimulation-distance=2\n", encoding="utf-8")
    lines = queue.Queue()
    process = subprocess.Popen([args.java, "-Xms512M", "-Xmx2G", "-jar", "paper.jar", "--nogui"],
                               cwd=work, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                               stderr=subprocess.STDOUT, text=True, encoding="utf-8", errors="replace")
    def read():
        for line in process.stdout:
            lines.put(line)
    threading.Thread(target=read, daemon=True).start()
    enabled = doctor = False
    sent = False
    failures = []
    deadline = time.monotonic() + args.timeout
    try:
        with (work / "smoke.log").open("w", encoding="utf-8") as log:
            while time.monotonic() < deadline:
                try:
                    line = lines.get(timeout=0.2)
                except queue.Empty:
                    if process.poll() is not None:
                        break
                    continue
                log.write(line)
                log.flush()
                enabled |= "SopLib.getInstance() after set ->" in line
                if "ERROR" in line or "[FAIL]" in line or "Exception" in line:
                    failures.append(line.strip())
                if "Done (" in line and not sent:
                    process.stdin.write("soplib doctor\n")
                    process.stdin.flush()
                    sent = True
                if "[SopLib doctor] Complete" in line:
                    doctor = True
                    process.stdin.write("stop\n")
                    process.stdin.flush()
            if process.poll() is None:
                process.terminate()
            process.wait(timeout=20)
    finally:
        if process.poll() is None:
            process.kill()
            process.wait()
        print("Isolated server logs:", work)
    if not enabled or not doctor or failures or process.returncode != 0:
        raise SystemExit(f"FAILED: enabled={enabled}, doctor={doctor}, exit={process.returncode}, errors={failures[:5]}")
    print("Startup and doctor passed; player-dependent features remain untested.")


if __name__ == "__main__":
    main()
