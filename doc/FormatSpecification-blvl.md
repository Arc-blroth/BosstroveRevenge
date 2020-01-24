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
  "title": "Lockdown",
  
  "globalData": [],
  
  "persistentData": [
    "chaseFinishedYet"
  ],
  
  "triggerData": [
    "counter1"
  ],
  
  "rooms": [
    {
      "roomId": "one",
      "width": 32,
      "height": 16,
      "initial": true,
      "initialX": 0,
      "initialY": 8,
      "background": "data/texture/backgrounds/greenStuff.png",
      "foreground": "data/texture/foregrounds/sunrays.png",
      
      "floorTiles": [
	    [
          "boss.grass",
          "boss.sand",
          ...
        ],
	    ...
	  ]
      
      "wallTiles": [
        [
          "boss.tree",
          {
            "tileId": "boss.trigger"
            "events": {
              "onEntityStep": [
                {
                  "operation": "set",
                  "var": "%chaseFinishedYet",
                  "value": "1"
                },
                {
                  "operation": "math",
                  "var": "$counter1",
                  "lhs": "$counter1",
	      	     "type": "multiply",
                  "rhs": "2"
                },
                {
                  "operation": "cutscene",
                  "trigger": "boss.chaseDone"
                }
              ]
            }
          },
          "boss.grass",
          "boss.coin",
          "boss.crow",
          ...
        ],
        ...
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

### `globalData`
- Global data stores variables that persist
  on the save file and between levels.
- Global variables are referred to in events
  as `#variableName`.
- Optional

### `persistentData`
- Persistent data stores variables that persist
  on the save file, and thus between replays of
  a level.
- Persistent variables are referred to in events
  as `%variableName`.
- Optional

### `triggerData`
- Trigger data stores variables that are not stored
  between replays of a level. These variables can
  be used for temporary keys, etc.
- Trigger variables are referred to in events
  as `$variableName`.
- Optional

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

### `initial`
- Whether or not this room is the first one in the level.
- Only one room may be the initial. If multiple rooms are marked as "initial", the first room marked is picked. If no rooms are marked as "initial", the first room in the array is used.
- Required

### `initialX` and `initialY`
- Marks the spawn point for the player when they enter a level.
- Required for the initial room, ignored for every other room

### `background` and `foreground`
- Specify background and foreground textures that are overlaid onto the level's tiles.
- Each is optional

### `floorTiles`
- floorTiles is a two-dimensional array that specifies the level's floor tiles.
- Each sub-array stores a _row_ of tiles.
- Required

### `wallTiles`
- wallTiles is a two-dimensional array that specifies the level's wall tiles.
- Each sub-array stores a _row_ of tiles.
- Required

# Specifying tiles
## The Usual Way
Tiles can be specified by their string tileId: `"boss.sand"`.

## The Special Way
For tiles that have special triggers, a tile can also be specified as a _json object_:

### `tileId`
- The id of this tile.
- Required

### `events`
- Specifies the event triggers for this tile. See the
  [Event Specification](https://github.com/Arc-blroth/BosstroveRevenge/blob/master/doc/EventSpecification.md).
- Required
