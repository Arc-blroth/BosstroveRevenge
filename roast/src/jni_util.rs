use std::marker::PhantomData;

use jni::errors::Result as JNIResult;
use jni::objects::{GlobalRef, JClass, JObject};
use jni::JNIEnv;

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
                let _ = $env.throw_new("ai/arcblroth/boss/roast/RoastException", "panic!");
            }
        }
    };
    ($env:expr, $code:block else $default:block) => {
        return match std::panic::catch_unwind(|| $code) {
            Ok(res) => res,
            Err(_) => {
                if !$env.exception_check().unwrap_or(false) {
                    let _ = $env.throw_new("ai/arcblroth/boss/roast/RoastException", "panic!");
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
                let _ = $env.throw_new("ai/arcblroth/boss/roast/RoastException", format!("{}: {}", $msg, err));
                panic!();
            }
        }
    };
    ($result:expr, $env:expr) => {
        match $result {
            Ok(res) => res,
            Err(err) => {
                let _ = $env.throw_new("ai/arcblroth/boss/roast/RoastException", format!("{}", err));
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
