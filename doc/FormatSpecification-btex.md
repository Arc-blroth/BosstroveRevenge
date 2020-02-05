# .btex File Format (Version 1)
A Bosstrove Texture is a json file that defines an animated
texture.

Example File:

```
{
  "versionId": 1,
  "spritesheet": "data/texture/entity/xulpir.png",
  "frames": 5,
  "width": 16,
  "height": 16,
  "rate": 10
}
```

# Elements

### `versionId`
- Should be 1
- Required

### `spritesheet`
- The spritesheet that contains the frames for this animated texture.
- Required

### `frames`
- Total number of frames in texture.
- Required

### `width` and `height`
- Size of the sprite (_not_ the spritesheet)
- Required

### `rate`
- Specifies the rate at which frames are advanced, in steps (where there are 30 steps per second).
- Defaults to 1