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
        recipe(f"combine/{color}_{kind}", [{"item": low}] * 4, normal)
        recipe(f"upgrade/{color}_{kind}", [{"item": normal}] * 4, high)
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

# Direct starter recipe: the same ingredients as AE2's four-cable recipe produce sixteen tier-I cables.
recipe("starter_glass_tier_1", [{"item": "ae2:quartz_fiber"}, {"tag": "ae2:all_fluix"}, {"tag": "ae2:all_fluix"}], item("glass_cable", "fluix", 1), 16)
write(f"assets/{MOD}/lang/zh_cn.json", zh)
write(f"assets/{MOD}/lang/en_us.json", en)
write("pack.mcmeta", {"pack": {"pack_format": 48, "description": "AE2 Cable Tiers resources"}})
print("Generated 170 model references, 426 recipes, tier/color tags, and translations.")
