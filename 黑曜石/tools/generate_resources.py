"""Generate recipes and block-textured voxel item models from the game's item silhouettes.

Usage: python generate_resources.py /path/to/1.21.1.jar
Requires Pillow only for reading vanilla silhouettes. No edited/recolored texture is produced.
"""
import io
import json
import sys
import zipfile
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
MOD = "obsidian_equipment"
PATTERNS = {
    "sword": ["O", "O", "S"],
    "pickaxe": ["OOO", " S ", " S "],
    "axe": ["OO", "OS", " S"],
    "shovel": ["O", "S", "S"],
    "hoe": ["OO", " S", " S"],
    "helmet": ["OOO", "O O"],
    "chestplate": ["O O", "OOO", "OOO"],
    "leggings": ["OOO", "O O", "O O"],
    "boots": ["O O", "O O"],
}
ZH = dict(zip(PATTERNS, ["剑", "镐", "斧", "锹", "锄", "头盔", "胸甲", "护腿", "靴子"]))
TAGS = dict(zip(PATTERNS, ["swords", "pickaxes", "axes", "shovels", "hoes", "head_armor", "chest_armor", "leg_armor", "foot_armor"]))


def write(path, data):
    target = ROOT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def model_from_silhouette(jar, kind):
    pixels = Image.open(io.BytesIO(jar.read(f"assets/minecraft/textures/item/iron_{kind}.png"))).convert("RGBA")
    elements = []
    for y in range(16):
        x = 0
        while x < 16:
            def material(px):
                r, g, b, a = pixels.getpixel((px, y))
                if a == 0:
                    return None
                return "handle" if kind in list(PATTERNS)[:5] and r > g * 1.2 and g > b * 1.2 else "obsidian"
            texture = material(x)
            if texture is None:
                x += 1
                continue
            end = x + 1
            while end < 16 and material(end) == texture:
                end += 1
            elements.append({
                "from": [x, 15-y, 7.5], "to": [end, 16-y, 8.5],
                "faces": {face: {"texture": "#" + texture, "uv": [x, y, end, y+1]}
                          for face in ["north", "south", "east", "west", "up", "down"]},
            })
            x = end
    display = json.loads(jar.read("assets/minecraft/models/item/handheld.json"))["display"] if kind in list(PATTERNS)[:5] else {}
    display.update({
        "gui": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
        "ground": {"translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
        "fixed": {"rotation": [0, 180, 0], "scale": [1, 1, 1]},
    })
    return {"gui_light": "front", "textures": {
        "obsidian": "minecraft:block/obsidian", "handle": "minecraft:block/oak_planks",
        "particle": "minecraft:block/obsidian"}, "elements": elements, "display": display}


with zipfile.ZipFile(sys.argv[1]) as jar:
    for kind, pattern in PATTERNS.items():
        name = "obsidian_" + kind
        recipe_id = MOD + ":" + name
        key = {"O": {"item": "minecraft:obsidian"}}
        if any("S" in row for row in pattern):
            key["S"] = {"item": "minecraft:stick"}
        write(f"data/{MOD}/recipe/{name}.json", {
            "type": "minecraft:crafting_shaped", "category": "equipment", "pattern": pattern,
            "key": key, "result": {"id": recipe_id, "count": 1}})
        write(f"data/{MOD}/advancement/recipes/{name}.json", {
            "parent": "minecraft:recipes/root",
            "criteria": {
                "has_obsidian": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": "minecraft:obsidian"}]}},
                "has_the_recipe": {"trigger": "minecraft:recipe_unlocked", "conditions": {"recipe": recipe_id}},
            },
            "requirements": [["has_obsidian", "has_the_recipe"]], "rewards": {"recipes": [recipe_id]}})
        write(f"data/minecraft/tags/item/{TAGS[kind]}.json", {"replace": False, "values": [recipe_id]})
        write(f"assets/{MOD}/models/item/{name}.json", model_from_silhouette(jar, kind))

    # Copy the unmodified vanilla icon; all equipment models directly reference the block texture.
    icon = ROOT / f"assets/{MOD}/textures/mob_effect/obsidian_fire_resistance.png"
    icon.parent.mkdir(parents=True, exist_ok=True)
    icon.write_bytes(jar.read("assets/minecraft/textures/mob_effect/fire_resistance.png"))

write(f"data/{MOD}/advancement/full_obsidian_armor.json", {
    "parent": "minecraft:story/obtain_armor",
    "display": {
        "icon": {"id": MOD + ":obsidian_chestplate"},
        "title": {"text": "？？？"},
        "description": {"translate": "advancement.obsidian_equipment.full_set.description"},
        "frame": "challenge", "show_toast": True, "announce_to_chat": True, "hidden": True,
    },
    "criteria": {"wear_full_set": {"trigger": "minecraft:impossible"}},
    "requirements": [["wear_full_set"]],
})
for language in ["zh_cn", "en_us"]:
    values = {
        f"item.{MOD}.obsidian_{kind}": "黑曜石" + ZH[kind] if language == "zh_cn" else "Obsidian " + kind.title()
        for kind in PATTERNS
    }
    values[f"effect.{MOD}.obsidian_fire_resistance"] = "抗火（黑曜石套装）" if language == "zh_cn" else "Fire Resistance (Obsidian Set)"
    values[f"advancement.{MOD}.full_set.description"] = "穿戴全套黑曜石护甲" if language == "zh_cn" else "Equip a full set of obsidian armor"
    write(f"assets/{MOD}/lang/{language}.json", values)
print("Generated 9 recipes, unlock advancements, item models, item tags, languages, and full-set advancement.")
