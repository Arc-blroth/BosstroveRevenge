use std::any::TypeId;
use std::fmt::Debug;

use bevy::app::Plugin;
use bevy::ecs::event::EventReader;
use bevy::ecs::system::Resource;
use bevy::log::warn;
use bevy::prelude::{App, Commands, Res, ResMut, State, SystemSet};

/// Main enum encompassing all of Bosstroves' possible game states.
#[derive(Copy, Clone, PartialEq, Eq, Debug, Hash)]
pub enum GameState {
    /// The initial game state used for the logo screen.
    Loading,
    /// Game state used for full screen UIs, such as the title screen or settings screen.
    Ui,
    /// The main game state used whenever the overworld is playable.
    Active,
    /// Game state used for pausing in the overworld.
    Paused,
    /// Game state used when a transition animation is fully showing.
    Transitioning,
}

/// Additional states used for transitions.
#[derive(Copy, Clone, PartialEq, Eq, Debug, Hash)]
pub enum TransitionState {
    /// State for when no transitions are running.
    None,
    /// Initial transition state used while the previous [`GameState`] is still active.
    Start,
    /// "Middle" transition state used while the next [`GameState`] is being loaded.
    /// If a transition doesn't need loading, this state will be skipped.
    Load,
    /// Final transition state used as the next [`GameState`] is activated and revealed.
    End,
}

/// Union trait for transition resources.
pub trait TransitionResource: Resource + Debug {
    fn resource_id(&self) -> TypeId;
}
impl<T: Resource + Debug> TransitionResource for T {
    fn resource_id(&self) -> TypeId {
        TypeId::of::<T>()
    }
}

/// Metadata on the currently running transition (if any).
/// **This should be specified as an `Option<Res<Transition>>` if a transition might not be running**,
/// ie for systems that can be invoked while `TransitionState` is `None`.
#[derive(Debug)]
pub struct Transition {
    /// A marker (likely an empty struct) for the current transition type.
    pub ty: &'static dyn TransitionResource,
    /// Optional marker for what type of loading is running during this transition.
    /// If this is `None`, the transition state will automatically be set to `End` after reaching `Load`.
    pub load_ty: Option<&'static dyn TransitionResource>,
    /// Game state to set once the current transition begins ending.
    pub next_state: GameState,
}

/// Events fired during the transition lifecycle.
#[derive(Debug)]
pub enum TransitionEvent {
    /// Starts a new transition. If a transition is already running, this will override the old transition.
    Start(Transition),
}

/// The plugin that ties the transition system together.
pub struct TransitionSupportPlugin;

impl Plugin for TransitionSupportPlugin {
    fn build(&self, app: &mut App) {
        app.add_state(TransitionState::None)
            .add_event::<TransitionEvent>()
            .add_system(pump_transition_events)
            .add_system_set(SystemSet::on_enter(TransitionState::None).with_system(on_transition_none))
            .add_system_set(SystemSet::on_enter(TransitionState::Load).with_system(on_transition_load))
            .add_system_set(SystemSet::on_enter(TransitionState::End).with_system(on_transition_end));
    }
}

fn pump_transition_events(
    mut state: ResMut<State<TransitionState>>,
    mut transition_events: EventReader<TransitionEvent>,
    current_transition: Option<Res<Transition>>,
    mut commands: Commands,
) {
    if let Some(event) = transition_events.iter().last() {
        match event {
            TransitionEvent::Start(transition) => {
                if let Some(current) = &current_transition {
                    warn!(
                        "Overriding running transition {:?} with new transition {:?}!",
                        *current, transition
                    );
                } else {
                    debug_assert_eq!(*state.current(), TransitionState::None);
                }
                let new_transition = Transition {
                    ty: transition.ty,
                    load_ty: transition.load_ty,
                    next_state: transition.next_state,
                };
                commands.insert_resource(new_transition);
                if *state.current() != TransitionState::Start {
                    state.replace(TransitionState::Start).unwrap();
                }
            }
        }
    }
}

fn on_transition_none(mut commands: Commands) {
    commands.remove_resource::<Transition>();
}

fn on_transition_load(
    mut game_state: ResMut<State<GameState>>,
    mut transition_state: ResMut<State<TransitionState>>,
    transition: Res<Transition>,
) {
    game_state.replace(GameState::Transitioning).unwrap();
    if transition.load_ty.is_none() {
        // Jump to `TransitionState::End` on the same frame.
        // Game state will be updated accordingly in the `on_transition_end` system below.
        transition_state.replace(TransitionState::End).unwrap();
    }
}

fn on_transition_end(mut state: ResMut<State<GameState>>, transition: Res<Transition>) {
    // overwrite_replace since we might have also just queued a `GameState::Transitioning`
    state.overwrite_replace(transition.next_state).unwrap();
}
