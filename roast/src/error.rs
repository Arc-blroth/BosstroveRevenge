//! Since the JVM does not expect native code to panic
//! (and will in fact crash if that happens), all of
//! Roast's external functions proactively catch panics
//! and return [`ForeignRoastResult`]s.
//!
//! As part of [`ForeignRoastError`], all errors come
//! with a string message that contains the reason for
//! the error and a backtrace. Since this string needs
//! to be returned to foreign code, it is allocated
//! on the Rust heap and then deconstructed into a
//! raw pointer. The foreign code **must** ensure
//! that the error message is later reconstructed and
//! deallocated by calling [`roast_free_error`].

use std::panic::UnwindSafe;

use anyhow::Result;
use thiserror::Error;

/// A special error type that will convert to
/// the respective variant of [`ForeignRoastError`].
#[derive(Error, Debug)]
pub enum RoastError {
    #[error("{0}")]
    Generic(String),
    #[error("{0}")]
    NullPointer(String),
    #[error("{0}")]
    IllegalArgument(String),
    #[error("{0}")]
    IllegalState(String),
}

/// Exception types for [`ForeignRoastError`].
#[repr(C)]
#[derive(Clone)]
pub enum ForeignRoastErrorType {
    /// Throws a RoastException
    Generic,
    /// Throws a NullPointerException
    NullPointer,
    /// Throws an IllegalArgumentException
    IllegalArgument,
    /// Throws an IllegalStateException
    IllegalState,
    /// Throws the exception that is serialized
    /// with the ForeignRoastError payload.
    Propagated,
}

/// The base error type for all top-level
/// external functions. Each variant of
/// `ty` will cause a different type of
/// exception to be thrown from Java.
#[repr(C)]
#[derive(Clone)]
pub struct ForeignRoastError {
    /// Type of this error.
    pub ty: ForeignRoastErrorType,

    /// If the type of this error is `Propagated`,
    /// this is a pointer to an arbitrary
    /// payload representing some foreign error
    /// type. The Java side of Roast stores the
    /// serialized form of a Java exception here.
    ///
    /// For all other error types, this is a
    /// pointer to a UTF-8 string containing
    /// this error's message.
    ///
    /// This pointer **must** have been allocated
    /// by the Rust allocator to be valid.
    pub payload_ptr: *mut u8,

    /// Length in bytes of the error payload.
    pub payload_len: usize,

    /// Capacity in bytes of the allocated memory
    /// for the error payload.
    pub payload_cap: usize,
}

/// We implement Send on ForeignRoastError
/// so that it can be used as a panic payload.
// FIXME: Remove when `panic_if_err` is removed.
unsafe impl Send for ForeignRoastError {}

/// The Result type that is returned by
/// all top-level external functions.
///
/// This does not use Rust's normal
/// Result type since it must be
/// `#[repr(C)]`.
#[repr(C)]
pub enum ForeignRoastResult<T> {
    Ok(T),
    Err(ForeignRoastError),
}

impl<T> ForeignRoastResult<T> {
    /// Converts an [`anyhow::Result`] to a [`ForeignRoastResult`]
    /// suitable for use in FFI.
    /// Note that if the result is an Err, the RoastError message
    /// will contain the debug representation of the source error,
    /// which includes a backtrace.
    ///
    /// # Safety
    /// If the returned result contains an Err, the error's message
    /// **must be** deallocated manually by the caller.
    fn from_anyhow(result: Result<T>) -> Self {
        match result {
            Ok(inner) => Self::Ok(inner),
            Err(error) => {
                let (ptr, len, cap) = format!("{:?}", error).into_raw_parts();
                let ty = match error.downcast_ref::<RoastError>() {
                    Some(roast_error) => match roast_error {
                        RoastError::Generic(_) => ForeignRoastErrorType::Generic,
                        RoastError::NullPointer(_) => ForeignRoastErrorType::NullPointer,
                        RoastError::IllegalArgument(_) => ForeignRoastErrorType::IllegalArgument,
                        RoastError::IllegalState(_) => ForeignRoastErrorType::IllegalState,
                    },
                    None => ForeignRoastErrorType::Generic,
                };
                Self::Err(ForeignRoastError {
                    ty,
                    payload_ptr: ptr,
                    payload_len: len,
                    payload_cap: cap,
                })
            }
        }
    }

