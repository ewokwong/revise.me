package com.example.reviseme.data

import Topic
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

// Section Data Class
@Entity(tableName = "sections")
data class Section(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

data class SectionWithTopics(
    @Embedded val section: Section,
    @Relation(
        parentColumn = "id",
        entityColumn = "sectionId"
    )
    val topics: List<Topic>
)