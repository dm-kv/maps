package ru.netology.nmedia.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.data.MarkerDao
import ru.netology.nmedia.data.MarkerEntity
import ru.netology.nmedia.domain.Marker

class MarkerRepository(private val dao: MarkerDao) {

    val allMarkers: Flow<List<Marker>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun save(marker: Marker) {
        Log.d("MarkerRepo", "[SAVE] marker: id=${marker.id}, title='${marker.title}'")
        val entity = MarkerEntity(
            id = marker.id,
            title = marker.title,
            description = marker.description,
            latitude = marker.latitude,
            longitude = marker.longitude
        )

        if (marker.id == 0L) {
            val newId = dao.insert(entity)
            Log.d("MarkerRepo", "[INSERT] newId=$newId")
        } else {
            dao.update(entity)
            Log.d("MarkerRepo", "[UPDATE] id=${marker.id}")
        }
    }

    suspend fun delete(marker: Marker) = dao.delete(marker.toEntity())

    suspend fun getById(id: Long): Marker? = dao.getById(id)?.toDomain()

    private fun MarkerEntity.toDomain() = Marker(id, title, description, latitude, longitude)
    private fun Marker.toEntity() = MarkerEntity(id, title, description, latitude, longitude)
}