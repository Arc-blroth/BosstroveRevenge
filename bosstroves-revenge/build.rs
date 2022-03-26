use std::fs::File;
use std::io::Cursor;
use std::{env, fs, io};

use path_macro::path;
use zip::ZipArchive;

const CASCADIA_RELEASE: &str =
    "https://github.com/microsoft/cascadia-code/releases/download/v2111.01/CascadiaCode-2111.01.zip";
const CASCADIA_LICENSE: &str =
    "https://raw.githubusercontent.com/microsoft/cascadia-code/de36d62e777d34d3bed92a7e23988e5d61e0ba02/LICENSE";

fn main() {
    println!("cargo:rerun-if-changed=build.rs");
    download_cascadia_font();
}

fn download_cascadia_font() {
    // Extract files to the generated assets
    // folder, skipping if already extracted
    let out_dir = path!(env::var("CARGO_MANIFEST_DIR").unwrap() / "assets/generated/fonts");
    println!("cargo:rerun-if-changed={}", out_dir.display());
    if !out_dir.exists() {
        fs::create_dir_all(&out_dir).unwrap();

        let bytes = reqwest::blocking::get(CASCADIA_RELEASE).unwrap().bytes().unwrap();
        let mut zip = ZipArchive::new(Cursor::new(bytes)).unwrap();

        let mut font_file = zip.by_name("ttf/static/CascadiaMono-Light.ttf").unwrap();
        let mut font_target = File::create(path!(out_dir / "CascadiaMono-Light.ttf")).unwrap();
        io::copy(&mut font_file, &mut font_target).unwrap();

        let mut license_target = File::create(path!(out_dir / "CascadiaMono-LICENSE")).unwrap();
        reqwest::blocking::get(CASCADIA_LICENSE)
            .unwrap()
            .copy_to(&mut license_target)
            .unwrap();
    }
}
