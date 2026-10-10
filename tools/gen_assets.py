#!/usr/bin/env python3
"""Generates textures, models, blockstates, sounds.json and dimension data for Liminal Reverie."""
import json, os, random
from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
A = os.path.join(ROOT, "assets", "liminalreverie")
D = os.path.join(ROOT, "data", "liminalreverie")
MOD = "liminalreverie"

def w(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

def clamp(v): return max(0, min(255, int(v)))

def noisy(base, amt, rnd):
    n = rnd.randint(-amt, amt)
    return tuple(clamp(c + n) for c in base)

# ---------------------------------------------------------------- block textures
rnd = random.Random(1999)

def tex(name, fn, size=16):
    img = Image.new("RGBA", (size, size))
    for y in range(size):
        for x in range(size):
            img.putpixel((x, y), fn(x, y) + (255,))
    path = os.path.join(A, "textures", "block", name + ".png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)

tex("wallpaper", lambda x, y: noisy((222, 207, 166) if x % 4 else (212, 196, 152), 4, rnd))
tex("liminal_carpet", lambda x, y: noisy((168, 134, 118), 9, rnd))
tex("fluorescent_light",
    lambda x, y: (190, 190, 180) if x in (0, 15) or y in (0, 15) else noisy((252, 250, 232), 3, rnd))
tex("pool_tile",
    lambda x, y: (238, 246, 248) if x % 4 == 0 or y % 4 == 0 else noisy((158, 208, 226), 5, rnd))
tex("mall_tile",
    lambda x, y: noisy((240, 232, 214) if ((x // 8) + (y // 8)) % 2 == 0 else (224, 211, 190), 4, rnd))

def orb(x, y):
    dx, dy = x - 7.5, y - 7.5
    d = (dx * dx + dy * dy) ** 0.5
    t = max(0.0, 1.0 - d / 8.0)
    return (clamp(150 + 105 * t), clamp(120 + 110 * t), clamp(200 + 55 * t))
tex("memory_fragment", orb)

PATTERNS = [
    [".###.", "#...#", "#...#", "#...#", ".###."], ["..#..", "..#..", "#####", "..#..", "..#.."],
    ["..#..", ".#.#.", "#...#", "#...#", "#####"], ["..#..", ".#.#.", "#...#", ".#.#.", "..#.."],
    ["#####", "#...#", "#...#", "#...#", "#####"], ["#...#", ".#.#.", "..#..", ".#.#.", "#...#"],
    ["..#..", "#.#.#", ".#.#.", ".....", "....."], ["..#..", ".###.", "#.#.#", "..#..", "..#.."],
    ["#.#.#", ".....", "#.#.#", ".....", "#.#.#"], [".....", ".###.", "#.#.#", ".###.", "....."],
    [".###.", "#...#", "#.#.#", "#...#", "#####"], ["#####", "#....", "#.###", "#...#", "#####"]]
for i, pat in enumerate(PATTERNS):
    def f(x, y, pat=pat):
        px, py = x // 3, y // 3
        if px < 5 and py < 5 and pat[py][px] == "#":
            return (70, 52, 44)
        return noisy((226, 212, 172), 3, random.Random(x * 31 + y))
    tex("symbol_%d" % i, f)

# ---------------------------------------------------------------- item textures
def item(name, rows, palette):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in palette:
                img.putpixel((x, y), palette[ch] + (255,))
    p = os.path.join(A, "textures", "item", name + ".png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)

item("journal", [
    "................", "..bbbbbbbbbbbb..", "..bkkkkkkkkkkb..", "..bkpppppppkkb..", "..bkpppppppkkb..",
    "..bkkkkkkkkkkb..", "..bkkkrkkkkkkb..", "..bkkkrkkkkkkb..", "..bkkkrkkkkkkb..", "..bkkkkkkkkkkb..",
    "..bkkkkkkkkkkb..", "..bkkkkkkkkkkb..", "..bwwwwwwwwwwb..", "..bbbbbbbbbbbb..", "................",
    "................"],
    {"b": (74, 48, 34), "k": (138, 98, 70), "p": (226, 212, 172), "r": (176, 64, 72), "w": (232, 224, 200)})
item("brass_key", [
    "................", "................", "....yyyy........", "...yyyyyy.......", "...yy..yy.......",
    "...yy..yy.......", "...yyyyyy.......", "....yyyy........", ".....yy.........", ".....yy.........",
    ".....yyyy.......", ".....yy.........", ".....yyyy.......", ".....yy.........", "................",
    "................"],
    {"y": (214, 174, 72)})
item("faded_ticket", [
    "................", "................", "................", "..tttttttttttt..", ".tttddttttttddt.",
    ".ttttttttttttt..", ".tttdddddddttt..", ".ttttttttttttt..", ".tttdddddttttt..", ".ttttttttttttt..",
    "..tttttttttttt..", "................", "................", "................", "................",
    "................"],
    {"t": (232, 220, 190), "d": (150, 120, 96)})

# ---------------------------------------------------------------- NPC skins (64x64, player layout)
def skin(name, skin_c, hair, shirt, pants, eyes="open"):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    def rect(x1, y1, x2, y2, c):
        for y in range(y1, y2):
            for x in range(x1, x2):
                img.putpixel((x, y), c + (255,))
    rect(0, 0, 32, 16, skin_c)               # head (all faces)
    rect(8, 0, 16, 8, hair)                  # top
    rect(0, 8, 32, 11, hair)                 # hairline on all sides
    rect(24, 8, 32, 16, hair)                # back of head
    if eyes == "open":
        rect(10, 12, 11, 13, (40, 30, 30)); rect(13, 12, 14, 13, (40, 30, 30))
    else:
        rect(10, 12, 12, 13, (90, 70, 70)); rect(12, 12, 14, 13, (90, 70, 70))
    rect(11, 14, 13, 15, (170, 110, 100))    # mouth
    rect(16, 16, 40, 32, shirt)              # torso
    rect(40, 16, 56, 32, shirt)              # right arm (sleeve)
    rect(40, 28, 56, 32, skin_c)             # hand
    rect(0, 16, 16, 32, pants)               # right leg
    rect(16, 48, 32, 64, pants)              # left leg
    rect(32, 48, 48, 64, shirt)              # left arm
    rect(32, 60, 48, 64, skin_c)
    p = os.path.join(A, "textures", "entity", name + ".png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)

skin("caretaker", (226, 196, 170), (168, 168, 160), (110, 130, 104), (70, 76, 70))
skin("child", (240, 208, 180), (120, 80, 50), (236, 204, 96), (84, 100, 140))
skin("wanderer", (214, 178, 150), (60, 44, 36), (126, 96, 70), (64, 58, 64))
skin("sleeper", (232, 202, 184), (50, 40, 54), (176, 160, 214), (150, 140, 190), eyes="closed")

# ---------------------------------------------------------------- blockstates & models
FACES = ["down", "up", "north", "south", "west", "east"]
simple_blocks = ["wallpaper", "liminal_carpet", "fluorescent_light", "pool_tile", "mall_tile"]
for b in simple_blocks:
    w(os.path.join(A, "blockstates", b + ".json"), {"variants": {"": {"model": "%s:block/%s" % (MOD, b)}}})
    w(os.path.join(A, "models", "block", b + ".json"),
      {"parent": "minecraft:block/cube_all", "textures": {"all": "%s:block/%s" % (MOD, b)}})
    w(os.path.join(A, "models", "item", b + ".json"), {"parent": "%s:block/%s" % (MOD, b)})

w(os.path.join(A, "blockstates", "memory_fragment.json"),
  {"variants": {"memory=%d" % i: {"model": "%s:block/memory_fragment" % MOD} for i in range(32)}})
w(os.path.join(A, "models", "block", "memory_fragment.json"), {
    "textures": {"all": "%s:block/memory_fragment" % MOD, "particle": "%s:block/memory_fragment" % MOD},
    "elements": [{"from": [5, 5, 5], "to": [11, 11, 11], "faces": {f: {"texture": "#all"} for f in FACES}}]})
w(os.path.join(A, "models", "item", "memory_fragment.json"), {"parent": "%s:block/memory_fragment" % MOD})

w(os.path.join(A, "blockstates", "symbol_mark.json"),
  {"variants": {"symbol=%d" % i: {"model": "%s:block/symbol_%d" % (MOD, i)} for i in range(12)}})
for i in range(12):
    w(os.path.join(A, "models", "block", "symbol_%d.json" % i),
      {"parent": "minecraft:block/cube_all", "textures": {"all": "%s:block/symbol_%d" % (MOD, i)}})
w(os.path.join(A, "models", "item", "symbol_mark.json"), {"parent": "%s:block/symbol_0" % MOD})

for it in ["journal", "brass_key", "faded_ticket"]:
    w(os.path.join(A, "models", "item", it + ".json"),
      {"parent": "minecraft:item/generated", "textures": {"layer0": "%s:item/%s" % (MOD, it)}})

# ---------------------------------------------------------------- sounds.json (aliases of vanilla sound events)
def ev(name, vol=1.0, pitch=1.0):
    return {"name": name, "type": "event", "volume": vol, "pitch": pitch}
w(os.path.join(A, "sounds.json"), {
    "ambient_lobby": {"sounds": [ev("block.beacon.ambient", 0.9, 0.55)]},
    "ambient_school": {"sounds": [ev("block.conduit.ambient", 0.8, 0.8), ev("block.bell.resonate", 0.4, 0.5)]},
    "ambient_pool": {"sounds": [ev("block.pointed_dripstone.drip_water", 1.0, 0.8), ev("ambient.underwater.loop", 0.7, 1.0)]},
    "ambient_mall": {"sounds": [ev("block.note_block.harp", 0.5, 0.5), ev("block.note_block.harp", 0.5, 0.75), ev("block.beacon.ambient", 0.6, 0.5)]},
    "ambient_fields": {"sounds": [ev("item.elytra.flying", 0.4, 0.6), ev("entity.bee.loop", 0.5, 0.7)]},
    "ambient_hotel": {"sounds": [ev("block.beacon.ambient", 0.9, 0.45), ev("block.iron_door.close", 0.3, 0.6)]},
    "ambient_safe": {"sounds": [ev("block.campfire.crackle", 1.0, 1.0)]},
    "chime": {"sounds": [ev("block.note_block.chime", 1.0, 1.0)]},
    "wrong": {"sounds": [ev("block.note_block.bass", 1.0, 0.8)]},
    "solve": {"sounds": [ev("block.amethyst_block.resonate", 1.0, 1.0)]},
    "bell": {"sounds": [ev("block.bell.resonate", 1.0, 0.7)]},
    "ding": {"sounds": [ev("block.note_block.bell", 1.0, 1.0)]},
    "step": {"sounds": [ev("block.stone.step", 1.0, 0.8)]},
    "laugh": {"sounds": [ev("entity.allay.ambient_without_item", 1.0, 1.5)]},
    "wind": {"sounds": [ev("item.elytra.flying", 0.9, 0.5)]},
})

# ---------------------------------------------------------------- dimension data
w(os.path.join(D, "dimension_type", "the_reverie.json"), {
    "ultrawarm": False, "natural": False, "coordinate_scale": 1.0, "has_skylight": True, "has_ceiling": False,
    "ambient_light": 0.12, "fixed_time": 12000, "monster_spawn_light_level": 0, "monster_spawn_block_light_limit": 0,
    "piglin_safe": False, "bed_works": True, "respawn_anchor_works": False, "has_raids": False,
    "logical_height": 384, "min_y": 0, "height": 384, "infiniburn": "#minecraft:infiniburn_overworld",
    "effects": "minecraft:overworld"})
w(os.path.join(D, "dimension", "the_reverie.json"), {
    "type": "%s:the_reverie" % MOD,
    "generator": {"type": "minecraft:flat", "settings": {
        "biome": "minecraft:the_void", "features": False, "lakes": False,
        "layers": [{"block": "minecraft:air", "height": 1}], "structure_overrides": []}}})
print("assets generated")
