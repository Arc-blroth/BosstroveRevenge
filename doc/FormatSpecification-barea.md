# .bent File Format (Version 1)
A Bosstrove Entity file is a json file that defines a
type of Area. An area is a textureless hitbox that players
and entities can interact with.

Example File:

```
{
  "versionId": 1,
  "areaId": "boss.roomChange",
  "class": "ai.arcblroth.boss.game.area.RoomChangeArea"
}
```

# Elements

### `versionId`
- Should be 1
- Required

### `areaId`
- The key that this area will be registered with. Must be unique.
- Required

### `class`
- Fully qualified class name of an Area subclass.
- Required