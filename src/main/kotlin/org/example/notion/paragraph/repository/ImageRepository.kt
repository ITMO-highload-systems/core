package org.example.notion.paragraph.repository

import org.example.notion.paragraph.entity.ImageRecord
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Repository

@Repository
class ImageRepository(
    private val namedParameterJdbcOperations: NamedParameterJdbcOperations
) {

    companion object {
        private const val SELECT_FROM_IMAGE = """
            select
                id,
                image_hash,
                paragraph_id
            from image_record
        """

        private const val DELETE_FROM_IMAGE = "delete from image_record"

        private const val FIND_BY_IMAGE_ID = "$SELECT_FROM_IMAGE where id = :image_id;"
        private const val FIND_BY_PARAGRAPH_ID = "$SELECT_FROM_IMAGE where paragraph_id = :paragraph_id;"
        private const val FIND_BY_IMAGE_HASH = "$SELECT_FROM_IMAGE where image_hash = :image_hash;"

        private const val DELETE_BY_IMAGE_ID = "$DELETE_FROM_IMAGE where id = :image_id;"
        private const val DELETE_BY_IMAGE_HASH = "$DELETE_FROM_IMAGE where image_hash = :image_hash;"
        private const val DELETE_BY_PARAGRAPH_ID = "$DELETE_FROM_IMAGE where paragraph_id = :paragraph_id;"

        private const val INSERT_INTO_IMAGE = """
            insert into image_record(
                paragraph_id,
                image_hash
            ) values (
                :paragraph_id,
                :image_hash
            )
            RETURNING id, image_hash, paragraph_id;
        """
    }

    private val rowMapper: RowMapper<ImageRecord> = RowMapper { rs, _ ->
        ImageRecord(
            id = rs.getLong("id"),
            paragraphId = rs.getLong("paragraph_id"),
            imageHash = rs.getString("image_hash")
        )
    }

    fun findByImageId(imageId: Long): ImageRecord? =
        namedParameterJdbcOperations.query(
            FIND_BY_IMAGE_ID,
            mapOf("id" to imageId),
            rowMapper
        ).singleOrNull()

    fun findByParagraphId(paragraphId: Long): List<ImageRecord> =
        namedParameterJdbcOperations.query(
            FIND_BY_PARAGRAPH_ID,
            mapOf("paragraph_id" to paragraphId),
            rowMapper
        )

    fun findByImageHash(imageHash: String): ImageRecord? =
        namedParameterJdbcOperations.query(
            FIND_BY_IMAGE_HASH,
            mapOf("image_hash" to imageHash),
            rowMapper
        ).singleOrNull()

    fun deleteByImageId(imageId: Long) =
        namedParameterJdbcOperations.update(
            DELETE_BY_IMAGE_ID,
            mapOf("id" to imageId)
        )

    fun deleteByImageHash(imageHash: String) =
        namedParameterJdbcOperations.update(
            DELETE_BY_IMAGE_HASH,
            mapOf("image_hash" to imageHash)
        )

    fun deleteByParagraphId(paragraphId: Int) =
        namedParameterJdbcOperations.update(
            DELETE_BY_PARAGRAPH_ID,
            mapOf("paragraph_id" to paragraphId)
        )

    fun insert(imageRecord: ImageRecord): ImageRecord =
        namedParameterJdbcOperations.queryForObject(
            INSERT_INTO_IMAGE,
            mapOf(
                "paragraph_id" to imageRecord.paragraphId,
                "image_hash" to imageRecord.imageHash
            ),
            rowMapper
        ) ?: throw IllegalStateException("Failed to insert image record")
}