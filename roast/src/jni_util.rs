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
                ::jni::signature::JavaType::Primitive(::jni::signature::Primitive::Integer)
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

    ($name:expr, $(data)? class $short_name:ident ($($va:tt $field:ident: $ty:tt),+$(,)?)) => {
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
