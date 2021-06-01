use std::marker::PhantomData;

use jni::errors::Result as JNIResult;
use jni::objects::{GlobalRef, JClass, JObject, JValue};
use jni::sys::{jboolean, jbyte, jchar, jdouble, jfloat, jint, jlong, jshort};
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
                let _ = $env.throw_new("ai/arcblroth/boss/desktop/RoastException", "panic!");
            }
        }
    };
    ($env:expr, $code:block else $default:block) => {
        return match std::panic::catch_unwind(|| $code) {
            Ok(res) => res,
            Err(_) => {
                if !$env.exception_check().unwrap_or(false) {
                    let _ = $env.throw_new("ai/arcblroth/boss/desktop/RoastException", "panic!");
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
                let _ = $env.throw_new("ai/arcblroth/boss/desktop/RoastException", format!("{}: {}", $msg, err));
                panic!();
            }
        }
    };
    ($result:expr, $env:expr) => {
        match $result {
            Ok(res) => res,
            Err(err) => {
                let _ = $env.throw_new("ai/arcblroth/boss/desktop/RoastException", format!("{}", err));
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
    ($env:expr, $obj:expr, $getter_name:expr, $ty:literal) => {
        unwrap_or_throw_new!($env.call_method($obj, $getter_name, concat!("()", $ty), &[]), $env)
    };
}

/// Primitive unboxing utils.
pub trait Unboxing {
    fn unbox_byte(self, env: &JNIEnv) -> JNIResult<jbyte>;
    fn unbox_char(self, env: &JNIEnv) -> JNIResult<jchar>;
    fn unbox_short(self, env: &JNIEnv) -> JNIResult<jshort>;
    fn unbox_int(self, env: &JNIEnv) -> JNIResult<jint>;
    fn unbox_long(self, env: &JNIEnv) -> JNIResult<jlong>;
    fn unbox_boolean(self, env: &JNIEnv) -> JNIResult<jboolean>;
    fn unbox_bool(self, env: &JNIEnv) -> JNIResult<bool>;
    fn unbox_float(self, env: &JNIEnv) -> JNIResult<jfloat>;
    fn unbox_double(self, env: &JNIEnv) -> JNIResult<jdouble>;
}

impl Unboxing for JObject<'_> {
    fn unbox_byte(self, env: &JNIEnv<'_>) -> JNIResult<jbyte> {
        env.call_method(self, "byteValue", "()B", &[])?.b()
    }

    fn unbox_char(self, env: &JNIEnv<'_>) -> JNIResult<jchar> {
        env.call_method(self, "charValue", "()C", &[])?.c()
    }

    fn unbox_short(self, env: &JNIEnv<'_>) -> JNIResult<jshort> {
        env.call_method(self, "shortValue", "()S", &[])?.s()
    }

    fn unbox_int(self, env: &JNIEnv<'_>) -> JNIResult<jint> {
        env.call_method(self, "intValue", "()I", &[])?.i()
    }

    fn unbox_long(self, env: &JNIEnv<'_>) -> JNIResult<jlong> {
        env.call_method(self, "longValue", "()J", &[])?.j()
    }

    fn unbox_boolean(self, env: &JNIEnv<'_>) -> JNIResult<jboolean> {
        Ok(self.unbox_bool(env)? as jboolean)
    }

    fn unbox_bool(self, env: &JNIEnv<'_>) -> JNIResult<bool> {
        env.call_method(self, "booleanValue", "()Z", &[])?.z()
    }

    fn unbox_float(self, env: &JNIEnv<'_>) -> JNIResult<jfloat> {
        env.call_method(self, "floatValue", "()F", &[])?.f()
    }

    fn unbox_double(self, env: &JNIEnv<'_>) -> JNIResult<jdouble> {
        env.call_method(self, "doubleValue", "()D", &[])?.d()
    }
}

impl Unboxing for JValue<'_> {
    fn unbox_byte(self, env: &JNIEnv<'_>) -> JNIResult<jbyte> {
        match self {
            JValue::Byte(b) => Ok(b),
            _ => self.l()?.unbox_byte(env),
        }
    }

    fn unbox_char(self, env: &JNIEnv<'_>) -> JNIResult<jchar> {
        match self {
            JValue::Char(c) => Ok(c),
            _ => self.l()?.unbox_char(env),
        }
    }

    fn unbox_short(self, env: &JNIEnv<'_>) -> JNIResult<jshort> {
        match self {
            JValue::Short(s) => Ok(s),
            _ => self.l()?.unbox_short(env),
        }
    }

    fn unbox_int(self, env: &JNIEnv<'_>) -> JNIResult<jint> {
        match self {
            JValue::Int(i) => Ok(i),
            _ => self.l()?.unbox_int(env),
        }
    }

    fn unbox_long(self, env: &JNIEnv<'_>) -> JNIResult<jlong> {
        match self {
            JValue::Long(j) => Ok(j),
            _ => self.l()?.unbox_long(env),
        }
    }

    fn unbox_boolean(self, env: &JNIEnv<'_>) -> JNIResult<jboolean> {
        match self {
            JValue::Bool(b) => Ok(b),
            _ => self.l()?.unbox_boolean(env),
        }
    }

    fn unbox_bool(self, env: &JNIEnv<'_>) -> JNIResult<bool> {
        match self {
            JValue::Bool(b) => Ok(b != 0),
            _ => self.l()?.unbox_bool(env),
        }
    }

    fn unbox_float(self, env: &JNIEnv<'_>) -> JNIResult<jfloat> {
        match self {
            JValue::Float(f) => Ok(f),
            _ => self.l()?.unbox_float(env),
        }
    }

    fn unbox_double(self, env: &JNIEnv<'_>) -> JNIResult<jdouble> {
        match self {
            JValue::Double(d) => Ok(d),
            _ => self.l()?.unbox_double(env),
        }
    }
}
