# .blvl File Format (Version 1)
A Bosstrove Level file is a json file that defines the information
required to construct the structure and events in a level.

Example File:

```
{
  "versionId": 1,
  "levelId": "boss.w1l1",
  "world": 1,
  "level": 1,
  "title": "Lockdown",,
  "initialRoom": "one",
      
  "palette": [
    "boss.grass",
    "boss.sand",
    "boss.tree"
  ]
  
  "rooms": [
    {
      "roomId": "one",
      "width": 32,
      "height": 16,
      "resetColor": "#000000",
      "initial": true,
      "initialX": 0,
      "initialY": 8,
      "background": "data/texture/backgrounds/greenStuff.png",
      "foreground": "data/texture/foregrounds/sunrays.png",
      
      "floorTiles": [
         [
           0,
           "boss.gravel",
           1,
           ...
         ],
         ...
      ]
      
      "wallTiles": [
        [
          2,
          {
            "tileId": "boss.lever"
          },
          2,
          "boss.rock",
          "boss.crow",
          ...
        ],
        ...
      ],
      
      "entities": [
      	{
      		"entityId": "boss.xulpir",
      		"x": 3,
      		"y": 5
      	}
      ]
      
    },
    {
      "roomId": 2,
      ...
    }
  ]
}
```

# Elements

### `versionId`
- Should be 1
- Required

### `levelId`
- The key that this levelId will be registered with. Must be unique.
- Required

### `world`
- World number to display
- Required

### `level`
- Level number to display
- Required

### `title`
- Level display title
- Required

### `introBackgroundColor` and `introForegroundColor`
- Defines the colors for the level intro animation
- Optional. Defaults to black for the background and white for the foreground.

### `initialRoom`
- ID of the first room in this level.
- Optional. Defaults to the first room in the `rooms` array.

### `palette`
- A tileset "palette" that can store commonly-used tile definitions.
- To refer to these tiles, specify the array index of the definition instead of a tile string or object.

### `rooms`
- List of the rooms in this level.
- Required

# Rooms
Within each level is an array of rooms. Each
room object stores some metadata and a complete
list of floorTiles and wallTiles.


### `roomId`
- Unique (within level) id for this room.
- Required

### `width`
- Width, in tiles, of the room.
- Required

### `height`
- Height, in tiles, of the room.
- Required

### `resetColor`
- Set the color shown in the sidebars and behind any backgrounds.
- Must be a 6-digit hex string (Example: "#00bfff")
- Optional

### `initialX` and `initialY`
- Marks the spawn point for the player when they enter a level.
- Highly suggested. If not present, defaults to (0, 0).

### `background` and `foreground`
- Specify background and foreground textures that are overlaid onto the level's tiles.
- Each is optional

### `roomEngine`
- Fully qualified class name of a RoomEngine class.
- This RoomEngine's callbacks will be called while this room is loaded.
- Optional

### `floorTiles`
- floorTiles is a two-dimensional array that specifies the level's floor tiles.
- Each sub-array stores a _row_ of tiles.
- Required

### `wallTiles`
- wallTiles is a two-dimensional array that specifies the level's wall tiles.
- Each sub-array stores a _row_ of tiles.
- Required

### `entities`
- List of the entities in this room. See the Format Specification for bent for more information.
- Optional.

# Specifying Tiles
## Simple Tiles
Tiles can be specified by their string tileId: `"boss.sand"`.

## Tiles with Extra Properties
For tiles that have special properties, a tile can also be specified as a _json object_:

### `tileId`
- The id of this tile.
- Required

All other properties are deserialized by the tile's FloorTileBuilder or a WallTileBuilder.

## Using Paletted Tiles
To save space and make levels more readable, you can put tile strings or objects in the level `palette` array.
Then, you can refer to those tiles by specifying their index in the array.
Note that both floorTiles and wallTiles go into the same palette!

See the example file above for an example.