    /// # Panics
    /// Panics the current thread with the contained
    /// [`ForeignRoastError`] as the payload.
    /// Intended to be used in conjunction with
    /// [`catch_panic`].
    // FIXME: This is incredibly hacky and only exists
    // because egui doesn't support InnerResponse on all
    // of its show() methods.
    pub fn panic_if_err(&self) {
        if let ForeignRoastResult::Err(foreign_error) = self {
            std::panic::panic_any(foreign_error.clone());
        }
    }
}

/// An FFI-safe struct for representing `()`.
#[repr(transparent)]
#[derive(Copy, Clone, Default)]
pub struct Nothing {
    _sadness: u8,
}

impl From<()> for Nothing {
    fn from(_: ()) -> Self {
        Self::default()
    }
}

/// Catches any unwinding panics from the provided closure.
///
/// If no panic is thrown, this will wrap the result
/// returned from the closure into a `ForeignRoastResult::Ok`
/// and return it.
///
/// If a panic is thrown and the payload is a
/// [`ForeignRoastError`], this will wrap the panic payload
/// into a `ForeignRoastResult::Err` and return it.
/// All other panics will return a
/// `ForeignRoastResult::Err(ForeignRoastError::Generic)`.
pub fn catch_panic<F, R>(f: F) -> ForeignRoastResult<R>
where
    F: FnOnce() -> Result<R> + UnwindSafe,
{
    static PANIC_MSG: &str = "panic!";

    match std::panic::catch_unwind(f) {
        Ok(res) => ForeignRoastResult::from_anyhow(res),
        Err(error) => match error.downcast_ref::<ForeignRoastError>() {
            Some(foreign_error) => ForeignRoastResult::Err(foreign_error.clone()),
            None => {
                let (ptr, len, cap) = PANIC_MSG.to_string().into_raw_parts();
                ForeignRoastResult::Err(ForeignRoastError {
                    ty: ForeignRoastErrorType::Generic,
                    payload_ptr: ptr,
                    payload_len: len,
                    payload_cap: cap,
                })
            }
        },
    }
}

/// Creates an instance of a [`ForeignRoastError`] with
/// the `Propagated` error type from an arbitrary payload.
/// The payload will be copied onto the Rust heap, and the
/// caller can safely deallocate the source payload memory
/// after this call.
///
/// The caller **must** ensure that `payload` is aligned,
/// non-zero, and points to `payload_len` number of bytes.
///
/// The caller **must** ensure that the error payload is
/// later freed by calling [`roast_free_error`].
///
/// Intended for use by foreign code.
#[no_mangle]
pub extern "C" fn roast_create_propagated_error(payload: *mut u8, payload_len: usize) -> ForeignRoastError {
    let mut copied_payload = Vec::<u8>::with_capacity(payload_len);

    // SAFETY: The caller must uphold the contract for `copy_from`.
    // Since u8 is Copy no drop guard is needed.
    unsafe {
        copied_payload.as_mut_ptr().copy_from(payload, payload_len);
        copied_payload.set_len(payload_len);
    }

    // SAFETY: The caller is responsible for calling `roast_free_error` later.
    let (ptr, len, cap) = copied_payload.into_raw_parts();

    ForeignRoastError {
        ty: ForeignRoastErrorType::Propagated,
        payload_ptr: ptr,
        payload_len: len,
        payload_cap: cap,
    }
}

/// Frees the memory used in a [`ForeignRoastError`].
///
/// Intended for use by foreign code.
#[no_mangle]
pub extern "C" fn roast_free_error(error: ForeignRoastError) {
    let (ptr, len, cap) = (error.payload_ptr, error.payload_len, error.payload_cap);
    // Reconstruct the error payload and drop it.
    drop(unsafe { Vec::from_raw_parts(ptr, len, cap) });
}
