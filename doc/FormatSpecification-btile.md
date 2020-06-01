# .btile File Format (Version 1)
A Bosstrove Tile file is a json file that defines a tile object
(specifically a subclass of either `WallTile` or `FloorTile`)

Example File:

```
{
  "versionId": 1,
  "tileId": "boss.sand",
  "tileType": "floorTile",
  "passable": true,
  "viscosity": 0.0,
  "texture": [
    "data/texture/deco/shells.png",
    "data/texture/tiles/sand.png"
  ]
}
```

# Elements

### `versionId`
- Should be 1
- Required

### `tileId`
- The key that this tile will be registered with. Must be unique.
- Required

### `tileType`
- Either `floorTile` or `wallTile` (case insensitive)
- Required

### `passable`
- Whether or not the player can walk through/on the tile.
- Defaults to false. Ignored if a builder is specified.

### `viscosity`
- The resistance the player recieves while walking through this tile.
- Defaults to 0.0. Ignored if a builder is specified.

### `smart`
- If `true`, the texture can have different variants based on the existence of neighboring blocks with the same type.
- This can help in building "connected" textures across tiles.
- Defaults to `false`. Cannot be true if `builder` is specified.

### `texture`
- If `smart` is false or unspecified:
    - Can be either a single string, in which case that texture is used,
      or can be an array of texture locations. Textures are automatically
      overlaid, with the first element in the array corresponding to the
      topmost texture.
    - If a texture cannot be loaded, it will be replaced with the default texture.
    - If not specified, texture will be set to the default.
    - Required, though builders may ignore the built texture.
- If `smart` is true:
    - Must be an array of objects with the following format:
    - `directions`
      - Either the string "default", or an array of any combination of "north", "south", "east", and "west". Repeat elements will lead to undefined behavior. The empty array is permitted.
      - When a tile is bordered with tiles of the same type in these directions, the following texture will be used.
      - For example, if `directions = ["north", "east"]`, then the following texture would be used when there is a tile of the same type to the north and to the east (and tiles of different types to the south and west).
    - `texture`
      - A string or array of strings specifying a texture as in the above specification.
    - If there is no texture for a combination of directions, the tile will default the default texture, or if that is not specified, to an invisible texture.
    - Duplicate mappings between a certain combination of directions and a texture will result in undefined behavior.

### `hitbox`
- An object specifying the hitbox of this tile in the following format:
    - `x`
        - X-offset of the hitbox.
    - `y`
        - Y-offset of the hitbox.
    - `w`
        - Width of the hitbox.
    - `h`
        - Height of the hitbox.
- Optional. Defaults to `{ "x": 0, "y": 0, "w": 1, "h": 1}`. Builders may choose to ignore this hitbox.

### `builder`
- Fully qualified class name of a FloorTileBuilder<? extends FloorTile> or WallTileBuilder<? extends WallTile>.
- If present, this class becomes responsible for parsing the json of a tile and constructing the corresponding ITile.
- Optional.
