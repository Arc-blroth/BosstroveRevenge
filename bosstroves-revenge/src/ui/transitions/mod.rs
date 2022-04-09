//! Various transition effects used throughout Bosstroves' Revenge.

use std::any::TypeId;

use bevy::app::Plugin;
use bevy::ecs::schedule::ShouldRun;
use bevy::prelude::{App, Res};

use crate::state::{Transition, TransitionResource};

mod fade;
pub use fade::{FadeTransition, FadeType};

/// Handler code for Bosstroves' transition effect library.
pub struct TransitionLibraryPlugin;

impl Plugin for TransitionLibraryPlugin {
    fn build(&self, app: &mut App) {
        fade::build(app);
    }
}

/// Run criteria for transition handler systems.
fn is_transition_type<T: TransitionResource>(maybe_transition: Option<Res<Transition>>) -> ShouldRun {
    match maybe_transition {
        Some(transition) if (transition.ty).resource_id() == TypeId::of::<T>() => ShouldRun::Yes,
        _ => ShouldRun::No,
    }
}
