package org.example.notion.paragraph.entity

data class ImageRecord(
    val id: Long,
    val imageHash: String,
    val paragraphId: Long
) {
    class Builder {
        private var id: Long = 0
        private var imageHash: String = ""
        private var paragraphId: Long = 0

        fun id(id: Long) = apply { this.id = id }
        fun imageHash(imageHash: String) = apply { this.imageHash = imageHash }
        fun paragraphId(paragraphId: Long) = apply { this.paragraphId = paragraphId }

        fun build(): ImageRecord {
            return ImageRecord(id, imageHash, paragraphId)
        }
    }
}

