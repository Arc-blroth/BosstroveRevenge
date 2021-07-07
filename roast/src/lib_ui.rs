//! JNI implementation of the `ai.arcblroth.roast.RoastUI`
//! and `ai.arcblroth.roast.RoastArea` native methods.

use std::ops::{Deref, DerefMut};
use std::sync::MutexGuard;

use egui::{CentralPanel, Frame, Label, Layout, Pos2 as EPos2, TextStyle, Ui, Vec2 as EVec2, Window};
use jni::objects::{JObject, JString, JValue};
use jni::signature::JavaType;
use jni::sys::{jboolean, jobject, jstring, JNI_TRUE};
use jni::JNIEnv;

use crate::backend::with_renderer;
use crate::check_backend;
use crate::jni_classes::{JavaBounds, JavaColor, JavaLabel};
use crate::jni_types::*;

/// Wrapper around a `*mut Ui` pointer
/// so that we can implement [`Send`] on
/// the pointer.
#[repr(transparent)]
struct UiRef {
    ptr: *mut Ui,
}

impl Deref for UiRef {
    type Target = Ui;

    fn deref(&self) -> &Self::Target {
        unsafe { &*self.ptr }
    }
}

impl DerefMut for UiRef {
    fn deref_mut(&mut self) -> &mut Self::Target {
        unsafe { &mut *self.ptr }
    }
}

unsafe impl Send for UiRef {}

