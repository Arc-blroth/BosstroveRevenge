use bevy::app::App;
use bevy::asset::AssetServer;
use bevy::prelude::{Color, Handle, Plugin};
use bevy::text::{Font, TextStyle};

/// Default styling info for BosstroveRevenge's
/// in-game UI. This is inserted as a resource
/// into Bevy's world and is initialized with
/// the [`UIStyleInitPlugin`].
#[derive(Default)]
pub struct UIStyles {
    mono_font: Handle<Font>,
}

macro declare_text_sizes($($name:ident => $size:literal),*$(,)?) {
    $(
        #[allow(unused)]
        pub fn $name(&self, color: Color) -> TextStyle {
            TextStyle {
                font: self.mono_font.clone(),
                font_size: $size,
                color,
            }
        }
    )*
}

impl UIStyles {
    fn new(asset_server: &AssetServer) -> Self {
        Self {
            mono_font: asset_server.load("generated/fonts/CascadiaMono-Light.ttf"),
        }
    }

    declare_text_sizes!(
        text_small => 8.0,
        text => 16.0,
        text_heading => 16.0,
    );
}

/// Plugin to initialize the global [`UIStyles`] resource.
pub struct UIStyleInitPlugin;

impl Plugin for UIStyleInitPlugin {
    fn build(&self, app: &mut App) {
        let asset_server = app.world.get_resource::<AssetServer>().unwrap();
        let styles = UIStyles::new(asset_server);
        app.insert_resource(styles);
    }
}
