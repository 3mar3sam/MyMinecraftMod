#!/usr/bin/env python3
"""
Convert Sponge Schematic (.schem) v3 to Minecraft Vanilla Structure Template (.nbt).
Optimized by omitting unnecessary empty air blocks outside the mountain boundaries.
"""

import gzip
import struct
import math
import os
import sys

def read_string(f):
    length = struct.unpack('>H', f.read(2))[0]
    return f.read(length).decode('utf-8', errors='replace')

def read_tag(f, tag_type):
    if tag_type == 0:
        return None
    elif tag_type == 1:
        return struct.unpack('>b', f.read(1))[0]
    elif tag_type == 2:
        return struct.unpack('>h', f.read(2))[0]
    elif tag_type == 3:
        return struct.unpack('>i', f.read(4))[0]
    elif tag_type == 4:
        return struct.unpack('>q', f.read(8))[0]
    elif tag_type == 5:
        return struct.unpack('>f', f.read(4))[0]
    elif tag_type == 6:
        return struct.unpack('>d', f.read(8))[0]
    elif tag_type == 7:
        length = struct.unpack('>i', f.read(4))[0]
        return f.read(length)
    elif tag_type == 8:
        return read_string(f)
    elif tag_type == 9:
        sub_type = struct.unpack('>b', f.read(1))[0]
        length = struct.unpack('>i', f.read(4))[0]
        return (sub_type, [read_tag(f, sub_type) for _ in range(length)])
    elif tag_type == 10:
        res = {}
        while True:
            sub_tag = struct.unpack('>b', f.read(1))[0]
            if sub_tag == 0:
                break
            name = read_string(f)
            res[name] = (sub_tag, read_tag(f, sub_tag))
        return res
    elif tag_type == 11:
        length = struct.unpack('>i', f.read(4))[0]
        return [struct.unpack('>i', f.read(4))[0] for _ in range(length)]
    elif tag_type == 12:
        length = struct.unpack('>i', f.read(4))[0]
        return [struct.unpack('>q', f.read(8))[0] for _ in range(length)]
    else:
        raise ValueError(f"Unknown tag {tag_type}")

def write_string(f, s):
    b = s.encode('utf-8')
    f.write(struct.pack('>H', len(b)))
    f.write(b)

def write_tag(f, tag_type, value):
    if tag_type == 1:
        f.write(struct.pack('>b', value))
    elif tag_type == 2:
        f.write(struct.pack('>h', value))
    elif tag_type == 3:
        f.write(struct.pack('>i', value))
    elif tag_type == 4:
        f.write(struct.pack('>q', value))
    elif tag_type == 5:
        f.write(struct.pack('>f', value))
    elif tag_type == 6:
        f.write(struct.pack('>d', value))
    elif tag_type == 7:
        f.write(struct.pack('>i', len(value)))
        f.write(value)
    elif tag_type == 8:
        write_string(f, value)
    elif tag_type == 9:
        sub_type, items = value
        f.write(struct.pack('>b', sub_type))
        f.write(struct.pack('>i', len(items)))
        for item in items:
            write_tag(f, sub_type, item)
    elif tag_type == 10:
        for k, (t, v) in value.items():
            f.write(struct.pack('>b', t))
            write_string(f, k)
            write_tag(f, t, v)
        f.write(b'\x00') # TAG_End
    elif tag_type == 11:
        f.write(struct.pack('>i', len(value)))
        for item in value:
            f.write(struct.pack('>i', item))
    elif tag_type == 12:
        f.write(struct.pack('>i', len(value)))
        for item in value:
            f.write(struct.pack('>q', item))

