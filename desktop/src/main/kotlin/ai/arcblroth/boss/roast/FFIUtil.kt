package ai.arcblroth.boss.roast

import jdk.incubator.foreign.MemoryAddress
import jdk.incubator.foreign.MemoryCopy
import jdk.incubator.foreign.MemorySegment
import jdk.incubator.foreign.ResourceScope

/**
 * Helper function to convert [Boolean]s to C bools.
 */
fun Boolean.toCBool(): Byte = if (this) 1 else 0

fun fromRustString(ptr: MemoryAddress, len: Long): String =
    String(ptr.asSegment(len, ResourceScope.newImplicitScope()).toByteArray(), Charsets.UTF_8)

fun toRustString(string: String, scope: ResourceScope): MemorySegment {
    val bytes = string.toByteArray(Charsets.UTF_8)
    val dest = MemorySegment.allocateNative(bytes.size.toLong(), scope)
    MemoryCopy.copyFromArray(bytes, 0, bytes.size, dest, 0)
    return dest
}
