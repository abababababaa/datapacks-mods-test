"""Deterministic tier recipes and parent-model references. No AE2 assets are copied."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
MOD = "ae2_cable_tiers"
COLORS = ["fluix", "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]
KINDS = ["glass_cable", "covered_cable", "smart_cable", "covered_dense_cable", "smart_dense_cable"]
ZH_COLORS = ["无色", "白色", "橙色", "品红色", "淡蓝色", "黄色", "黄绿色", "粉红色", "灰色", "淡灰色", "青色", "紫色", "蓝色", "棕色", "绿色", "红色", "黑色"]
ZH_KINDS = ["水晶线缆", "包层线缆", "智能线缆", "致密包层线缆", "致密智能线缆"]
EN_KINDS = ["Glass Cable", "Covered Cable", "Smart Cable", "Dense Covered Cable", "Dense Smart Cable"]


def write(path, value):
    p = ROOT / path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def item(kind, color, tier):
    return f"ae2:{color}_{kind}" if tier == 2 else f"{MOD}:{color}_{kind}_tier_{tier}"


def recipe(name, inputs, output, count=1):
    write(f"data/{MOD}/recipe/{name}.json", {"type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": inputs, "result": {"id": output, "count": count}})


def shaped(name, pattern, ingredient, output, count=1):
    write(f"data/{MOD}/recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "misc",
        "pattern": pattern, "key": {"C": {"item": ingredient}}, "result": {"id": output, "count": count}})


zh = {"itemGroup.ae2_cable_tiers": "AE2 线缆拓展", "tooltip.ae2_cable_tiers.tier": "等级 %s · 默认模式容量：%s 频道",
      "tooltip.ae2_cable_tiers.mode": "实际容量随 AE2 网络频道模式变化"}
en = {"itemGroup.ae2_cable_tiers": "AE2 Cable Tiers", "tooltip.ae2_cable_tiers.tier": "Tier %s · Default mode capacity: %s channels",
      "tooltip.ae2_cable_tiers.mode": "Actual capacity follows the AE2 network channel mode"}
for kind_index, kind in enumerate(KINDS):
    for color_index, color in enumerate(COLORS):
        normal = item(kind, color, 2)
        low = item(kind, color, 1)
        high = item(kind, color, 3)
        recipe(f"split/{color}_{kind}", [{"item": normal}], low, 4)
        shaped(f"combine/{color}_{kind}", ["C C", "   ", "C C"], low, normal)
        recipe(f"upgrade/{color}_{kind}", [{"item": normal}] * 8, high, 2)
        for tier in [1, 3]:
            name = f"{color}_{kind}_tier_{tier}"
            write(f"assets/{MOD}/models/item/{name}.json", {"parent": f"ae2:item/{color}_{kind}"})
            zh[f"item.{MOD}.{name}"] = f"{ZH_COLORS[color_index]} ME {ZH_KINDS[kind_index]} · {'一级' if tier == 1 else '三级'}"
            en[f"item.{MOD}.{name}"] = f"{color.replace('_', ' ').title()} ME {EN_KINDS[kind_index]} · Tier {tier}"
            if color != "fluix":
                recipe(f"dye/{name}", [{"tag": f"{MOD}:{kind}_tier_{tier}"}] * 8 + [{"tag": f"c:dyes/{color}"}], item(kind, color, tier), 8)
    for tier in [1, 3]:
        all_items = [item(kind, color, tier) for color in COLORS]
        write(f"data/{MOD}/tags/item/{kind}_tier_{tier}.json", {"replace": False, "values": all_items})
        write(f"data/{MOD}/tags/item/colored_{kind}_tier_{tier}.json", {"replace": False, "values": all_items[1:]})
        # Keep AE2's own color-removal ingredient policy (water bucket, etc.) and vanilla remainders.
        recipe(f"clean/{kind}_tier_{tier}", [{"tag": f"{MOD}:colored_{kind}_tier_{tier}"}, {"tag": "ae2:can_remove_color"}], item(kind, "fluix", tier))

# Each fragment is exactly one eighth of its named source item.
materials = [
    ("fluix_crystal_fragment", "ae2:fluix_crystal", "福鲁伊克斯水晶碎块", "Fluix Crystal Fragment"),
    ("certus_quartz_fragment", "ae2:certus_quartz_crystal", "赛特斯石英水晶碎块", "Certus Quartz Fragment"),
    ("nether_quartz_fragment", "minecraft:quartz", "下界石英碎块", "Nether Quartz Fragment"),
    ("charged_certus_quartz_fragment", "ae2:charged_certus_quartz_crystal", "充能赛特斯石英水晶碎块", "Charged Certus Quartz Fragment"),
    ("redstone_fragment", "minecraft:redstone", "红石粉碎料", "Redstone Dust Fragment"),
    ("glowstone_fragment", "minecraft:glowstone_dust", "荧石粉碎料", "Glowstone Dust Fragment"),
]
for i, color in enumerate(COLORS[1:], 1):
    materials.append((f"{color}_wool_fragment", f"minecraft:{color}_wool", f"{ZH_COLORS[i]}羊毛碎料", f"{color.replace('_', ' ').title()} Wool Scrap"))
for name, source, zh_name, en_name in materials:
    recipe(f"materials/split_{name}", [{"item": source}], f"{MOD}:{name}", 8)
    recipe(f"materials/combine_{name}", [{"item": f"{MOD}:{name}"}] * 8, source)
for name, zh_name, en_name in [("micro_quartz_fiber", "微石英纤维", "Micro Quartz Fiber")] + [(m[0], m[2], m[3]) for m in materials]:
    write(f"assets/{MOD}/models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/material_placeholder"}})
    zh[f"item.{MOD}.{name}"] = zh_name
    en[f"item.{MOD}.{name}"] = en_name
write(f"data/{MOD}/tags/item/wool_fragments.json", {"replace": False, "values": [f"{MOD}:{c}_wool_fragment" for c in COLORS[1:]]})
write(f"data/{MOD}/recipe/materials/micro_quartz_fiber.json", {
    "type": "minecraft:crafting_shaped", "category": "misc", "pattern": ["G", "Q", "G"],
    "key": {"G": {"tag": "c:glass_blocks/cheap"}, "Q": {"tag": "ae2:all_quartz_dust"}},
    "result": {"id": f"{MOD}:micro_quartz_fiber", "count": 4}})
def ingredients(*ids):
    return [{"tag": value[1:]} if value.startswith("#") else {"item": value} for value in ids]
for tier in [1, 3]:
    cable = lambda kind: item(kind, "fluix", tier)
    if tier == 1:
        glass = [f"{MOD}:micro_quartz_fiber"] + [f"{MOD}:fluix_crystal_fragment"] * 4
        wool = [f"#{MOD}:wool_fragments"] * 2
        electronics = [f"{MOD}:redstone_fragment"] * 2 + [f"{MOD}:glowstone_fragment"] * 2
    else:
        glass = ["ae2:quartz_fiber"] * 4 + ["ae2:fluix_block"] * 2
        wool = ["#minecraft:wool"] * 4
        electronics = ["minecraft:redstone"] * 4 + ["minecraft:glowstone"]
    recipe(f"independent/glass_tier_{tier}", ingredients(*glass), cable("glass_cable"), 4)
    recipe(f"independent/covered_tier_{tier}", ingredients(cable("glass_cable"), *wool), cable("covered_cable"))
    recipe(f"independent/smart_tier_{tier}", ingredients(cable("covered_cable"), *electronics), cable("smart_cable"))
    shaped(f"independent/dense_covered_tier_{tier}", ["CC", "CC"], cable("covered_cable"), cable("covered_dense_cable"))
    recipe(f"independent/dense_smart_tier_{tier}", ingredients(cable("covered_dense_cable"), *electronics), cable("smart_dense_cable"))
    shaped(f"independent/dense_smart_from_smart_tier_{tier}", ["CC", "CC"], cable("smart_cable"), cable("smart_dense_cable"))

# A deliberate question-mark icon, not Minecraft's missing-texture checkerboard.
import struct
import zlib
pixels = bytearray(16 * 16 * 4)
glyph = ["11111", "00001", "00001", "00111", "00100", "00000", "00100"]
for y, row in enumerate(glyph):
    for x, bit in enumerate(row):
        if bit == "1":
            for dy in range(2):
                for dx in range(2):
                    offset = ((y * 2 + dy + 1) * 16 + x * 2 + dx + 3) * 4
                    pixels[offset:offset + 4] = bytes((220, 190, 255, 255))
def chunk(kind, data):
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))
image = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))
image += chunk(b"IDAT", zlib.compress(b"".join(b"\0" + pixels[y * 64:(y + 1) * 64] for y in range(16)))) + chunk(b"IEND", b"")
texture = ROOT / f"assets/{MOD}/textures/item/material_placeholder.png"
texture.parent.mkdir(parents=True, exist_ok=True)
texture.write_bytes(image)

write(f"assets/{MOD}/lang/zh_cn.json", zh)
write(f"assets/{MOD}/lang/en_us.json", en)
write("pack.mcmeta", {"pack": {"pack_format": 48, "description": "AE2 Cable Tiers resources"}})
print("Generated 170 cable models, 23 material models, 482 recipes, tier/color/material tags, placeholder texture, and translations.")
