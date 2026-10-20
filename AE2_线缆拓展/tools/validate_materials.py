"""Check reversible material values, independent routes, and placeholder references."""
import json
from fractions import Fraction as F
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
MOD = 'ae2_cable_tiers'
RECIPES = ROOT / f'data/{MOD}/recipe'
COLORS = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black']
SOURCES = {
    'fluix_crystal_fragment': 'ae2:fluix_crystal',
    'certus_quartz_fragment': 'ae2:certus_quartz_crystal',
    'nether_quartz_fragment': 'minecraft:quartz',
    'charged_certus_quartz_fragment': 'ae2:charged_certus_quartz_crystal',
    'redstone_fragment': 'minecraft:redstone',
    'glowstone_fragment': 'minecraft:glowstone_dust',
    **{c + '_wool_fragment': 'minecraft:' + c + '_wool' for c in COLORS},
}
def read(name):
    return json.loads((RECIPES / (name + '.json')).read_text())
for fragment, source in SOURCES.items():
    split = read('materials/split_' + fragment)
    combine = read('materials/combine_' + fragment)
    assert split['ingredients'] == [{'item': source}]
    assert split['result'] == {'id': MOD + ':' + fragment, 'count': 8}
    assert combine['ingredients'] == [{'item': MOD + ':' + fragment}] * 8
    assert combine['result'] == {'id': source, 'count': 1}
for name in ['micro_quartz_fiber', *SOURCES]:
    model = json.loads((ROOT / f'assets/{MOD}/models/item/{name}.json').read_text())
    assert model['textures']['layer0'] == MOD + ':item/material_placeholder'
assert (ROOT / f'assets/{MOD}/textures/item/material_placeholder.png').read_bytes().startswith(b'\x89PNG\r\n\x1a\n')
micro = read('materials/micro_quartz_fiber')
assert micro['pattern'] == ['G', 'Q', 'G']
assert micro['key'] == {'G': {'tag': 'c:glass_blocks/cheap'}, 'Q': {'tag': 'ae2:all_quartz_dust'}}
assert micro['result'] == {'id': MOD + ':micro_quartz_fiber', 'count': 4}
wool_tag = json.loads((ROOT / f'data/{MOD}/tags/item/wool_fragments.json').read_text())
assert set(wool_tag['values']) == {MOD + ':' + c + '_wool_fragment' for c in COLORS}
# Fragments never enter whole-material tags, so machines cannot value 1/8 as one whole.
for path in (ROOT / 'data').rglob('tags/item/*.json'):
    if f'/data/{MOD}/' not in path.as_posix():
        assert not any(MOD + ':' + name in path.read_text() for name in SOURCES)

# Vectors: ordinary quartz fiber, fluix crystal, wool, redstone dust, glowstone dust.
def vector(*values): return tuple(F(v) for v in values)
def add(a, b): return tuple(x + y for x, y in zip(a, b))
def scale(a, n): return tuple(x * n for x in a)
zero = vector(0, 0, 0, 0, 0)
glass = vector(F(1, 4), F(1, 2), 0, 0, 0)
covered = add(glass, vector(0, 0, 1, 0, 0))
smart = add(covered, vector(0, 0, 0, 1, 1))
dense_covered = scale(covered, 4)
dense_smart = add(dense_covered, vector(0, 0, 0, 1, 1))
normal = dict(zip(['glass', 'covered', 'smart', 'dense_covered', 'dense_smart'], [glass, covered, smart, dense_covered, dense_smart]))
suffix = {'glass': 'glass_cable', 'covered': 'covered_cable', 'smart': 'smart_cable', 'dense_covered': 'covered_dense_cable', 'dense_smart': 'smart_dense_cable'}
for tier, factor in [(1, F(1, 4)), (3, F(4))]:
    costs = {
        'ae2:quartz_fiber': vector(1, 0, 0, 0, 0), 'ae2:fluix_block': vector(0, 4, 0, 0, 0),
        MOD + ':micro_quartz_fiber': vector(F(1, 4), 0, 0, 0, 0),
        MOD + ':fluix_crystal_fragment': vector(0, F(1, 8), 0, 0, 0),
        '#' + MOD + ':wool_fragments': vector(0, 0, F(1, 8), 0, 0),
        '#minecraft:wool': vector(0, 0, 1, 0, 0),
        MOD + ':redstone_fragment': vector(0, 0, 0, F(1, 8), 0),
        MOD + ':glowstone_fragment': vector(0, 0, 0, 0, F(1, 8)),
        'minecraft:redstone': vector(0, 0, 0, 1, 0), 'minecraft:glowstone': vector(0, 0, 0, 0, 4),
    }
    for kind, baseline in normal.items():
        r = read(f'independent/{kind}_tier_{tier}')
        inputs = r.get('ingredients')
        if inputs is None:
            inputs = [r['key'][char] for row in r['pattern'] for char in row if char != ' ']
        total = zero
        for ingredient in inputs:
            key = ingredient.get('item') or '#' + ingredient['tag']
            total = add(total, costs[key])
        quantity = r['result']['count']
        result = scale(total, F(1, quantity))
        assert result == scale(baseline, factor), (kind, tier, result)
        costs[MOD + f':fluix_{suffix[kind]}_tier_{tier}'] = result
    alt = read(f'independent/dense_smart_from_smart_tier_{tier}')
    assert alt['result'] == {'id': MOD + f':fluix_smart_dense_cable_tier_{tier}', 'count': 1}
print('Passed: 22 reversible material pairs; 16 wool colors; charged identity; 23 placeholder models; exact 1/4 and 4x direct-route material costs.')
