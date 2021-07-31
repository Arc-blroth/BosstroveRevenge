use std::sync::Arc;

use image::{DynamicImage, GenericImageView};
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
    width: u32,
    height: u32,
    image: Arc<ImageWithView<ImmutableImage>>,
    sampling: TextureSampling,
    mipmapped: bool,
}

impl Texture {
    /// Creates and uploads a texture.
    pub fn new(vulkan: &VulkanWrapper, image: DynamicImage, sampling: TextureSampling, mipmapped: bool) -> Self {
        Texture {
            width: image.width(),
            height: image.height(),
            image: vulkan.create_texture_image(image, mipmapped),
            sampling,
            mipmapped,
        }
    }

    /// The internal image object of this texture.
    pub fn image(&self) -> &Arc<ImageWithView<ImmutableImage>> {
        &self.image
    }

    /// The width of this texture.
    pub fn width(&self) -> u32 {
        self.width
    }

    /// The height of this texture.
    pub fn height(&self) -> u32 {
        self.height
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
