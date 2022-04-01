use bevy::input::Input;
use bevy::prelude::{KeyCode, Res, ResMut};
use bevy::window::{WindowMode, Windows};

pub mod fps_mem_display;
pub mod memory_diagnostics;

/// Toggles fullscreen mode when F11 is pressed.
pub fn fullscreen_toggle(mut windows: ResMut<Windows>, key_inputs: Res<Input<KeyCode>>) {
    if let Some(primary) = windows.get_primary_mut() {
        if key_inputs.just_pressed(KeyCode::F11) {
            match primary.mode() {
                WindowMode::Windowed => primary.set_mode(WindowMode::BorderlessFullscreen),
                _ => primary.set_mode(WindowMode::Windowed),
            }
        }
    }
}
