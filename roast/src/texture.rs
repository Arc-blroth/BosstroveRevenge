//! Native implementation of the `ai.arcblroth.roast.RoastTexture` methods
//! and the default texture generator.

use image::{DynamicImage, GenericImage, Rgba};

use crate::backend;
use crate::error::{catch_panic, ForeignRoastResult};
use crate::renderer::texture::TextureSampling;
use crate::renderer::TextureId;

/// A function that constructs a Java Random object given an initial seed
/// and returns a function that calls `nextDouble` on that object.
pub type JavaRandomInterface = extern "C" fn(i64) -> extern "C" fn() -> f64;

/// Generates the default texture from Bosstrove's Revenge Try 1.
/// See [StaticDefaults.java][1] for the original implementation.
///
/// [1]: https://github.com/Arc-blroth/BosstroveRevenge/blob/try1/core/src/main/java/ai/arcblroth/boss/util/StaticDefaults.java#L28
pub fn generate_default_texture(random_builder: JavaRandomInterface) -> DynamicImage {
    let size: u32 = 16;
    let actual_size: u32 = 8;

    let next_double = random_builder(size as i64);

    let mut image = DynamicImage::new_rgb8(actual_size, actual_size);
    for y in 0..actual_size {
        for x in 0..size {
            let random_double = next_double();
            if x < actual_size {
                let color = egui::color::rgb_from_hsv(((random_double / 5.0 + 0.5) as f32, 0.5, 0.8));
                let color = color.map(|c| (c * 255.0).round() as u8);
                image.put_pixel(x, y, Rgba([color[0], color[1], color[2], 255]));
            }
        }
    }
    image
}

const TEXTURE_NOT_FOUND_MSG: &str = "Texture pointer does not point to a valid texture";

macro_rules! texture_getters {
    ($($name:ident: $return_ty:ident = $getter:ident),*$(,)?) => {
        $(
            #[no_mangle]
            pub extern "C" fn $name(this: TextureId) -> ForeignRoastResult<$return_ty> {
                catch_panic(move || {
                    backend::with_renderer(move |renderer| {
                        Ok(renderer.textures.get(&this).expect(TEXTURE_NOT_FOUND_MSG).$getter())
                    })
                })
            }
        )*
    }
}

texture_getters! {
    roast_texture_get_width: u32 = width,
    roast_texture_get_height: u32 = height,
    roast_texture_get_texture_sampling: TextureSampling = sampling,
    roast_texture_get_mipmapped: bool = mipmapped,
}
