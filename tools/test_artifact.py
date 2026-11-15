from pathlib import Path
import tempfile
import unittest
from tools.verify_artifact import verify


class ArtifactTest(unittest.TestCase):
    def test_built_jar_is_complete(self):
        verify(Path(__file__).resolve().parents[1])

    def test_missing_jar_fails(self):
        with tempfile.TemporaryDirectory() as folder:
            with self.assertRaises(RuntimeError):
                verify(Path(folder))

    def test_extra_jar_fails(self):
        with tempfile.TemporaryDirectory() as folder:
            target = Path(folder) / "shade/target"
            target.mkdir(parents=True)
            (target / "SopLib.jar").touch()
            (target / "original-SopLib.jar").touch()
            with self.assertRaises(RuntimeError):
                verify(Path(folder))


if __name__ == "__main__":
    unittest.main()
