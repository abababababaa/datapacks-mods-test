"""Validate complete coverage and references against the exact official AE2 dependency JAR."""
import json
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
COLORS = ["fluix", "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]
KINDS = ["glass_cable", "covered_cable", "smart_cable", "covered_dense_cable", "smart_dense_cable"]

def shapeless_key(recipe):
    if recipe.get("type") != "minecraft:crafting_shapeless":
        return None
    return tuple(sorted(json.dumps(value, sort_keys=True) for value in recipe["ingredients"]))

with zipfile.ZipFile(sys.argv[1]) as ae2:
    official_inputs = {}
    for name in ae2.namelist():
        if name.startswith("data/ae2/recipe/") and name.endswith(".json"):
            recipe = json.loads(ae2.read(name))
            key = shapeless_key(recipe)
            if key is not None:
                official_inputs[key] = name
    own_inputs = {}
    for path in (RES / "data/ae2_cable_tiers/recipe").rglob("*.json"):
        key = shapeless_key(json.loads(path.read_text()))
        if key is None:
            continue
        assert key not in official_inputs, f"Conflicts with AE2: {path} / {official_inputs.get(key)}"
        assert key not in own_inputs, f"Ambiguous addon recipes: {path} / {own_inputs.get(key)}"
        own_inputs[key] = path

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
                ("split", normal, 1, low, 4), ("combine", low, 4, normal, 1), ("upgrade", normal, 8, high, 2)
            ]:
                r = json.loads((recipes / category / f"{base}.json").read_text())
                if category == "combine":
                    assert r["type"] == "minecraft:crafting_shaped"
                    assert r["pattern"] == ["C C", "   ", "C C"]
                    assert r["key"] == {"C": {"item": ingredient}}
                else:
                    assert r["ingredients"] == [{"item": ingredient}] * count
                assert r["result"] == {"id": output, "count": quantity}
            for tier in [1, 3]:
                if color != "fluix":
                    r = json.loads((recipes / "dye" / f"{base}_tier_{tier}.json").read_text())
                    assert len(r["ingredients"]) == 9 and r["result"]["count"] == 8
                    assert r["result"]["id"] == f"ae2_cable_tiers:{base}_tier_{tier}"
    for p in RES.rglob("*.json"):
        json.loads(p.read_text())
    assert len(list((RES / "assets/ae2_cable_tiers/models/item").glob("*.json"))) == 193
    assert len(list((RES / "data/ae2_cable_tiers/recipe").rglob("*.json"))) == 482
    assert not (RES / "data/ae2/recipe").exists(), "Do not overwrite AE2 recipes"
print("Passed: 255 combinations; 193 model references; 482 recipes; exact tier conversion costs; valid JSON; no overwritten AE2 recipes.")
