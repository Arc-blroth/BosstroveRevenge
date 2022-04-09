use std::time::Duration;

use bevy::core::Time;
use bevy::ecs::schedule::ShouldRun;
use bevy::math::Size;
use bevy::prelude::{
    App, BuildChildren, Color, Commands, Component, DespawnRecursiveExt, Entity, NodeBundle, Query, Res, ResMut, State,
    SystemLabel, SystemSet, With,
};
use bevy::ui::{PositionType, Style, UiColor, Val};
use bevy_zhack::{ZHackRootBundle, ZIndex};

use super::is_transition_type;
use crate::state::{Transition, TransitionState};

#[derive(Clone, PartialEq, Eq, Hash, Debug, SystemLabel)]
struct Setup;

pub(super) fn build(app: &mut App) {
    app.add_system_set(
        SystemSet::on_enter(TransitionState::Start)
            .with_system(setup)
            .label(Setup),
    );
    app.add_system_set(
        SystemSet::on_enter(TransitionState::Start)
            .with_system(update_fade_start_time)
            .label(Setup),
    );
    app.add_system_set(
        SystemSet::on_enter(TransitionState::End)
            .with_system(update_fade_start_time)
            .label(Setup),
    );
    app.add_system_set(
        SystemSet::new()
            .with_run_criteria(is_transition_type::<FadeTransition>)
            .with_system(handle_fade_transition)
            .after(Setup),
    );
}

/// A transition that linearly fades to a color over time.
#[derive(Debug)]
pub struct FadeTransition;

pub enum FadeType {
    FadeIn,
    FadeOut,
    FadeInOut,
}

struct FadeTransitionParams {
    ty: FadeType,
    start_time: Duration,
    fade_duration: f64,
    color: Color,
}

#[derive(Component)]
struct FadeTransitionOverlay;

impl FadeTransition {
    /// Configures a new fade transition to the specified color that will start
    /// from the next frame and last for `fade_duration` seconds.
    pub fn configure(commands: &mut Commands, ty: FadeType, fade_duration: f64, color: Color) {
        assert!(fade_duration >= 0.0, "Fade transition duration cannot be negative!");

        commands.insert_resource(FadeTransitionParams {
            ty,
            start_time: Duration::default(), // will be actually set on next frame
            fade_duration,
            color,
        });
    }
}

fn setup(maybe_transition: Option<Res<Transition>>, mut commands: Commands) {
    // TODO replace with actual run criteria once #2446 gets merged
    if is_transition_type::<FadeTransition>(maybe_transition) == ShouldRun::Yes {
        commands
            .spawn_bundle(ZHackRootBundle::default())
            .with_children(|builder| {
                builder
                    .spawn_bundle(NodeBundle {
                        color: Color::NONE.into(),
                        style: Style {
                            position_type: PositionType::Absolute,
                            size: Size::new(Val::Percent(100.0), Val::Percent(100.0)),
                            ..Style::default()
                        },
                        ..NodeBundle::default()
                    })
                    .insert(ZIndex(900.0))
                    .insert(FadeTransitionOverlay);
            });
    }
}

#[allow(clippy::only_used_in_recursion)] // false positive
fn update_fade_start_time(
    maybe_transition: Option<Res<Transition>>,
    mut params: ResMut<FadeTransitionParams>,
    time: Res<Time>,
) {
    // TODO replace with actual run criteria once #2446 gets merged
    if is_transition_type::<FadeTransition>(maybe_transition) == ShouldRun::Yes {
        params.start_time = time.time_since_startup();
    }
}

fn handle_fade_transition(
    mut state: ResMut<State<TransitionState>>,
    params: Res<FadeTransitionParams>,
    time: Res<Time>,
    mut overlay: Query<(&mut UiColor, Entity), With<FadeTransitionOverlay>>,
    mut commands: Commands,
) {
    let (mut color, entity) = overlay.single_mut();

    // note that we update `start_time` on entering `TransitionState::End`
    let time_passed = (time.time_since_startup() - params.start_time).as_secs_f64();
    let progress = (time_passed / params.fade_duration).clamp(0.0, 1.0);

    match *state.current() {
        TransitionState::Start => {
            match params.ty {
                FadeType::FadeOut => {
                    // skip directly to Load
                }
                _ => {
                    *color = (*params.color.clone().set_a(progress as f32)).into();

                    if progress != 1.0 {
                        return;
                    }
                }
            }
            state.replace(TransitionState::Load).unwrap();
        }
        TransitionState::Load => {}
        TransitionState::End => {
            match params.ty {
                FadeType::FadeIn => {
                    // skip directly to None
                }
                _ => {
                    *color = (*params.color.clone().set_a(progress as f32)).into();

                    if progress != 1.0 {
                        return;
                    }
                }
            }
            // cleanup
            commands.entity(entity).despawn_recursive();
            commands.remove_resource::<FadeTransitionParams>();
            state.replace(TransitionState::None).unwrap();
        }
        _ => unreachable!(),
    }
}
