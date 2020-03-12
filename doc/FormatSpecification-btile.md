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
- Defaults to true. Ignored if a builder is specified.

### `viscosity`
- The resistance the player recieves while walking through this tile.
- Defaults to 0.0. Ignored if a builder is specified.

### `texture`
- Can be either a single string, in which case that texture is used,
  or can be an array of texture locations. Textures are automatically
  overlaid, with the first element in the array corresponding to the
  topmost texture.
- If a texture cannot be loaded, it will be replaced with the default texture.
- If not specified, texture will be set to the default.
- Required, though builders may ignore the built texture.

### `builder`
- Fully qualified class name of a FloorTileBuilder<? extends FloorTile> or WallTileBuilder<? extends WallTile>.
- If present, this class becomes responsible for parsing the json of a tile and constructing the corresponding ITile.
- Optional.
