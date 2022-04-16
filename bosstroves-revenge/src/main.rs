#![doc = include_str!("../../README.md")]
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]
#![feature(decl_macro)]

#[cfg(all(not(debug_assertions), feature = "bevy_dyn"))]
compile_error!("Bevy should not be dynamically linked for release builds!");

use bevy::app::App;
use bevy::diagnostic::FrameTimeDiagnosticsPlugin;
use bevy::prelude::{
    ClearColor, Color, Commands, PerspectiveCameraBundle, PerspectiveProjection, Transform, UiCameraBundle,
};
use bevy::window::WindowDescriptor;
use bevy::{log, DefaultPlugins};
use bevy_flycam::{FlyCam, MovementSettings, NoCameraPlayerPlugin};
use bevy_zhack::ZHackPlugin;

use crate::debug::fps_mem_display::FPSAndMemoryDisplayPlugin;
use crate::debug::memory_diagnostics::MemoryDiagnosticsPlugin;
use crate::level::LevelPlugin;
use crate::load::LoadingPlugin;
use crate::state::{GameState, TransitionSupportPlugin};
use crate::ui::styles::UIStyleInitPlugin;
use crate::ui::transitions::TransitionLibraryPlugin;

pub mod debug;
pub mod level;
pub mod load;
pub mod state;
pub mod ui;
pub mod util;

fn main() {
    log::info!("Hello Bosstrove!");

    App::new()
        .insert_resource(WindowDescriptor {
            title: concat!("Bosstrove's Revenge ", env!("CARGO_PKG_VERSION")).to_string(),
            ..Default::default()
        })
        .insert_resource(MovementSettings {
            sensitivity: 0.0002,
            speed: 80.0,
        })
        .insert_resource(ClearColor(Color::rgba(0.0, 0.0, 0.0, 1.0)))
        .add_plugins(DefaultPlugins)
        .add_plugin(FrameTimeDiagnosticsPlugin::default())
        .add_plugin(MemoryDiagnosticsPlugin)
        .add_plugin(FPSAndMemoryDisplayPlugin)
        .add_plugin(UIStyleInitPlugin)
        .add_plugin(ZHackPlugin)
        .add_plugin(TransitionSupportPlugin)
        .add_plugin(TransitionLibraryPlugin)
        .add_plugin(LoadingPlugin)
        .add_plugin(LevelPlugin)
        .add_plugin(NoCameraPlayerPlugin)
        .add_state(GameState::Loading)
        .add_startup_system(setup_cameras)
        .add_system(debug::fullscreen_toggle)
        .run();
}

fn setup_cameras(mut commands: Commands) {
    commands
        .spawn_bundle(PerspectiveCameraBundle {
            perspective_projection: PerspectiveProjection {
                near: 0.1,
                ..PerspectiveProjection::default()
            },
            transform: Transform::from_xyz(0.0, 2.0, 80.0),
            ..PerspectiveCameraBundle::default()
        })
        .insert(FlyCam);
    commands.spawn_bundle(UiCameraBundle::default());
}
