//! JNI implementation of the `ai.arcblroth.roast.RoastUI`
//! and `ai.arcblroth.roast.RoastArea` native methods.

use std::ffi::c_void;

use anyhow::{anyhow, Result};
use egui::{CentralPanel, Color32, Frame, Label, Layout, Pos2 as EPos2, TextStyle, Ui, Vec2 as EVec2, Window};

use crate::backend::with_renderer;
use crate::check_backend;
use crate::error::{catch_panic, ForeignRoastResult, Nothing, RoastError};
use crate::ffi::util::{is_aligned_and_not_null, string_from_foreign, ForeignOption};

type Contents = extern "C" fn(*mut c_void) -> ForeignRoastResult<Nothing>;

/// Rust version of `ai.arcblroth.boss.backend.ui.Bounds`.
#[repr(C)]
pub struct Bounds {
    pub x: f32,
    pub y: f32,
    pub w: f32,
    pub h: f32,
}

#[repr(C)]
pub enum ForeignTextStyle {
    Small,
    Body,
    Button,
    Heading,
    Monospace,
}

impl From<ForeignTextStyle> for TextStyle {
    fn from(foreign: ForeignTextStyle) -> Self {
        match foreign {
            ForeignTextStyle::Small => TextStyle::Small,
            ForeignTextStyle::Body => TextStyle::Body,
            ForeignTextStyle::Button => TextStyle::Button,
            ForeignTextStyle::Heading => TextStyle::Heading,
            ForeignTextStyle::Monospace => TextStyle::Monospace,
        }
    }
}

fn int_to_color(rgba: u32) -> Color32 {
    Color32::from_rgba_unmultiplied(
        ((rgba >> 16) & 0xFF) as u8,
        ((rgba >> 8) & 0xFF) as u8,
        (rgba & 0xFF) as u8,
        ((rgba >> 24) & 0xFF) as u8,
    )
}

/// Creates a closure that takes a `&mut Ui` and invokes
/// the provided callback with it.
#[must_use = "Should be immediately passed to an egui UI builder"]
fn add_contents(kt_add_contents: Contents) -> impl FnOnce(&mut Ui) {
    move |ui: &mut Ui| kt_add_contents(ui as *mut Ui as *mut _).panic_if_err()
}

// ===================================
//              RoastUI
// ===================================

/// Checks that this RoastUI instance refers to a valid
/// pointer. This pointer should be equal to the thread
/// local backend pointer.
///
/// # Panics
/// If the RoastUI pointer is not valid.
#[inline]
fn check_roast_ui(this: u64) -> Result<u64> {
    check_backend(this)
}

#[no_mangle]
pub extern "C" fn roast_ui_center_panel(this: u64, kt_add_contents: Contents) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        check_roast_ui(this)?;

        let ctx = with_renderer(|renderer| renderer.gui.context());
        CentralPanel::default().show(&ctx, add_contents(kt_add_contents));
        Ok(Nothing::default())
    })
}

#[no_mangle]
pub extern "C" fn roast_ui_area(
    this: u64,
    name: *const u8,
    name_len: usize,
    bounds: Bounds,
    kt_add_contents: Contents,
) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        check_roast_ui(this)?;

        let name = string_from_foreign(name, name_len)?;

        // Contrary to the name of the method this uses a Window
        // rather than an Area because Areas can have weird layout
        // when attempting to center and justify.
        let ctx = with_renderer(|renderer| renderer.gui.context());
        Window::new(name)
            .fixed_pos(EPos2::new(bounds.x, bounds.y))
            .fixed_size(EVec2::new(bounds.w, bounds.h))
            .title_bar(false)
            .frame(Frame::none())
            .show(&ctx, add_contents(kt_add_contents));
        Ok(Nothing::default())
    })
}

#[no_mangle]
pub extern "C" fn roast_ui_window(
    this: u64,
    name: *const u8,
    name_len: usize,
    resizable: bool,
    kt_add_contents: Contents,
) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        check_roast_ui(this)?;

        let name = string_from_foreign(name, name_len)?;

        let ctx = with_renderer(|renderer| renderer.gui.context());
        Window::new(name)
            .resizable(resizable)
            .show(&ctx, add_contents(kt_add_contents));
        Ok(Nothing::default())
    })
}

// ===================================
//             RoastArea
// ===================================

const UI_NOT_FOUND_MSG: &str = "RoastArea pointer is null or not aligned - this should *never* occur!";

/// Gets the ui pointer from the pointer passed to a RoastArea method.
fn get_ui_pointer<'a>(this: *mut c_void) -> Result<&'a mut Ui> {
    if !is_aligned_and_not_null(this) {
        Err(anyhow!(RoastError::NullPointer(UI_NOT_FOUND_MSG.to_string())))
    } else {
        // SAFETY: the pointer is definitely aligned and non-null.
        // It is up to the caller to make sure that it is actually valid.
        unsafe { Ok((this as *mut Ui).as_mut().unwrap()) }
    }
}

macro_rules! impl_layout_functions {
    ($($java_name:ident => $rust_name:ident),*$(,)?) => {
        $(
            #[no_mangle]
            pub extern "C" fn $java_name(
                this: *mut c_void,
                kt_add_contents: Contents,
            ) -> ForeignRoastResult<Nothing> {
                catch_panic(move || {
                    get_ui_pointer(this)?.$rust_name(add_contents(kt_add_contents));
                    Ok(Nothing::default())
                })
            }
        )*
    }
}

impl_layout_functions! {
    roast_area_horizontal => horizontal,
    roast_area_horizontal_wrapped => horizontal_wrapped,
    roast_area_vertical => vertical,
    roast_area_vertical_centered => vertical_centered,
    roast_area_vertical_centered_justified => vertical_centered_justified,
}

#[no_mangle]
pub extern "C" fn roast_area_horizontal_right(
    this: *mut c_void,
    kt_add_contents: Contents,
) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        let ui = get_ui_pointer(this)?;
        ui.horizontal(|ui| {
            ui.with_layout(Layout::right_to_left(), add_contents(kt_add_contents));
        });
        Ok(Nothing::default())
    })
}

#[no_mangle]
pub extern "C" fn roast_area_label(
    this: *mut c_void,
    text: *const u8,
    text_len: usize,
    wrap: ForeignOption<bool>,
    text_style: ForeignOption<ForeignTextStyle>,
    background_color: u32,
    text_color: ForeignOption<u32>,
    code: bool,
    strong: bool,
    weak: bool,
    strikethrough: bool,
    underline: bool,
    italics: bool,
    raised: bool,
) -> ForeignRoastResult<Nothing> {
    catch_panic(move || {
        let mut label = Label::new(string_from_foreign(text, text_len)?);

        if let ForeignOption::Some(wrap_inner) = wrap {
            label = label.wrap(wrap_inner);
        }
        if let ForeignOption::Some(text_style_inner) = text_style {
            label = label.text_style(text_style_inner.into());
        }
        label = label.background_color(int_to_color(background_color));
        if let ForeignOption::Some(text_color_inner) = text_color {
            label = label.text_color(int_to_color(text_color_inner));
        }
        if code {
            label = label.code();
        }
        if strong {
            label = label.strong();
        }
        if weak {
            label = label.weak();
        }
        if strikethrough {
            label = label.strikethrough();
        }
        if underline {
            label = label.underline();
        }
        if italics {
            label = label.italics();
        }
        if raised {
            label = label.raised();
        }

        get_ui_pointer(this)?.label(label);
        Ok(Nothing::default())
    })
}
