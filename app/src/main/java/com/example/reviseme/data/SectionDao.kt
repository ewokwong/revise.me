package com.example.reviseme.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// Section Data Dao
@Dao
interface SectionDao {
    @Insert
    suspend fun insertSection(section: Section)

    @Query("SELECT * FROM sections")
    suspend fun getAllSections(): List<Section>

    @Delete
    suspend fun deleteSection(section: Section)
}