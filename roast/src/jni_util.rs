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
                let _ = $env.throw_new(
                    "ai/arcblroth/boss/desktop/RoastException",
                    format!("{}: {}", $msg, err),
                );
                return;
            }
        }
    };
    ($result:expr, $env:expr) => {
        match $result {
            Ok(res) => res,
            Err(err) => {
                let _ = $env.throw_new(
                    "ai/arcblroth/boss/desktop/RoastException",
                    format!("{}", err),
                );
                return;
            }
        }
    };
}
