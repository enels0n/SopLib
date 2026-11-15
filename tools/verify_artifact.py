"""Check the distributable, without claiming server compatibility."""
from pathlib import Path
from zipfile import ZipFile


def verify(root):
    jars = sorted((root / "shade/target").glob("*.jar"))
    if [p.name for p in jars] != ["SopLib.jar"]:
        raise RuntimeError(f"Expected exactly SopLib.jar, found: {jars}")
    with ZipFile(jars[0]) as archive:
        names = set(archive.namelist())
        for service in ("item.ItemUtils", "util.Util"):
            path = "META-INF/services/net.enelson.sopli.lib." + service
            providers = [line.split("#", 1)[0].strip()
                         for line in archive.read(path).decode("utf-8").splitlines()]
            for provider in filter(None, providers):
                if provider.replace(".", "/") + ".class" not in names:
                    raise RuntimeError("Missing service provider: " + provider)
        for version in ("26_1", "26_2", "26_3"):
            for path in (f"item/impl/ItemUtils_{version}", f"util/impl/Util_{version}",
                         f"corpse/CorpseService_{version}",
                         f"customblocks/CustomBlockVisualService_{version}",
                         f"protection/ProtectionService_{version}", f"region/RegionService_{version}"):
                if "net/enelson/sopli/lib/" + path + ".class" not in names:
                    raise RuntimeError("Missing adapter: " + path)
        if "net/enelson/sopli/lib/DoctorCommand.class" not in names:
            raise RuntimeError("Doctor command is missing")
    print("Artifact verified: one JAR, complete providers and 26.x adapters")


if __name__ == "__main__":
    verify(Path(__file__).resolve().parents[1])
