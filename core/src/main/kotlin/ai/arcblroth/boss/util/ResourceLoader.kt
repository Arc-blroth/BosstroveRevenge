package ai.arcblroth.boss.util

import java.io.IOException

object ResourceLoader {
    fun loadResourceAsBytes(res: String): ByteArray =
        ResourceLoader.javaClass.classLoader.getResourceAsStream(res)?.readAllBytes() ?: throw ResourceNotFoundException(res)
}

class ResourceNotFoundException(path: String) : IOException("Resource at $path doesn't exist!")
