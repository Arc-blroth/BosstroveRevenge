use std::slice;

use anyhow::{anyhow, Result};

use crate::error::RoastError;

/// A `#[repr(C)]` option.
#[repr(C)]
#[derive(Copy, Clone, PartialEq, Eq, Debug)]
pub enum ForeignOption<T> {
    /// No value
    None,
    /// Some value `T`
    Some(T),
}

impl<T> From<Option<T>> for ForeignOption<T> {
    #[inline]
    fn from(option: Option<T>) -> Self {
        match option {
            None => Self::None,
            Some(inner) => Self::Some(inner),
        }
    }
}

impl<T> From<ForeignOption<T>> for Option<T> {
    #[inline]
    fn from(option: ForeignOption<T>) -> Self {
        match option {
            ForeignOption::None => None,
            ForeignOption::Some(inner) => Some(inner),
        }
    }
}

/// A `#[repr(C)]` two-element tuple.
#[repr(C)]
#[derive(Copy, Clone, PartialEq, Eq, Debug)]
pub struct ForeignPair<T, U>(pub T, pub U);

impl<T, U> From<(T, U)> for ForeignPair<T, U> {
    #[inline]
    fn from(tuple: (T, U)) -> Self {
        Self(tuple.0, tuple.1)
    }
}

impl<T, U> From<ForeignPair<T, U>> for (T, U) {
    #[inline]
    fn from(pair: ForeignPair<T, U>) -> Self {
        (pair.0, pair.1)
    }
}

/// See [`core::intrinsics::is_aligned_and_not_null`].
#[inline]
pub fn is_aligned_and_not_null<T>(ptr: *const T) -> bool {
    !ptr.is_null() && ptr as usize % std::mem::align_of::<T>() == 0
}

/// Wraps a pointer and length into a slice.
/// The data will **not** be copied into a new space.
pub fn slice_from_foreign<'a, T>(ptr: *const T, len: usize) -> Result<&'a [T]> {
    if !is_aligned_and_not_null(ptr) {
        Err(anyhow!(RoastError::NullPointer(
            "Provided data is null or not aligned!".to_string()
        )))
    } else {
        // SAFETY: Pointer is aligned and non-null.
        Ok(unsafe { slice::from_raw_parts(ptr, len) })
    }
}

/// Converts a pointer and length to a String.
/// This function assumes that we don't own the data in the
/// pointer and will copy the data into a new String.
pub fn string_from_foreign(ptr: *const u8, len: usize) -> Result<String> {
    if !is_aligned_and_not_null(ptr) {
        Err(anyhow!(RoastError::NullPointer(
            "Provided string is null or not aligned!".to_string()
        )))
    } else {
        // SAFETY: Pointer is aligned and non-null.
        Ok(std::str::from_utf8(unsafe { slice::from_raw_parts(ptr, len) })?.to_string())
    }
}
