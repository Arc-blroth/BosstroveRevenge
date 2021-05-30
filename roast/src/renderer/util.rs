use std::sync::Arc;

use vulkano::device::DeviceExtensions;
use vulkano::instance::{layers_list, InstanceExtensions, PhysicalDevice};

use crate::renderer;
use crate::renderer::types::{QueueFamilyIndices, WindowSurface};
use crate::renderer::{ENABLE_DEBUG_UTILS, VALIDATION_LAYERS};

pub fn check_validation_layer_support() -> bool {
    let layers: Vec<_> = layers_list().unwrap().map(|l| l.name().to_owned()).collect();
    VALIDATION_LAYERS
        .iter()
        .all(|layer_name| layers.contains(&layer_name.to_string()))
}

pub fn get_required_extensions() -> InstanceExtensions {
    let mut extensions = vulkano_win::required_extensions();
    if ENABLE_DEBUG_UTILS {
        extensions.ext_debug_utils = true;
    }
    extensions
}

pub fn is_physical_device_suitable(device: &PhysicalDevice, surface: &Arc<WindowSurface>) -> bool {
    let indices: QueueFamilyIndices = find_queue_families(&device, &surface);
    let extensions_supported = {
        let available_extensions = DeviceExtensions::supported_by_device(*device);
        let device_extensions = renderer::get_device_extensions();
        available_extensions.intersection(&device_extensions) == device_extensions
    };
    let swap_chain_adequate = if extensions_supported {
        let capabilities = surface
            .capabilities(*device)
            .expect("Failed to get window capabilities");
        !capabilities.supported_formats.is_empty() && capabilities.present_modes.iter().next().is_some()
    } else {
        false
    };
    let device_features = renderer::get_device_features();
    indices.is_complete()
        && extensions_supported
        && swap_chain_adequate
        && device.supported_features().intersection(&device_features) == device_features
}

pub fn find_queue_families(device: &PhysicalDevice, surface: &Arc<WindowSurface>) -> QueueFamilyIndices {
    let mut indices = QueueFamilyIndices::default();
    for (i, family) in device.queue_families().enumerate() {
        let i = Some(i as i32);
        if family.supports_graphics() {
            indices.graphics = i;
        }
        if surface.is_supported(family).unwrap() {
            indices.present = i;
        }
        if i != indices.graphics && i != indices.present {
            indices.transfer = i;
        }
        if indices.is_complete() {
            break;
        }
    }
    if indices.transfer.is_none() {
        indices.transfer = indices.graphics.clone();
    }
    indices
}
