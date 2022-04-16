use bevy::app::{App, Plugin};
use bevy::ecs::event::EventWriter;
use bevy::prelude::{Color, Commands, Local, SystemSet};
use bevy_vox_mesh::VoxMeshPlugin;

use crate::level::load::InitLevel;
use crate::state::{GameState, Transition, TransitionEvent};
use crate::ui::logo::LogoScreenPlugin;
use crate::ui::transitions::{FadeTransition, FadeType};

/// Loads assets during the initial loading state of the game
/// and sets the game state to the title screen when done.
///
/// Ported from try2's
/// [`LoadEngine`](https://github.com/Arc-blroth/BosstroveRevenge/blob/6fa583e5af7bf4676c04feabc6dec8fa5d0e05fc/core/src/main/kotlin/ai/arcblroth/boss/load/LoadEngine.kt).
pub struct LoadingPlugin;

impl Plugin for LoadingPlugin {
    fn build(&self, app: &mut App) {
        app.add_plugin(LogoScreenPlugin)
            .add_plugin(VoxMeshPlugin::default())
            .add_system_set(SystemSet::on_update(GameState::Loading).with_system(test_finish_load));
    }
}

fn test_finish_load(
    mut commands: Commands,
    mut events: EventWriter<TransitionEvent>,
    mut sent_transition: Local<bool>,
) {
    if !*sent_transition {
        *sent_transition = true;
        FadeTransition::configure(&mut commands, FadeType::FadeIn, 1.0 / (0.04 * 30.0), Color::BLACK);
        events.send(TransitionEvent::Start(Transition {
            ty: &FadeTransition,
            load_ty: Some(&InitLevel),
            next_state: GameState::Active,
        }));
    }
}
