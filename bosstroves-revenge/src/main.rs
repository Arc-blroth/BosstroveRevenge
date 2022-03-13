#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

#[cfg(all(not(debug_assertions), feature = "bevy_dyn"))]
compile_error!("Bevy should not be dynamically linked for release builds!");

fn main() {
    println!("Loading...");
}
