use bevy::prelude::*;
use bevy_zhack::{ZHackPlugin, ZHackRootBundle, ZIndex};

fn main() {
    App::new()
        .insert_resource(WindowDescriptor {
            title: "bevy_zhack example".to_string(),
            ..WindowDescriptor::default()
        })
        .add_plugins(DefaultPlugins)
        .add_plugin(ZHackPlugin)
        .add_startup_system(setup)
        .add_system(move_with_mouse)
        .run();
}

fn setup(mut commands: Commands) {
    commands.spawn_bundle(UiCameraBundle::default());

    // an ordinary rectangle
    // Bevy's z-indices currently start at 0 and increment by
    // `bevy_ui::update::UI_Z_STEP` = 0.001 per UI node child
    commands.spawn_bundle(NodeBundle {
        color: Color::VIOLET.into(),
        style: Style {
            position_type: PositionType::Absolute,
            size: Size::new(Val::Px(400.0), Val::Px(400.0)),
            position: Rect {
                left: Val::Px(0.0),
                top: Val::Px(0.0),
                ..Rect::default()
            },
            ..Style::default()
        },
        ..NodeBundle::default()
    });

    // but with the power of Z...
    commands
        // ...and making sure that the `ZHackRootBundle` has no parent entity...
        .spawn_bundle(ZHackRootBundle::default())
        .with_children(|builder| {
            builder
                .spawn_bundle(NodeBundle {
                    color: Color::TEAL.into(),
                    style: Style {
                        position_type: PositionType::Absolute,
                        position: Rect {
                            left: Val::Px(100.0),
                            bottom: Val::Px(570.0),
                            ..Rect::default()
                        },
                        size: Size::new(Val::Px(200.0), Val::Px(200.0)),
                        ..Style::default()
                    },
                    ..NodeBundle::default()
                })
                // ...you can set any arbitrary z-value!
                // note: by default Bevy's `UICamera` has a far plane at +999.9
                // if you set a higher z-index than that, your component won't render
                .insert(ZIndex(2.0))
                // ---------------------------------------
                .with_children(|builder| {
                    // this rectangle doesn't specify its own z-index and
                    // thus should inherit the base z-index of its parent
                    builder.spawn_bundle(NodeBundle {
                        color: Color::WHITE.into(),
                        style: Style {
                            position_type: PositionType::Absolute,
                            position: Rect {
                                left: Val::Px(150.0),
                                top: Val::Px(50.0),
                                ..Rect::default()
                            },
                            size: Size::new(Val::Px(100.0), Val::Px(100.0)),
                            ..Style::default()
                        },
                        ..NodeBundle::default()
                    });
                });
        });
}

/// also let the user move around the node with the
/// custom z-index to prove that it really works :D
fn move_with_mouse(windows: Res<Windows>, mut query: Query<&mut Style, With<ZIndex>>) {
    if let Ok(mut style) = query.get_single_mut() {
        if let Some(primary) = windows.get_primary() {
            if let Some(mouse_pos) = primary.cursor_position() {
                (*style).position.left = Val::Px(mouse_pos.x - 100.0);
                (*style).position.bottom = Val::Px(mouse_pos.y - 100.0);
            }
        }
    }
}
