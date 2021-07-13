use std::marker::PhantomData;

use jni::errors::Result as JNIResult;
use jni::JNIEnv;
use jni::objects::{GlobalRef, JClass, JObject};

/// Slightly more type safe version of GlobalRef
#[derive(Clone)]
pub struct TypedGlobalRef<T> {
    global: GlobalRef,
    ty: PhantomData<T>,
}

impl<T> TypedGlobalRef<T> {
    pub fn new<'a, I: Into<JObject<'a>>>(env: &JNIEnv, obj: I) -> JNIResult<TypedGlobalRef<I>> {
        Ok(TypedGlobalRef::from_ref(env.new_global_ref(obj.into())?))
    }

    pub fn from_ref(global: GlobalRef) -> Self {
        Self {
            global,
            ty: PhantomData,
        }
    }
}

impl<'a> TypedGlobalRef<JObject<'a>> {
    pub fn as_obj(&self) -> JObject {
        self.global.as_obj()
    }
}

impl<'a> TypedGlobalRef<JClass<'a>> {
    pub fn as_class(&self) -> JClass {
        JClass::from(self.global.as_obj().clone())
    }
}

/// Catches any unwinding panics and rethrows them as a Java exception.
/// This will not attempt to throw a second exception if an exception is already being throw.
#[macro_export]
macro_rules! catch_panic {
    ($env:expr, $code:block) => {
        if let Err(_) = std::panic::catch_unwind(|| $code) {
            if !$env.exception_check().unwrap_or(false) {
                let _ = $env.throw_new($crate::ROAST_EXCEPTION_CLASS, "panic!");
            }
        }
    };
    ($env:expr, $code:block else $default:block) => {
        return match std::panic::catch_unwind(|| $code) {
            Ok(res) => res,
            Err(_) => {
                if !$env.exception_check().unwrap_or(false) {
                    let _ = $env.throw_new($crate::ROAST_EXCEPTION_CLASS, "panic!");
                }
                $default
            }
        }
    };
}

/// Unwraps the Result or throws a Java exception with a message.
#[macro_export]
macro_rules! unwrap_or_throw_new {
    ($result:expr, $env:expr, $msg:expr) => {
        match $result {
            Ok(res) => res,
            Err(err) => {
                // If this throw fails then we have a catastrophic failure
                // and the JVM is probably not in a state to continue running
                // anyway
                let _ = $env.throw_new($crate::ROAST_EXCEPTION_CLASS, format!("{}: {}", $msg, err));
                panic!();
            }
        }
    };
    ($result:expr, $env:expr) => {
        match $result {
            Ok(res) => res,
            Err(err) => {
                let _ = $env.throw_new($crate::ROAST_EXCEPTION_CLASS, format!("{}", err));
                panic!();
            }
        }
    };
}

/// Gets a Kotlin field by calling its getter.
///
/// If calling the getter fails, this will throw
/// a new Java Exception and return.
#[macro_export]
macro_rules! call_getter {
    ($env:expr, $obj:expr, $getter_name:expr, $ty:expr) => {
        unwrap_or_throw_new!($env.call_method($obj, $getter_name, concat!("()", $ty), &[]), $env)
    };
}

