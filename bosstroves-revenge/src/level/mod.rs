//! The overworld logic for Bosstroves' Revenge.
//!
//! Note: the name "level" is somewhat of a misnomer - Bosstroves' Revenge does not contain
//! discreet levels, and the map dynamically loads based on the player's position. The name
//! is a historical quirk and comes from the fact that `try1` was intended to have discreet
//! levels, though level switching was never implemented before it was abandoned.

use bevy::app::Plugin;

use crate::App;

pub mod load;

/// Super-plugin for all level related functionality.
pub struct LevelPlugin;

impl Plugin for LevelPlugin {
    fn build(&self, app: &mut App) {
        load::build(app);
    }
}
