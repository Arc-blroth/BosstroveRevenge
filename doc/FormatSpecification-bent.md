# .bent File Format (Version 1)
A Bosstrove Entity file is a json file that defines the information
required to deserialize and construct IEntity's from JSON.

Example File:

```
{
  "versionId": 1,
  "entityId": "boss.xulpir",
  "class": "ai.arcblroth.boss.game.entity.Xulpir",
  "deserializer": "ai.arcblroth.boss.game.entity.XulpirDeserializer"
}
```

# Elements

### `versionId`
- Should be 1
- Required

### `entityId`
- The key that this entity will be registered with. Must be unique.
- Required

### `class`
- Fully qualified class name of an IEntity implementation.
- Required

### `deserializer`
- Fully qualified class name of a JsonDeserializer<class>.
- Required


# Specifying Entities in Levels

Example Entity Declaration:

```
{
  "entityId": "boss.xulpir",
  "x": 3,
  "y": 5,
  "happiness": 9001
}
```

Entities are specified as json objects, with a few required fields.
All other fields are deserialized depending on the `deserializer` class in the entity specification.

### `entityId`
- Id of the entity.
- Required

### `x` and `y`
- The initial position of the entity.
- Required
