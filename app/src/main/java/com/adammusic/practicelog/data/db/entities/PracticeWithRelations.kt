package com.adammusic.practicelog.data.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class PracticeWithRelations(
    @Embedded val practice: PracticeEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "practiceId"
    )
    val sessions: List<SessionEntity>
)