package org.example.notion.util

import org.springframework.web.multipart.MultipartFile
import java.security.MessageDigest

fun calculateFileHash(file: MultipartFile, algorithm: String = "SHA-256"): String {
    val digest = MessageDigest.getInstance(algorithm)
    file.inputStream.use { inputStream ->
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }
    val hashBytes = digest.digest()
    return hashBytes.joinToString("") { "%02x".format(it) }
}