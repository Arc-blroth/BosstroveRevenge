#![doc = include_str!("../README.md")]

use bevy::app::{App, CoreStage, Plugin, StartupStage};
use bevy::math::{Size, Vec2};
use bevy::prelude::{
    Bundle, Changed, Children, Component, Entity, GlobalTransform, ParallelSystemDescriptorCoercion, Parent, Query,
    Res, Transform, With, Without,
};
use bevy::transform::TransformSystem;
use bevy::ui::{Node, PositionType, Style, Val};
use bevy::window::Windows;

// TODO yeet once bikeshedding settles
// for now, component order is determined by child depth
// see https://github.com/bevyengine/bevy/issues/1275
/// A hacky override to manually set the z-index of UI nodes.
///
/// Add any UI nodes you want to manually set the z-index of as a child of an entity with this bundle.
#[derive(Clone, Debug, Bundle)]
pub struct ZHackRootBundle {
    marker: ZHackRootBundleMarker,
    node: Node,
    style: Style,
    dummy_global_transform: GlobalTransform,
}

impl Default for ZHackRootBundle {
    fn default() -> Self {
        Self {
            marker: ZHackRootBundleMarker,
            node: Node::default(),
            style: Style {
                position_type: PositionType::Absolute,
                size: Size::new(Val::Percent(100.0), Val::Percent(100.0)),
                ..Style::default()
            },
            dummy_global_transform: GlobalTransform::default(),
        }
    }
}

/// Marker for the z-index hack bundle.
#[derive(Copy, Clone, Eq, PartialEq, Default, Debug, Component)]
struct ZHackRootBundleMarker;

/// Add this component to each UI node that you want to override the z-index of.
#[derive(Copy, Clone, PartialEq, Default, Debug, Component)]
pub struct ZIndex(pub f32);

/// Support systems for the z-index hack.
pub struct ZHackPlugin;

impl Plugin for ZHackPlugin {
    fn build(&self, app: &mut App) {
        app.add_startup_system_to_stage(
            StartupStage::PostStartup,
            zhack_copy_transform
                .label(TransformSystem::TransformPropagate)
                .after(TransformSystem::ParentUpdate),
        );
        app.add_system_to_stage(
            CoreStage::PostUpdate,
            zhack_copy_transform
                .label(TransformSystem::TransformPropagate)
                .after(TransformSystem::ParentUpdate),
        );
    }
}

fn zhack_copy_transform(
    windows: Res<Windows>,
    mut root_query: Query<Option<&Children>, (With<ZHackRootBundleMarker>, Without<Parent>)>,
    mut transform_query: Query<(&Transform, &mut GlobalTransform, Option<&ZIndex>), With<Parent>>,
    transform_changed_query: Query<Entity, Changed<Transform>>,
    z_changed_query: Query<Entity, Changed<ZIndex>>,
    children_query: Query<Option<&Children>, (With<Parent>, With<GlobalTransform>)>,
) {
    // replicate the default transform set in `bevy_ui::flex::flex_node_system`
    let global_transform = if let Some(size) = primary_window_size(windows) {
        GlobalTransform {
            translation: (size / 2.0).extend(0.0),
            ..GlobalTransform::default()
        }
    } else {
        GlobalTransform::default()
    };

    for children in root_query.iter_mut().flatten() {
        for child in children.iter() {
            zhack_propagate_transform(
                &global_transform,
                *child,
                false,
                &mut transform_query,
                &transform_changed_query,
                &z_changed_query,
                &children_query,
            );
        }
    }
}

fn zhack_propagate_transform(
    parent: &GlobalTransform,
    child: Entity,
    mut changed: bool,
    transform_query: &mut Query<(&Transform, &mut GlobalTransform, Option<&ZIndex>), With<Parent>>,
    transform_changed_query: &Query<Entity, Changed<Transform>>,
    z_changed_query: &Query<Entity, Changed<ZIndex>>,
    children_query: &Query<Option<&Children>, (With<Parent>, With<GlobalTransform>)>,
) {
    changed |= transform_changed_query.get(child).is_ok() || z_changed_query.get(child).is_ok();

    let global_transform = if let Ok((transform, mut global_transform, z)) = transform_query.get_mut(child) {
        if changed {
            *global_transform = parent.mul_transform(*transform);
            if let Some(&ZIndex(z)) = z {
                (*global_transform.translation).z = z;
            }
        }
        *global_transform
    } else {
        return;
    };

    if let Ok(Some(sub_children)) = children_query.get(child) {
        for sub_child in sub_children.iter() {
            zhack_propagate_transform(
                &global_transform,
                *sub_child,
                changed,
                transform_query,
                transform_changed_query,
                z_changed_query,
                children_query,
            );
        }
    }
}

/// Gets the size of the primary window in logical pixels.
///
/// # Panics
/// If there is no primary window.
fn primary_window_size(windows: Res<Windows>) -> Option<Vec2> {
    let window = windows.get_primary()?;
    Some(Vec2::new(window.width(), window.height()))
}