/// Defines, in a roughly Kotlin-like syntax, the
/// structure of a Java class. Creates a struct
/// with the same name as the provided short class
/// name that can be used to access fields of that
/// class. To create an accessor, call the
/// `accessor` method and pass in the local JNIEnv.
#[macro_export]
macro_rules! class {
    (@jvm_type Byte) => ("B");
    (@jvm_type Char) => ("C");
    (@jvm_type Short) => ("S");
    (@jvm_type Int) => ("I");
    (@jvm_type Long) => ("J");
    (@jvm_type Boolean) => ("Z");
    (@jvm_type Float) => ("F");
    (@jvm_type Double) => ("D");
    (@jvm_type String) => ("Ljava/lang/String;".to_string());
    (@jvm_type $class_name:expr) => (::std::format!("L{};", $class_name));

    (@field_init $env:expr, $class:expr, $name:ident: $type:tt) => (
        $env.get_field_id($class, stringify!($name), $crate::class!(@jvm_type $type)).unwrap();
    );

    (@field_impl val $name:ident: Byte) => (
        #[inline(always)]
        pub fn $name<O: ::core::convert::Into<::jni::objects::JObject<'a>>>(&self, obj: O) -> u8 {
            self.env.get_field_unchecked(
                obj,
                self.$name,
                ::jni::signature::JavaType::Primitive(::jni::signature::Primitive::Byte)
            )
            .unwrap()
            .b()
            .unwrap()
        }
    );
    (@field_impl val $name:ident: Char) => (
        #[inline(always)]
        pub fn $name<O: ::core::convert::Into<::jni::objects::JObject<'a>>>(&self, obj: O) -> char {
            self.env.get_field_unchecked(
                obj,
                self.$name,
                ::jni::signature::JavaType::Primitive(::jni::signature::Primitive::Char)
            )
            .unwrap()
            .c()
            .unwrap()
        }
    );
    (@field_impl val $name:ident: Short) => (
        #[inline(always)]
        pub fn $name<O: ::core::convert::Into<::jni::objects::JObject<'a>>>(&self, obj: O) -> i16 {
            self.env.get_field_unchecked(
                obj,
                self.$name,
                ::jni::signature::JavaType::Primitive(::jni::signature::Primitive::Short)
            )
            .unwrap()
            .s()
            .unwrap()
        }
    );
    (@field_impl val $name:ident: Int) => (
        #[inline(always)]
        pub fn $name<O: ::core::convert::Into<::jni::objects::JObject<'a>>>(&self, obj: O) -> i32 {
            self.env.get_field_unchecked(
                obj,
                self.$name,
                ::jni::signature::JavaType::Primitive(::jni::signature::Primitive::Int)
            )
            .unwrap()
            .i()
            .unwrap()
        }
    );
    (@field_impl val $name:ident: Long) => (
        #[inline(always)]
        pub fn $name<O: ::core::convert::Into<::jni::objects::JObject<'a>>>(&self, obj: O) -> i64 {
            self.env.get_field_unchecked(
                obj,
                self.$name,
                ::jni::signature::JavaType::Primitive(::jni::signature::Primitive::Long)
            )
            .unwrap()
            .j()
            .unwrap()
        }
    );
    (@field_impl val $name:ident: Boolean) => (
        #[inline(always)]
        pub fn $name<O: ::core::convert::Into<::jni::objects::JObject<'a>>>(&self, obj: O) -> bool {
            self.env.get_field_unchecked(
                obj,
                self.$name,
                ::jni::signature::JavaType::Primitive(::jni::signature::Primitive::Boolean)
            )
            .unwrap()
            .z()
            .unwrap()
        }
    );
    (@field_impl val $name:ident: Float) => (
        #[inline(always)]
        pub fn $name<O: ::core::convert::Into<::jni::objects::JObject<'a>>>(&self, obj: O) -> f32 {
            self.env.get_field_unchecked(
                obj,
                self.$name,
                ::jni::signature::JavaType::Primitive(::jni::signature::Primitive::Float)
            )
            .unwrap()
            .f()
            .unwrap()
        }
    );
    (@field_impl val $name:ident: Double) => (
        #[inline(always)]
        pub fn $name<O: ::core::convert::Into<::jni::objects::JObject<'a>>>(&self, obj: O) -> f64 {
            self.env.get_field_unchecked(
                obj,
                self.$name,
                ::jni::signature::JavaType::Primitive(::jni::signature::Primitive::Double)
            )
            .unwrap()
            .d()
            .unwrap()
        }
    );
    (@field_impl val $name:ident: String) => (
        #[inline(always)]
        pub fn $name<O: ::core::convert::Into<::jni::objects::JObject<'a>>>(&self, obj: O) -> String {
            let string = self.env.get_field_unchecked(
                obj,
                self.$name,
                ::jni::signature::JavaType::Object($crate::class!(@jvm_type String))
            )
            .unwrap()
            .l()
            .unwrap();
            self.env.get_string(::jni::objects::JString::from(string)).unwrap().into()
        }
    );
    (@field_impl val $name:ident: $class_name:expr) => (
        #[inline(always)]
        pub fn $name<O>(&self, obj: O) -> ::jni::objects::JObject<'a>
        where
            O: ::core::convert::Into<::jni::objects::JObject<'a>>,
        {
            self.env.get_field_unchecked(
                obj,
                self.$name,
                ::jni::signature::JavaType::Object($crate::class!(@jvm_type $class_name))
            )
            .unwrap()
            .l()
            .unwrap()
        }
    );

    ($name:expr, $(data)? class $short_name:ident ($($va:tt $field:ident: $ty:tt$(?)?),+$(,)?)) => {
        #[allow(non_snake_case)]
        pub struct $short_name<'a> {
            env: ::jni::JNIEnv<'a>,
            $($field: ::jni::objects::JFieldID<'a>,)+
        }

        #[allow(non_snake_case)]
        impl<'a> $short_name<'a> {
            #[inline]
            pub fn accessor(env: ::jni::JNIEnv<'a>) -> Self {
                let class = env.find_class($name).unwrap();
                $(let $field = $crate::class!(@field_init env, class, $field: $ty);)+
                Self {
                    env,
                    $($field),+
                }
            }

            $($crate::class!(@field_impl $va $field: $ty);)+
        }
    };
}

