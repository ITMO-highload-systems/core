package org.example.notion.app.paragraph.repository

import org.example.notion.app.paragraph.entity.ImageRecord
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface ImageRepository : CrudRepository<ImageRecord, Long> {

    @Query("select * from image_record where image_hash = :imageHash")
    fun findByImageHash(imageHash: String): ImageRecord?

    @Query("select * from image_record where paragraph_id = :paragraphId")
    fun findByParagraphId(paragraphId: Long): List<ImageRecord>

    @Modifying
    @Query("delete from image_record where image_hash = :imageHash")
    fun deleteByImageHash(imageHash: String)

    @Modifying
    @Query("delete from image_record where paragraph_id = :paragraphId")
    fun deleteByParagraphId(paragraphId: Long)
}