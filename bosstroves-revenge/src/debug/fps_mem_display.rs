use bevy::app::{App, Plugin};
use bevy::diagnostic::{Diagnostic, Diagnostics, FrameTimeDiagnosticsPlugin};
use bevy::prelude::{BuildChildren, Color, Commands, Component, Query, Rect, Res, TextBundle, With, Without};
use bevy::text::{HorizontalAlign, Text, TextAlignment, VerticalAlign};
use bevy::ui::{PositionType, Style, Val};
use bevy_zhack::{ZHackRootBundle, ZIndex};

use crate::ui::styles::UIStyles;
use crate::MemoryDiagnosticsPlugin;

const BYTES_IN_MB: f64 = (2 << 20) as f64;

/// Displays the current FPS and used memory at the top of the game output.
/// Based on the previous incarnations of this feature in
/// [try1](https://github.com/Arc-blroth/BosstroveRevenge/blob/dc451be706da7eee6f17241b1ae14257bcf9f041/console/src/main/java/ai/arcblroth/boss/io/console/AnsiOutputRenderer.java#L123)
/// and
/// [try2](https://github.com/Arc-blroth/BosstroveRevenge/blob/6fa583e5af7bf4676c04feabc6dec8fa5d0e05fc/core/src/main/kotlin/ai/arcblroth/boss/BosstrovesRevenge.kt#L36).
pub struct FPSAndMemoryDisplayPlugin;

impl Plugin for FPSAndMemoryDisplayPlugin {
    fn build(&self, app: &mut App) {
        app.add_startup_system(setup).add_system(update);
    }
}

#[derive(Component)]
struct FPSDisplay;

#[derive(Component)]
struct MemDisplay;

fn setup(mut commands: Commands, styles: Res<UIStyles>) {
    commands
        .spawn_bundle(ZHackRootBundle::default())
        .with_children(|builder| {
            builder
                .spawn_bundle(TextBundle {
                    text: Text::with_section(
                        "",
                        styles.text(Color::WHITE),
                        TextAlignment {
                            vertical: VerticalAlign::Top,
                            horizontal: HorizontalAlign::Left,
                        },
                    ),
                    style: Style {
                        position_type: PositionType::Absolute,
                        position: Rect {
                            left: Val::Px(2.0),
                            top: Val::Px(1.0),
                            ..Rect::default()
                        },
                        ..Style::default()
                    },
                    ..TextBundle::default()
                })
                .insert(ZIndex(999.0))
                .insert(FPSDisplay);

            builder
                .spawn_bundle(TextBundle {
                    text: Text::with_section(
                        "",
                        styles.text(Color::WHITE),
                        TextAlignment {
                            vertical: VerticalAlign::Top,
                            horizontal: HorizontalAlign::Right,
                        },
                    ),
                    style: Style {
                        position_type: PositionType::Absolute,
                        position: Rect {
                            right: Val::Px(3.0),
                            top: Val::Px(1.0),
                            ..Rect::default()
                        },
                        ..Style::default()
                    },
                    ..TextBundle::default()
                })
                .insert(ZIndex(999.0))
                .insert(MemDisplay);
        });
}

fn unwrap_diagnostic(diagnostic: Option<&Diagnostic>) -> f64 {
    diagnostic.and_then(|x| x.average()).unwrap_or(0.0)
}

fn update(
    mut fps_query: Query<&mut Text, (With<FPSDisplay>, Without<MemDisplay>)>,
    mut mem_query: Query<&mut Text, (With<MemDisplay>, Without<FPSDisplay>)>,
    diagnostics: Res<Diagnostics>,
) {
    let mut fps_text = fps_query.single_mut();
    let mut mem_text = mem_query.single_mut();

    if let Some(fps_display) = fps_text.sections.get_mut(0) {
        let fps = unwrap_diagnostic(diagnostics.get(FrameTimeDiagnosticsPlugin::FPS));
        fps_display.value = format!("{:.0} FPS", fps);
    }

    if let Some(mem_display) = mem_text.sections.get_mut(0) {
        let physical_mem = unwrap_diagnostic(diagnostics.get(MemoryDiagnosticsPlugin::PHYSICAL_MEM));
        let virtual_mem = unwrap_diagnostic(diagnostics.get(MemoryDiagnosticsPlugin::VIRTUAL_MEM));
        mem_display.value = format!(
            "{:.0} MB / {:.0} MB",
            physical_mem / BYTES_IN_MB,
            virtual_mem / BYTES_IN_MB
        );
    }
}
