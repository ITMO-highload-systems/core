package org.example.notion.minio.service

import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

/**
 * Interface for interacting with MinIO storage.
 *
 * This interface defines methods for uploading and retrieving images from MinIO.
 * The main functions include uploading images to MinIO and retrieving images by their unique hash.
 *
 * @author Nikita Ivanov
 */
interface MinioStorageService {

    /**
     * Uploads an image to MinIO.
     *
     * This method takes an image file in `MultipartFile` format and uploads it to MinIO.
     * The file is identified and stored in the storage with a unique name derived from its hash.
     *
     * @param file The image file to upload.
     */
    fun uploadImg(file: MultipartFile)

    /**
     * Retrieves an image from MinIO by its unique hash.
     *
     * This method returns an `InputStream` for the image stored in MinIO,
     * identified by its unique hash. The data stream can be used to read the image content.
     *
     * @param fileHash The unique hash of the image.
     * @return An `InputStream` for reading the image content.
     */
    fun getImageContent(fileHash: String): InputStream

    fun getImageUrl(fileHash: String): String

    /**
     * Deletes an image from MinIO by its unique hash.
     */
    fun deleteImage(fileHash: String)
}