/// Creates a closure that takes a `&mut Ui` and invokes
/// the provided Kotlin lambda with it.
///
/// The returned closure should **only** be used within the
/// lifetime of env - otherwise undefined behavior may occur!
#[must_use = "Should be immediately passed to an egui UI builder"]
fn add_contents(env: JNIEnv, kt_add_contents: jobject) -> impl FnOnce(&mut Ui) -> () {
    // We need to extend the lifetime of env in order to pass it into a closure.
    let env = unsafe { std::mem::transmute::<_, JNIEnv<'static>>(env) };

    move |ui: &mut Ui| {
        // Store a pointer to the UI into a new Area object
        let area_object = env.new_object(ROAST_AREA_CLASS, "()V", &[]).unwrap();
        let ui_pointer = UiRef { ptr: ui as *mut Ui };
        env.set_rust_field(area_object, "pointer", ui_pointer).unwrap();

        // Invoke the callback
        let function_class = env.get_object_class(kt_add_contents).unwrap();
        let invoke_method = env
            .get_method_id(function_class, "invoke", "(Ljava/lang/Object;)Ljava/lang/Object;")
            .unwrap();
        env.call_method_unchecked(
            kt_add_contents,
            invoke_method,
            JavaType::Object(OBJECT_TYPE.to_string()),
            &[JValue::Object(JObject::from(area_object))],
        )
        .unwrap();

        // Retrieve the UI pointer and drop it
        let ui_pointer: UiRef = env.take_rust_field(area_object, "pointer").unwrap();
        drop(ui_pointer);
    }
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
fn check_roast_ui(env: &JNIEnv, this: jobject) {
    check_backend(&env, this).unwrap();
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastUI_centerPanel(
    env: JNIEnv,
    this: jobject,
    kt_add_contents: jobject,
) {
    catch_panic!(env, {
        check_roast_ui(&env, this);

        let ctx = with_renderer(|renderer| renderer.gui.context());
        CentralPanel::default().show(&ctx, add_contents(env, kt_add_contents));
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastUI_area(
    env: JNIEnv,
    this: jobject,
    name: jstring,
    bounds: jobject,
    kt_add_contents: jobject,
) {
    catch_panic!(env, {
        check_roast_ui(&env, this);

        let bounds_class = JavaBounds::accessor(env);

        let name: String = env.get_string(JString::from(name)).unwrap().into();
        let pos = EPos2::new(bounds_class.x(bounds), bounds_class.y(bounds));
        let size = EVec2::new(bounds_class.w(bounds), bounds_class.h(bounds));

        // Contrary to the name of the method this uses a Window
        // rather than an Area because Areas can have weird layout
        // when attempting to center and justify.
        let ctx = with_renderer(|renderer| renderer.gui.context());
        Window::new(name)
            .fixed_pos(pos)
            .fixed_size(size)
            .title_bar(false)
            .frame(Frame::none())
            .show(&ctx, add_contents(env, kt_add_contents));
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastUI_window(
    env: JNIEnv,
    this: jobject,
    name: jstring,
    resizable: jboolean,
    kt_add_contents: jobject,
) {
    catch_panic!(env, {
        check_roast_ui(&env, this);

        let name: String = env.get_string(JString::from(name)).unwrap().into();
        let resizable = resizable == JNI_TRUE;

        let ctx = with_renderer(|renderer| renderer.gui.context());
        Window::new(name)
            .resizable(resizable)
            .show(&ctx, add_contents(env, kt_add_contents));
    });
}

// ===================================
//             RoastArea
// ===================================

const UI_NOT_FOUND_MSG: &str = "RoastArea pointer is null - this should *never* occur!";

/// Gets the UiRef pointer stored on this RoastArea object.
///
/// # Panics
/// If the pointer is null. This will also instruct the JVM to
/// throw a NullPointerException.
fn get_ui_pointer<'a>(env: &'a JNIEnv, this: jobject) -> MutexGuard<'a, UiRef> {
    let ui_ref: MutexGuard<UiRef> = env.get_rust_field(this, "pointer").unwrap();
    if ui_ref.ptr.is_null() {
        env.throw_new(NULL_POINTER_EXCEPTION_CLASS, UI_NOT_FOUND_MSG).unwrap();
        panic!("{}", UI_NOT_FOUND_MSG);
    }
    ui_ref
}

macro_rules! impl_layout_functions {
    ($($java_name:ident => $rust_name:ident),*$(,)?) => {
        $(
            #[no_mangle]
            pub extern "system" fn $java_name(
                env: JNIEnv,
                this: jobject,
                kt_add_contents: jobject,
            ) {
                catch_panic!(env, {
                    let mut ui = get_ui_pointer(&env, this);
                    ui.$rust_name(add_contents(env, kt_add_contents));
                });
            }
        )*
    }
}

impl_layout_functions! {
    Java_ai_arcblroth_boss_roast_RoastArea_horizontal => horizontal,
    Java_ai_arcblroth_boss_roast_RoastArea_horizontalWrapped => horizontal_wrapped,
    Java_ai_arcblroth_boss_roast_RoastArea_vertical => vertical,
    Java_ai_arcblroth_boss_roast_RoastArea_verticalCentered => vertical_centered,
    Java_ai_arcblroth_boss_roast_RoastArea_verticalCenteredJustified => vertical_centered_justified,
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastArea_horizontalRight(
    env: JNIEnv,
    this: jobject,
    kt_add_contents: jobject,
) {
    catch_panic!(env, {
        let mut ui = get_ui_pointer(&env, this);
        ui.horizontal(|ui| {
            ui.with_layout(Layout::right_to_left(), add_contents(env, kt_add_contents));
        });
    });
}

#[no_mangle]
pub extern "system" fn Java_ai_arcblroth_boss_roast_RoastArea_label(env: JNIEnv, this: jobject, java_label: jobject) {
    catch_panic!(env, {
        let label_class = JavaLabel::accessor(env);
        let color_class = JavaColor::accessor(env);

        let mut label = Label::new(label_class.text(java_label));

        let wrap_obj = label_class.wrap(java_label);
        if !wrap_obj.is_null() {
            let wrap = call_getter!(env, wrap_obj, "booleanValue", "Z").z().unwrap();
            label = label.wrap(wrap);
        }

        let text_style_obj = label_class.textStyle(java_label);
        if !text_style_obj.is_null() {
            let text_style = match call_getter!(env, text_style_obj, "ordinal", "I").i().unwrap() {
                0 => TextStyle::Small,
                1 => TextStyle::Body,
                2 => TextStyle::Button,
                3 => TextStyle::Heading,
                4 => TextStyle::Monospace,
                _ => {
                    env.throw_new(ILLEGAL_ARGUMENT_EXCEPTION_CLASS, "Invalid text style!")
                        .unwrap();
                    panic!();
                }
            };
            label = label.text_style(text_style);
        }

        let background_color = label_class.backgroundColor(java_label);
        let background_color = color_class.as_color32(background_color);
        label = label.background_color(background_color);

        let text_color = label_class.textColor(java_label);
        if !text_color.is_null() {
            let text_color = color_class.as_color32(text_color);
            label = label.text_color(text_color);
        }

        if label_class.code(java_label) {
            label = label.code();
        }
        if label_class.strong(java_label) {
            label = label.strong();
        }
        if label_class.weak(java_label) {
            label = label.weak();
        }
        if label_class.strikethrough(java_label) {
            label = label.strikethrough();
        }
        if label_class.underline(java_label) {
            label = label.underline();
        }
        if label_class.italics(java_label) {
            label = label.italics();
        }
        if label_class.raised(java_label) {
            label = label.raised();
        }

        let mut ui = get_ui_pointer(&env, this);
        ui.label(label);
    });
}
