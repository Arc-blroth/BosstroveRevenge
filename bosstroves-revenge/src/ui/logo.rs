use bevy::app::{App, Plugin};
use bevy::asset::AssetServer;
use bevy::core::Time;
use bevy::math::{Rect, Size, Vec3, Vec4};
use bevy::prelude::{
    BuildChildren, Color, Component, ImageBundle, NodeBundle, Query, Res, SystemSet, TextBundle, Transform, With,
};
use bevy::text::{HorizontalAlign, Text, TextAlignment, VerticalAlign};
use bevy::ui::{AlignSelf, FlexDirection, JustifyContent, PositionType, Style, UiColor, Val};

use crate::ui::hsv::HSVA;
use crate::ui::styles::UIStyles;
use crate::{Commands, GameState};

// Padding height and logo colors from
// https://github.com/Arc-blroth/BosstroveRevenge/blob/try1/core/src/main/java/ai/arcblroth/boss/load/LoadEngine.java#L20
// Note that a single "row" of characters in a `PixelAndTextGrid`
// took up 16 pixels and was represented by 2 units of height.
const ARBITRARY_PADDING_HEIGHT: f32 = 8.0 * 16.0;
const LIGHT_BLUE: HSVA = HSVA::hsv(0.5699301, 0.572, 0.98039216);
const SAT_BLUE: HSVA = HSVA::hsv(0.56931466, 0.8392157, 1.0);

/// The old Bosstrove's Revenge ran at 30 FPS. We keep the
/// logo glow animation at the same speed as in the original
/// and step the animation by [LOGO_ANIMATION_SPEED] every
/// this amount of milliseconds.
const MS_PER_STEP: f32 = 1000.0 / 30.0;

/// Every [MS_PER_STEP] the logo glow animation
/// is incremented by this amount.
const LOGO_ANIMATION_SPEED: f32 = 0.01;

/// Displays the initial "logo" screen during the
/// initial loading state of the game. This is
/// automatically initialized by the
/// [`LoadingPlugin`](crate::load::LoadingPlugin).
pub struct LogoScreenPlugin;

impl Plugin for LogoScreenPlugin {
    fn build(&self, app: &mut App) {
        app.add_startup_system(setup)
            .add_system_set(SystemSet::on_update(GameState::Loading).with_system(update_logo));
    }
}

#[derive(Component)]
struct Logo;

fn setup(mut commands: Commands, asset_server: Res<AssetServer>, styles: Res<UIStyles>) {
    commands
        .spawn_bundle(NodeBundle {
            color: Color::NONE.into(),
            style: Style {
                position_type: PositionType::Absolute,
                justify_content: JustifyContent::Center,
                flex_direction: FlexDirection::Column,
                size: Size::new(Val::Px(0.0), Val::Px(0.0)),
                position: Rect {
                    left: Val::Percent(50.0),
                    top: Val::Percent(50.0),
                    ..Rect::default()
                },
                ..Style::default()
            },
            ..NodeBundle::default()
        })
        .with_children(|builder| {
            builder
                .spawn_bundle(ImageBundle {
                    image: asset_server.load("bitmap.png").into(),
                    color: SAT_BLUE.as_rgba().into(),
                    style: Style {
                        position_type: PositionType::Absolute,
                        align_self: AlignSelf::Center,
                        position: Rect {
                            bottom: Val::Px(ARBITRARY_PADDING_HEIGHT / 2.0 - 8.0),
                            ..Rect::default()
                        },
                        ..Style::default()
                    },
                    transform: Transform::from_scale(Vec3::splat(8.0)),
                    ..ImageBundle::default()
                })
                .insert(Logo);

            builder.spawn_bundle(TextBundle {
                text: Text::with_section(
                    "Loading - 0%",
                    styles.text(Color::rgb(40.0 / 255.0, 237.0 / 255.0, 63.0 / 255.0)),
                    TextAlignment {
                        vertical: VerticalAlign::Bottom,
                        horizontal: HorizontalAlign::Center,
                    },
                ),
                style: Style {
                    position_type: PositionType::Absolute,
                    align_self: AlignSelf::Center,
                    position: Rect {
                        top: Val::Px(ARBITRARY_PADDING_HEIGHT - 8.0),
                        ..Rect::default()
                    },
                    ..Style::default()
                },
                ..TextBundle::default()
            });
        });
}

/// Make the logo change color!
fn update_logo(mut logo_color: Query<&mut UiColor, With<Logo>>, time: Res<Time>) {
    let millis = (time.time_since_startup().as_millis() % 20000) as f32;
    let blue_interpolation = (millis / (MS_PER_STEP / LOGO_ANIMATION_SPEED) % 2.0 - 1.0).abs();
    *logo_color.single_mut() = HSVA::from(Vec4::from(SAT_BLUE).lerp(LIGHT_BLUE.into(), blue_interpolation))
        .as_rgba()
        .into();
}
