"""Validate complete coverage and references against the exact official AE2 dependency JAR."""
import json
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
COLORS = ["fluix", "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]
KINDS = ["glass_cable", "covered_cable", "smart_cable", "covered_dense_cable", "smart_dense_cable"]

with zipfile.ZipFile(sys.argv[1]) as ae2:
    for kind in KINDS:
        for color in COLORS:
            base = f"{color}_{kind}"
            assert f"assets/ae2/models/item/{base}.json" in ae2.namelist()
            for tier in [1, 3]:
                name = f"{base}_tier_{tier}"
                model = json.loads((RES / f"assets/ae2_cable_tiers/models/item/{name}.json").read_text())
                assert model == {"parent": f"ae2:item/{base}"}
            recipes = RES / "data/ae2_cable_tiers/recipe"
            low = f"ae2_cable_tiers:{base}_tier_1"
            normal = f"ae2:{base}"
            high = f"ae2_cable_tiers:{base}_tier_3"
            for category, ingredient, count, output, quantity in [
                ("split", normal, 1, low, 4), ("combine", low, 4, normal, 1), ("upgrade", normal, 4, high, 1)
            ]:
                r = json.loads((recipes / category / f"{base}.json").read_text())
                assert r["ingredients"] == [{"item": ingredient}] * count
                assert r["result"] == {"id": output, "count": quantity}
            for tier in [1, 3]:
                if color != "fluix":
                    r = json.loads((recipes / "dye" / f"{base}_tier_{tier}.json").read_text())
                    assert len(r["ingredients"]) == 9 and r["result"]["count"] == 8
                    assert r["result"]["id"] == f"ae2_cable_tiers:{base}_tier_{tier}"
    for p in RES.rglob("*.json"):
        json.loads(p.read_text())
    assert len(list((RES / "assets/ae2_cable_tiers/models/item").glob("*.json"))) == 170
    assert len(list((RES / "data/ae2_cable_tiers/recipe").rglob("*.json"))) == 426
    assert not (RES / "data/ae2/recipe").exists(), "Do not overwrite AE2 recipes"
print("Passed: 255 combinations; 170 model references; 426 recipes; exact tier conversion costs; valid JSON; no overwritten AE2 recipes.")
