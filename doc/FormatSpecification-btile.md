# .btile File Format (Version 1)
A Bosstrove Tile file is a json file that defines a tile object
(specifically a subclass of either `WallTile` or `FloorTile`)
It may also specify default behaviors.

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
  ],
  "events": {
    ...
  }
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
- Defaults to true.

### `viscosity`
- The resistance the player recieves while walking through this tile.
- Defaults to 0.0.

### `texture`
- Can be either a single string, in which case that texture is used,
  or can be an array of texture locations. Textures are automatically
  overlaid, with the first element in the array corresponding to the
  topmost texture.
- If a texture cannot be loaded, it will be replaced with the default texture.
- If not specified, texture will be set to the default.

### `events`
- Specifies the event triggers for this tile. See the
  [Event Specification](https://github.com/Arc-blroth/BosstroveRevenge/blob/master/doc/EventSpecification.md).