use bevy::app::{App, Plugin};

use crate::ui::logo::LogoScreenPlugin;

/// Loads assets during the initial loading state of the game
/// and sets the game state to the title screen when done.
///
/// Ported from try2's
/// [`LoadEngine`](https://github.com/Arc-blroth/BosstroveRevenge/blob/6fa583e5af7bf4676c04feabc6dec8fa5d0e05fc/core/src/main/kotlin/ai/arcblroth/boss/load/LoadEngine.kt).
pub struct LoadingPlugin;

impl Plugin for LoadingPlugin {
    fn build(&self, app: &mut App) {
        app.add_plugin(LogoScreenPlugin);
    }
}
