use std::sync::Arc;

use image::{DynamicImage, GenericImage, GenericImageView, Rgba};
use jni::objects::JValue;
use jni::signature::{JavaType, Primitive};
use jni::sys::jlong;
use jni::JNIEnv;
use vulkano::format::Format;
use vulkano::image::view::ImageView;
use vulkano::image::{ImageDimensions, ImmutableImage, MipmapsCount};
use vulkano::sync::GpuFuture;

use crate::renderer::types::ImageWithView;
use crate::renderer::vulkan::VulkanWrapper;

impl VulkanWrapper {
    /// Creates a 32-bit RGBA texture image.
    pub fn create_texture_image(
        &self,
        image: DynamicImage,
        generate_mipmaps: bool,
    ) -> Arc<ImageWithView<ImmutableImage>> {
        let (texture, command) = ImmutableImage::from_iter(
            image.to_rgba8().into_raw().into_iter(),
            ImageDimensions::Dim2d {
                width: image.width(),
                height: image.height(),
                array_layers: 1,
            },
            if generate_mipmaps {
                MipmapsCount::Log2
            } else {
                MipmapsCount::One
            },
            Format::R8G8B8A8Unorm,
            if self.queues.transfer.family().supports_graphics() {
                self.queues.transfer.clone()
            } else {
                self.queues.graphics.clone()
            },
        )
        .unwrap();
        command.then_signal_fence_and_flush().unwrap().wait(None).unwrap();

        ImageView::new(texture).unwrap()
    }
}

/// Sampling method for a texture.
#[repr(u8)]
#[derive(Copy, Clone, Eq, PartialEq, Debug)]
pub enum TextureSampling {
    /// A linear sampling method that
    /// interpolates nearby pixels.
    Smooth,
    /// A nearest sampling method that
    /// uses the nearest pixel.
    Pixel,
}

impl Default for TextureSampling {
    fn default() -> Self {
        Self::Smooth
    }
}

/// A sampled and possibly mipmapped image that
/// can be bound to a descriptor set and used
/// alongside a mesh.
#[derive(Clone)]
pub struct Texture {
    image: Arc<ImageWithView<ImmutableImage>>,
    sampling: TextureSampling,
    mipmapped: bool,
}

impl Texture {
    /// Creates and uploads a texture.
    pub fn new(vulkan: &VulkanWrapper, image: DynamicImage, sampling: TextureSampling, mipmapped: bool) -> Self {
        Texture {
            image: vulkan.create_texture_image(image, mipmapped),
            sampling,
            mipmapped,
        }
    }

    /// The internal image object of this texture.
    pub fn image(&self) -> &Arc<ImageWithView<ImmutableImage>> {
        &self.image
    }

    /// The sampling method for this texture.
    pub fn sampling(&self) -> TextureSampling {
        self.sampling
    }

    /// Whether this texture has mipmaps.
    pub fn mipmapped(&self) -> bool {
        self.mipmapped
    }
}

impl PartialEq for Texture {
    fn eq(&self, other: &Self) -> bool {
        self.sampling == other.sampling
            && self.mipmapped == other.mipmapped
            && self.image.image() == other.image.image()
    }
}

impl Eq for Texture {}

/// Generates the default texture from Bosstrove's Revenge Try 1.
/// See [StaticDefaults.java][1] for the original implementation.
///
/// [1]: https://github.com/Arc-blroth/BosstroveRevenge/blob/try1/core/src/main/java/ai/arcblroth/boss/util/StaticDefaults.java#L28
pub fn generate_default_texture(env: JNIEnv) -> DynamicImage {
    let size: u32 = 16;
    let actual_size: u32 = 8;

    let random = env
        .new_object("java/util/Random", "(J)V", &[JValue::Long(size as jlong)])
        .unwrap();
    let random_class = env.get_object_class(random).unwrap();
    let next_double_method = env.get_method_id(random_class, "nextDouble", "()D").unwrap();
    let next_double = move || {
        env.call_method_unchecked(random, next_double_method, JavaType::Primitive(Primitive::Double), &[])
            .unwrap()
            .d()
            .unwrap()
    };

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
