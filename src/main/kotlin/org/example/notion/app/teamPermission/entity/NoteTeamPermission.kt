package org.example.notion.app.userPermission.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table(name = "note_team_permission")
data class NoteTeamPermission(
    @Id
    val permissionId: Long,
    val teamId: Long,
    val noteId: Long,
    val permission: Permission,
)