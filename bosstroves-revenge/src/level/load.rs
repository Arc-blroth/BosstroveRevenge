use std::any::TypeId;
use std::time::Instant;

use bevy::app::App;
use bevy::asset::{AssetServer, Handle, LoadState};
use bevy::ecs::schedule::ShouldRun;
use bevy::log;
use bevy::math::{Quat, Vec3};
use bevy::pbr::{DirectionalLight, DirectionalLightBundle};
use bevy::prelude::{Commands, OrthographicProjection, Res, ResMut, State, SystemLabel, SystemSet, Transform};
use bevy::scene::{Scene, SpawnSceneCommands};
use humantime::format_duration;

use crate::state::{Transition, TransitionState};
use crate::Color;

#[derive(Clone, PartialEq, Eq, Hash, Debug, SystemLabel)]
struct Setup;

pub(super) fn build(app: &mut App) {
    app.add_system_set(
        SystemSet::on_enter(TransitionState::Load)
            .with_system(setup)
            .label(Setup),
    );
    app.add_system_set(
        SystemSet::on_update(TransitionState::Load)
            .with_system(wait_for_load)
            .after(Setup),
    );
}

/// Initial loader for the terrain and entities around the player when they first load from a save.
#[derive(Debug)]
pub struct InitLevel;

/// A struct to keep track of all loading `Handle`s.
struct InitLevelStorage {
    /// Use an `Instant` rather than Bevy's `Time` since we don't want frame time
    start_time: Instant,
    terrain: Vec<Handle<Scene>>,
}

/// Run criteria for init level loading
fn is_init_level(maybe_transition: Option<Res<Transition>>) -> ShouldRun {
    match maybe_transition {
        Some(transition) => match transition.load_ty {
            Some(ty) if ty.resource_id() == TypeId::of::<InitLevel>() => ShouldRun::Yes,
            _ => ShouldRun::No,
        },
        _ => ShouldRun::No,
    }
}

fn setup(maybe_transition: Option<Res<Transition>>, mut commands: Commands, asset_server: Res<AssetServer>) {
    // TODO replace with actual run criteria once #2446 gets merged
    if is_init_level(maybe_transition) == ShouldRun::Yes {
        log::info!("Loading level...");

        commands.insert_resource(InitLevelStorage {
            start_time: Instant::now(),
            terrain: vec![asset_server.load("level/test/w0l1-debugging-seas-reloaded.vox")],
        });
    }
}

fn wait_for_load(
    maybe_transition: Option<Res<Transition>>,
    mut commands: Commands,
    asset_server: Res<AssetServer>,
    mut storage: ResMut<InitLevelStorage>,
    mut state: ResMut<State<TransitionState>>,
) {
    // TODO replace with actual run criteria once #2446 gets merged
    if is_init_level(maybe_transition) != ShouldRun::Yes {
        return;
    }

    let handles = storage.terrain.iter().map(|x| x.id);
    match asset_server.get_group_load_state(handles) {
        LoadState::Loaded => {
            // spawn everything
            let shadow_size = 100.0;
            let shadow_projection = OrthographicProjection {
                left: -shadow_size,
                right: shadow_size,
                bottom: -shadow_size,
                top: shadow_size,
                near: -shadow_size,
                far: shadow_size,
                ..OrthographicProjection::default()
            };

            commands.spawn_bundle(DirectionalLightBundle {
                directional_light: DirectionalLight {
                    color: Color::WHITE,
                    illuminance: 50000.0,
                    shadows_enabled: true,
                    shadow_projection,
                    shadow_depth_bias: DirectionalLight::DEFAULT_SHADOW_DEPTH_BIAS,
                    shadow_normal_bias: DirectionalLight::DEFAULT_SHADOW_NORMAL_BIAS,
                },
                transform: Transform {
                    translation: Vec3::new(0.0, shadow_size / 2.0, 0.0),
                    rotation: Quat::from_rotation_x(-std::f32::consts::FRAC_PI_4),
                    ..Transform::default()
                },
                ..DirectionalLightBundle::default()
            });

            for scene in storage.terrain.drain(..) {
                commands.spawn_scene(scene);
            }

            // cleanup
            commands.remove_resource::<InitLevelStorage>();

            // finish loading
            log::info!(
                "Finished loading level in {}!",
                format_duration(Instant::now() - storage.start_time)
            );
            state.replace(TransitionState::End).unwrap();
        }
        LoadState::Failed => {
            // TODO show user a screen instead of crashing
            panic!("Couldn't load all required assets!");
        }
        _ => {}
    }
}
