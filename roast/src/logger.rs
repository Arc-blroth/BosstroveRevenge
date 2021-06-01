use std::convert::TryFrom;

use jni::errors::Result as JNIResult;
use jni::objects::{JClass, JMethodID, JObject, JValue};
use jni::signature::{JavaType, Primitive};
use jni::JNIEnv;
use log::{Level, LevelFilter, Log, Metadata, Record, SetLoggerError};

use crate::jni_util::TypedGlobalRef;

/// Logging facade that redirects to the RoastBackend logger.
pub struct JavaLogger<'a> {
    env: JNIEnv<'a>,
    logger: TypedGlobalRef<JObject<'a>>,
    #[allow(unused)]
    logger_class: TypedGlobalRef<JClass<'a>>,

    method_error: JMethodID<'a>,
    method_warn: JMethodID<'a>,
    method_info: JMethodID<'a>,
    method_debug: JMethodID<'a>,
    method_trace: JMethodID<'a>,
}

impl<'a> JavaLogger<'a> {
    #[must_use = "call init() or else :)"]
    pub fn new(env: JNIEnv<'a>) -> JNIResult<Self> {
        let logger = TypedGlobalRef::<JObject>::new(
            &env,
            JObject::try_from(env.get_static_field(
                "ai/arcblroth/boss/roast/RoastBackend",
                "LOGGER",
                "Lorg/slf4j/Logger;",
            )?)?,
        )?;
        let logger_class = TypedGlobalRef::<JClass>::new(&env, env.get_object_class(logger.as_obj())?)?;

        // DRY taken a bit too far?
        macro_rules! define_methods {
            ($($var:ident, $name:expr),*$(,)?) => {
                $(let $var = env.get_method_id(logger_class.as_class(), $name, "(Ljava/lang/String;)V")?;)*
            }
        }
        #[rustfmt::skip]
        define_methods!(
            method_error, "error",
            method_warn, "warn",
            method_info, "info",
            method_debug, "debug",
            method_trace, "trace",
        );

        Ok(Self {
            env,
            logger,
            logger_class,
            method_error,
            method_warn,
            method_info,
            method_debug,
            method_trace,
        })
    }
}

impl JavaLogger<'static> {
    pub fn init(self) -> Result<(), SetLoggerError> {
        log::set_max_level(LevelFilter::Trace);
        log::set_boxed_logger(Box::new(self))
    }
}

unsafe impl<'a> Sync for JavaLogger<'a> {}
unsafe impl<'a> Send for JavaLogger<'a> {}

impl<'a> Log for JavaLogger<'a> {
    fn enabled(&self, metadata: &Metadata<'_>) -> bool {
        // To prevent recursion, we never log stuff from jni-rs
        !metadata.target().starts_with("jni")
    }

    fn log(&self, record: &Record<'_>) {
        if !self.enabled(record.metadata()) {
            return;
        }
        let string = self.env.new_string(format!("{}", record.args())).unwrap();
        let args = [JValue::Object(*string)];
        self.env
            .call_method_unchecked(
                self.logger.as_obj(),
                match record.level() {
                    Level::Error => self.method_error,
                    Level::Warn => self.method_warn,
                    Level::Info => self.method_info,
                    Level::Debug => self.method_debug,
                    Level::Trace => self.method_trace,
                },
                JavaType::Primitive(Primitive::Void),
                &args,
            )
            .unwrap();
    }

    fn flush(&self) {}
}
