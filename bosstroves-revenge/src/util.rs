/// A `uuid!` macro for compile-time UUID verification.
// TODO: this should be removed once the uuid! macro
// is stabilized in the uuid-rs crate proper
pub macro uuid($uuid:literal) {{
    ::uuid::Uuid::from_bytes(::uuid_macro_internal::parse_lit!($uuid))
}}