/// Defines, in an unholy fusion of Kotlin and Rust syntax,
/// a mapping between a Java enum and a Rust enum.
/// Creates a struct with the same name as the
/// provided short class name that can be used to convert
/// a JObject of that enum class into the Rust enum.
#[macro_export]
macro_rules! enum_class {
    // The two `@ord_to_rust` rules use tt-munching and
    // push-down accumulation to generate a match statement
    // that looks like this:
    // ```rust
    // match $obj {
    //     x if x == 0i32 => $rust_variant_0,
    //     x if x == 0i32 + 1i32 => $rust_variant_1,
    //     x if x == 0i32 + 1i32 + 1i32 => $rust_variant_2,
    //     _ => { /* panic */ },
    // }
    // ```
    // The `x if x == 0` match patterns are used because
    // basic match patterns cannot take expressions.
    // Luckily, the compiler will properly
    // [optimize this implementation detail][1]
    // away by folding constants, reducing the above to
    // ```rust
    // match $obj {
    //     0i32 => $rust_variant_0,
    //     1i32 => $rust_variant_1,
    //     2i32 => $rust_variant_2,
    //     _ => { /* panic */ },
    // }
    // ```
    //
    // [1]: https://play.rust-lang.org/?version=nightly&mode=release&edition=2018&gist=946278dca19c5d7a281029d851b26743
    (@ord_to_rust $name:expr, $rust_name:ident, $self:expr, $obj:expr, $ordinal:expr, (), ($($push:tt)*)) => {
        match $self.env.call_method_unchecked(
            $obj,
            $self.ordinal_method,
            ::jni::signature::JavaType::Primitive(::jni::signature::Primitive::Int),
            &[],
        )
        .unwrap()
        .i()
        .unwrap() {
            $(
                $push
            )*
            _ => {
                $self.env.throw_new(
                    $crate::jni_types::ILLEGAL_ARGUMENT_EXCEPTION_CLASS,
                    ::std::format!("Invalid variant of {}!", $name),
                )
                .unwrap();
                ::std::panic!();
            }
        }
    };
    (@ord_to_rust
        $name:expr, $rust_name:ident, $self:expr, $obj:expr, $ordinal:expr,
        ($java_variant:ident => $rust_variant:ident $($tail:tt)*),
        ($($push:tt)*)
    ) => {
        $crate::enum_class!(
            @ord_to_rust $name, $rust_name, $self, $obj, $ordinal + 1i32,
            ($($tail)*),
            ($($push)* x if x == $ordinal => $rust_name::$rust_variant,)
        )
    };

    (@get_java_variant $rust_name:ident, $obj:expr, $($java_variant:ident => $rust_variant:ident),+) => (
        match $obj {
            $(
                $rust_name::$rust_variant => stringify!($java_variant),
            )+
        }
    );

    ($name:expr => $rust_name:ident, enum class $short_name:ident {
        $($java_variant:tt $arrow:tt $rust_variant:tt),+$(,)?
    }) => {
        pub struct $short_name<'a> {
            env: ::jni::JNIEnv<'a>,
            class: ::jni::objects::JClass<'a>,
            ordinal_method: ::jni::objects::JMethodID<'a>,
        }

        impl<'a> $short_name<'a> {
            #[inline]
            pub fn accessor(env: ::jni::JNIEnv<'a>) -> Self {
                let class = env.find_class($name).unwrap();
                let ordinal_method = env.get_method_id(class, "ordinal", "()I").unwrap();
                Self {
                    env,
                    class,
                    ordinal_method,
                }
            }

            /// Converts a Java object to its Rust enum variant.
            ///
            /// # Panics
            /// If the Java object is not an enum or does not have a valid ordinal.
            #[inline(always)]
            pub fn from_java<O: ::core::convert::Into<::jni::objects::JObject<'a>>>(&self, obj: O) -> $rust_name {
                $crate::enum_class!(@ord_to_rust $name, $rust_name, self, obj, 0i32,
                                    ($($java_variant $arrow $rust_variant)+), ())
            }

            /// Converts a Rust enum variant to the Java enum variant.
            #[inline(always)]
            pub fn to_java(&self, obj: $rust_name) -> ::jni::objects::JObject<'a> {
                let java_variant_name = crate::enum_class!(@get_java_variant $rust_name, obj,
                                                           $($java_variant $arrow $rust_variant),+);
                let enum_type = $crate::class!(@jvm_type $name);

                self.env.get_static_field_unchecked(
                    self.class,
                    (self.class, java_variant_name, enum_type.clone()),
                    ::jni::signature::JavaType::Object(enum_type),
                )
                .unwrap()
                .l()
                .unwrap()
            }
        }
    };
}
