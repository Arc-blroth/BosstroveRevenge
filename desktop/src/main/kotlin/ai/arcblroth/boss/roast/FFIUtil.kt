package ai.arcblroth.boss.roast

import ai.arcblroth.boss.roast.lib.ForeignRoastError
import ai.arcblroth.boss.roast.lib.Roast.Generic
import ai.arcblroth.boss.roast.lib.Roast.IllegalArgument
import ai.arcblroth.boss.roast.lib.Roast.IllegalState
import ai.arcblroth.boss.roast.lib.Roast.NullPointer
import ai.arcblroth.boss.roast.lib.Roast.roast_create_propagated_error
import ai.arcblroth.boss.roast.lib.Roast.roast_free_error
import jdk.incubator.foreign.MemoryAddress
import jdk.incubator.foreign.MemoryCopy
import jdk.incubator.foreign.MemorySegment
import jdk.incubator.foreign.ResourceScope
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Helper function to convert [Boolean]s to C bools.
 */
fun Boolean.toCBool(): Byte = if (this) 1 else 0

/**
 * Copies a [ByteArray] into a new native [MemorySegment].
 */
fun copyToNativeArray(scope: ResourceScope, array: ByteArray): MemorySegment {
    val dest = MemorySegment.allocateNative(array.size.toLong(), scope)
    MemoryCopy.copyFromArray(array, 0, array.size, dest, 0)
    return dest
}

fun fromRustString(ptr: MemoryAddress, len: Long) = ResourceScope.newConfinedScope().use { scope ->
    String(ptr.asSegment(len, scope).toByteArray(), Charsets.UTF_8)
}

fun toRustString(scope: ResourceScope, string: String): MemorySegment {
    val bytes = string.toByteArray(Charsets.UTF_8)
    return copyToNativeArray(scope, bytes)
}

/**
 * Throws an [AssertionError].
 */
@Suppress("NOTHING_TO_INLINE", "FunctionName")
inline fun UNREACHABLE(reason: String = "this should be impossible") = AssertionError(reason)

/**
 * Wraps the given closure into a try-catched closure
 * that returns a ForeignRoastResult.
 */
inline fun wrapUpcall(
    scope: ResourceScope,
    crossinline resultAllocate: (ResourceScope) -> MemorySegment,
    crossinline `tag$set`: (MemorySegment, Int) -> Unit,
    crossinline `err$slice`: (MemorySegment) -> MemorySegment,
    crossinline block: () -> Unit,
): () -> MemorySegment = {
    try {
        block()
        resultAllocate(scope).apply {
            `tag$set`(this, 0)
        }
    } catch (t: Throwable) {
        // Serialize the error and propagate it.
        val outStream = ByteArrayOutputStream()
        ObjectOutputStream(outStream).use { it.writeObject(t) }
        val payload = copyToNativeArray(scope, outStream.toByteArray())
        val error = roast_create_propagated_error(scope, payload.address(), payload.byteSize())
        resultAllocate(scope).apply {
            `tag$set`(this, 1)
            `err$slice`(this).copyFrom(error)
        }
    }
}

/**
 * A helper function that unwraps the result of a `ForeignRoastResult`,
 * returning the contained value if `Ok` and throwing a [PropagatedRoastError]
 * if `Err`.
 */
inline fun MemorySegment.unwrap(
    `tag$get`: (MemorySegment) -> Int,
    `ok$slice`: (MemorySegment) -> MemorySegment,
    `err$slice`: (MemorySegment) -> MemorySegment,
): MemorySegment {
    if (`tag$get`(this) == 0) {
        return `ok$slice`(this)
    } else {
        handleForeignRoastError(`err$slice`(this))
    }
}

/**
 * A helper function that unwraps the result of a `ForeignRoastResult`,
 * returning the contained value if `Ok` and throwing a [PropagatedRoastError]
 * if `Err`.
 */
inline fun <T> MemorySegment.unwrap(
    `tag$get`: (MemorySegment) -> Int,
    `ok$get`: (MemorySegment) -> T,
    `err$slice`: (MemorySegment) -> MemorySegment,
): T {
    if (`tag$get`(this) == 0) {
        return `ok$get`(this)
    } else {
        handleForeignRoastError(`err$slice`(this))
    }
}

@PublishedApi
internal fun handleForeignRoastError(error: MemorySegment): Nothing {
    throw try {
        when (val ty = ForeignRoastError.`ty$get`(error)) {
            Generic(), NullPointer(), IllegalArgument(), IllegalState() -> {
                val errorMessage = fromRustString(
                    ForeignRoastError.`payload_ptr$get`(error),
                    ForeignRoastError.`payload_len$get`(error)
                )
                when (ty) {
                    Generic() -> RoastException(errorMessage)
                    NullPointer() -> NullPointerException(errorMessage)
                    IllegalArgument() -> IllegalArgumentException(errorMessage)
                    IllegalState() -> IllegalStateException(errorMessage)
                    else -> UNREACHABLE()
                }
            }
            else -> {
                val payload = ResourceScope.newConfinedScope().use { scope ->
                    ForeignRoastError.`payload_ptr$get`(error).asSegment(
                        ForeignRoastError.`payload_len$get`(error),
                        scope
                    ).toByteArray()
                }
                val exception = ObjectInputStream(ByteArrayInputStream(payload)).use {
                    it.readObject()
                }
                if (exception is Throwable) {
                    exception
                } else {
                    AssertionError("Propagated error payload was not an exception?")
                }
            }
        }
    } finally {
        roast_free_error(error)
    }
}
