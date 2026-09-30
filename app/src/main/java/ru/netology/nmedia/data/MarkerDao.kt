package ru.netology.nmedia.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(marker: MarkerEntity): Long

    @Update
    suspend fun update(marker: MarkerEntity)

    @Delete
    suspend fun delete(marker: MarkerEntity)

    @Query("SELECT * FROM markers ORDER BY id DESC")
    fun getAll(): Flow<List<MarkerEntity>>

    @Query("SELECT * FROM markers WHERE id = :id")
    suspend fun getById(id: Long): MarkerEntity?
}