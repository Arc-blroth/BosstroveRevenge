#![doc = include_str!("../../README.md")]
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]
#![feature(decl_macro)]

extern crate core;
#[cfg(all(not(debug_assertions), feature = "bevy_dyn"))]
compile_error!("Bevy should not be dynamically linked for release builds!");

use bevy::app::App;
use bevy::diagnostic::FrameTimeDiagnosticsPlugin;
use bevy::prelude::{ClearColor, Color, Commands, PerspectiveCameraBundle, UiCameraBundle};
use bevy::window::WindowDescriptor;
use bevy::{log, DefaultPlugins};

use crate::debug::fps_mem_display::FPSAndMemoryDisplayPlugin;
use crate::debug::memory_diagnostics::MemoryDiagnosticsPlugin;
use crate::load::LoadingPlugin;
use crate::ui::styles::UIStyleInitPlugin;

pub mod debug;
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
        .insert_resource(ClearColor(Color::rgba(0.0, 0.0, 0.0, 1.0)))
        .add_plugins(DefaultPlugins)
        .add_plugin(FrameTimeDiagnosticsPlugin::default())
        .add_plugin(MemoryDiagnosticsPlugin)
        .add_plugin(FPSAndMemoryDisplayPlugin)
        .add_plugin(UIStyleInitPlugin)
        .add_plugin(LoadingPlugin)
        .add_startup_system(setup_cameras)
        .run();
}

fn setup_cameras(mut commands: Commands) {
    commands.spawn_bundle(PerspectiveCameraBundle::new_3d());
    commands.spawn_bundle(UiCameraBundle::default());
}
