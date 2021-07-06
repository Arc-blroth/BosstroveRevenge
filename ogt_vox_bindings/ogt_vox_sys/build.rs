use std::path::PathBuf;

fn main() {
    // Rerun this script if the wrappers are updated
    println!("cargo:rerun-if-changed=wrapper.hpp");
    println!("cargo:rerun-if-changed=wrapper.cpp");

    // Compile opengametools
    cc::Build::new()
        .cpp(true)
        .file("wrapper.cpp")
        .compile("opengametools");

    // Generate bindings
    let bindings = bindgen::Builder::default()
        .header("wrapper.hpp")
        .parse_callbacks(Box::new(bindgen::CargoCallbacks))
        .size_t_is_usize(true)
        .allowlist_type("ogt_.*")
        .allowlist_function("ogt_.*")
        .generate()
        .expect("Unable to generate opengametools bindings!");

    let out_path = PathBuf::from(std::env::var("OUT_DIR").unwrap());
    bindings
        .write_to_file(out_path.join("bindings.rs"))
        .expect("Couldn't write bindings!");
}