def convert(input_path, output_path):
    print(f"Reading Sponge schematic from: {input_path}")
    with gzip.open(input_path, "rb") as f:
        root_type = struct.unpack('>b', f.read(1))[0]
        root_name = read_string(f)
        root = read_tag(f, root_type)
        schem = root['Schematic'][1] if 'Schematic' in root else root

    width = schem['Width'][1]
    height = schem['Height'][1]
    length = schem['Length'][1]
    data_version = schem.get('DataVersion', (3, 3955))[1]
    palette_raw = schem['Blocks'][1]['Palette'][1]
    data_bytes = schem['Blocks'][1]['Data'][1]
    
    be_list_tag = schem['Blocks'][1].get('BlockEntities')
    block_entities_raw = be_list_tag[1][1] if be_list_tag else []

    print(f"Schematic Dimensions: {width} x {height} x {length}")
    print(f"DataVersion: {data_version}")
    print(f"Palette size: {len(palette_raw)}")
    print(f"BlockEntities count: {len(block_entities_raw)}")

    # Decode VarInt block IDs
    block_ids = []
    i = 0
    bytes_len = len(data_bytes)
    while i < bytes_len:
        value = 0
        varint_length = 0
        while True:
            b = data_bytes[i]
            i += 1
            value |= (b & 0x7F) << (varint_length * 7)
            varint_length += 1
            if (b & 0x80) == 0:
                break
        block_ids.append(value)

    air_id = palette_raw['minecraft:air'][1]

    # Convert Palette to Vanilla Structure Template format
    palette_entries = []
    old_to_new_palette = {}
    for name, (_, old_id) in sorted(palette_raw.items(), key=lambda x: x[1][1]):
        if '[' in name and name.endswith(']'):
            bname, props_str = name[:-1].split('[', 1)
            props = {}
            for prop in props_str.split(','):
                pk, pv = prop.split('=', 1)
                props[pk] = (8, pv)
            entry = {
                'Name': (8, bname),
                'Properties': (10, props)
            }
        else:
            entry = {
                'Name': (8, name)
            }
        old_to_new_palette[old_id] = len(palette_entries)
        palette_entries.append(entry)

    # Map block entities by (x, y, z)
    be_map = {}
    for be in block_entities_raw:
        pos = tuple(be['Pos'][1])
        data = be['Data'][1]
        be_map[pos] = data

    # Analyze mountain terrain surface
    max_y = [[0 for _ in range(length)] for _ in range(width)]
    for y in range(height):
        for z in range(length):
            for x in range(width):
                idx = (y * length + z) * width + x
                if block_ids[idx] != air_id:
                    if y > max_y[x][z]:
                        max_y[x][z] = y

    # Altar center position
    cx, cz = 71, 70
    
    # Mountain ridge calculation around center
    ridge_h = [0] * 360
    ridge_r = [0] * 360
    for deg in range(360):
        rad = math.radians(deg)
        for r in range(1, 75):
            x = int(round(cx + r * math.cos(rad)))
            z = int(round(cz + r * math.sin(rad)))
            if 0 <= x < width and 0 <= z < length:
                h = max_y[x][z]
                if h > ridge_h[deg]:
                    ridge_h[deg] = h
                    ridge_r[deg] = r

    # Build blocks list
    # Omit air blocks outside the mountain boundaries (air above the rim, air outside the outer base, and open sky air)
    blocks_list = []
    altar_found = False
    altar_pos_found = None

    for y in range(height):
        for x in range(width):
            for z in range(length):
                idx = (y * length + z) * width + x
                bid = block_ids[idx]
                
                keep = False
                if bid != air_id:
                    keep = True
                else:
                    # Check if inside mountain boundaries (inside the arena bowl or underground pocket)
                    dx = x - cx
                    dz = z - cz
                    dist = math.hypot(dx, dz)
                    deg = int(math.degrees(math.atan2(dz, dx))) % 360
                    if dist < ridge_r[deg] and y <= ridge_h[deg]:
                        # Inside the bowl enclosed by the mountain ridge
                        keep = True
                    elif y < max_y[x][z]:
                        # Inside the solid terrain (e.g. underground air)
                        keep = True
                
                if keep:
                    state_idx = old_to_new_palette[bid]
                    block_compound = {
                        'pos': (9, (3, [x, y, z])),
                        'state': (3, state_idx)
                    }
                    if (x, y, z) in be_map:
                        block_compound['nbt'] = (10, be_map[(x, y, z)])
                    
                    bname = palette_entries[state_idx]['Name'][1]
                    if 'sand_worm_altar' in bname:
                        altar_found = True
                        altar_pos_found = [x, y, z]

                    blocks_list.append((10, block_compound))

    print(f"Total blocks in converted structure: {len(blocks_list)}")
    print(f"Center altar block found: {altar_found} at pos: {altar_pos_found}")

    root_out = {
        'DataVersion': (3, data_version),
        'size': (9, (3, [width, height, length])),
        'palette': (9, (10, palette_entries)),
        'blocks': (9, (10, [b[1] for b in blocks_list])),
        'entities': (9, (10, []))
    }

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    print(f"Writing Vanilla Structure Template to: {output_path}")
    with gzip.open(output_path, "wb") as out:
        out.write(b'\n') # TAG_Compound
        write_string(out, "")
        write_tag(out, 10, root_out)

    size_kb = os.path.getsize(output_path) / 1024
    print(f"Done! Written successfully. File size: {size_kb:.1f} KB ({size_kb / 1024:.2f} MB)")

if __name__ == '__main__':
    in_file = "run/client/config/worldedit/schematics/sand_worm_arena.schem"
    out_file = "src/main/resources/data/examplemod/structure/sand_worm_arena.nbt"
    if len(sys.argv) > 1:
        in_file = sys.argv[1]
    if len(sys.argv) > 2:
        out_file = sys.argv[2]
    convert(in_file, out_file)
