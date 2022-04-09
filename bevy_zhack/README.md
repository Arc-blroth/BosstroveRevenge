# 💤 Bevy Z-Hack

[![License](https://img.shields.io/badge/license-MIT%2FApache--2.0-blue)](https://github.com/Arc-blroth/BosstroveRevenge/tree/try3/bevy_zhack)
![Dragon Powered](https://img.shields.io/badge/%F0%9F%90%89-dragon%20powered-brightgreen)

A quick and dirty hack for setting the z-index of Bevy UI nodes that works
Just Good Enough&trade; until the bikeshedding in
[bevyengine/bevy#1275](https://github.com/bevyengine/bevy/issues/1275) settles.

## Getting Started in 3 Ez Steps

1. Add this crate to your Cargo.toml:

```toml
bevy_zhack = { git = "https://github.com/Arc-blroth/BosstroveRevenge", version = "0.1.0", rev = "<commit id here>" }
```

|Make sure to pin a specific revision since this crate will likely be removed from HEAD once an official z-indexing solution is implemented!|
|-----|

2. Add the `ZHackPlugin` to your App:

```rust
use bevy::prelude::*;
use bevy_zhack::ZHackPlugin;

fn main() {
    App::new()
        .add_plugins(DefaultPlugins)
        .add_plugin(ZHackPlugin)
        // other plugins here
        .run();
}
```

3. Add UI nodes under a `ZIndexHackBundle`, and tag each UI node you want to set the z-index of with the `ZIndex` component:

```rust
use bevy::prelude::*;
use bevy_zhack::{ZHackRootBundle, ZIndex};

fn setup(mut commands: Commands) {
    commands
        // make sure that the `ZHackRootBundle` has no parent entity here
        .spawn_bundle(ZHackRootBundle::default())
        .with_children(|builder| {
            builder
                .spawn_bundle(NodeBundle {
                    color: Color::TEAL.into(),
                    style: Style {
                        position_type: PositionType::Absolute,
                        position: Rect {
                            left: Val::Px(100.0),
                            top: Val::Px(100.0),
                            ..Rect::default()
                        },
                        size: Size::new(Val::Px(200.0), Val::Px(200.0)),
                        ..Style::default()
                    },
                    ..NodeBundle::default()
                })
                // set any arbitrary z-value here
                // note: by default Bevy's `UICamera` has a far plane at +999.9
                // if you set a higher z-index than that, your component won't render
                .insert(ZIndex(2.0));
        });
}
```

## License

This crate is dual-licensed under either:

- the [Apache License, Version 2.0](LICENSE-APACHE)
- the [MIT license](LICENSE-MIT)

at your option.

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be dual licensed as above, without any additional terms or conditions.
