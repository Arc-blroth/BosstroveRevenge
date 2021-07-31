use log::{Level, LevelFilter, Log, Metadata, Record, SetLoggerError};

/// A logger callback that takes a slice of characters.
pub type JavaLoggerCallback = extern "C" fn(*mut u8, usize) -> ();

/// The six ~~infinity stones~~ callbacks
/// that log at different levels to the
/// `RoastBackend` logger.
#[repr(C)]
pub struct JavaLoggerCallbacks {
    error: JavaLoggerCallback,
    warn: JavaLoggerCallback,
    info: JavaLoggerCallback,
    debug: JavaLoggerCallback,
    trace: JavaLoggerCallback,
}

/// Logging facade that redirects to the RoastBackend logger.
pub struct JavaLogger {
    callbacks: JavaLoggerCallbacks,
}

impl JavaLogger {
    #[must_use = "call init() or else :)"]
    pub fn new(callbacks: JavaLoggerCallbacks) -> Self {
        Self { callbacks }
    }
}

impl JavaLogger {
    pub fn init(self) -> Result<(), SetLoggerError> {
        log::set_max_level(LevelFilter::Trace);
        log::set_boxed_logger(Box::new(self))
    }
}

unsafe impl Sync for JavaLogger {}
unsafe impl Send for JavaLogger {}

impl Log for JavaLogger {
    fn enabled(&self, _metadata: &Metadata<'_>) -> bool {
        true
    }

    fn log(&self, record: &Record<'_>) {
        if !self.enabled(record.metadata()) {
            return;
        }
        let mut string = format!("{}", record.args());
        (match record.level() {
            Level::Error => self.callbacks.error,
            Level::Warn => self.callbacks.warn,
            Level::Info => self.callbacks.info,
            Level::Debug => self.callbacks.debug,
            Level::Trace => self.callbacks.trace,
        })(string.as_mut_ptr(), string.len());
    }

    fn flush(&self) {}
}
