package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.data.MarkerDao
import ru.netology.nmedia.data.MarkerEntity
import ru.netology.nmedia.domain.Marker

class MarkerRepository(private val dao: MarkerDao) {

    val allMarkers: Flow<List<Marker>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun save(marker: Marker) {
        val entity = MarkerEntity(
            id = marker.id, // если 0 — Room сам поставит новый ID при insert
            title = marker.title,
            description = marker.description,
            latitude = marker.latitude,
            longitude = marker.longitude
        )
        if (marker.id == 0L) {
            dao.insert(entity) // автоинкремент даст новый ID
        } else {
            dao.update(entity)
        }
        // Важно: не нужно ничего дополнительно делать — Flow от Room сам обновится.
    }

    suspend fun delete(marker: Marker) = dao.delete(marker.toEntity())

    suspend fun getById(id: Long): Marker? = dao.getById(id)?.toDomain()

    private fun MarkerEntity.toDomain() = Marker(id, title, description, latitude, longitude)
    private fun Marker.toEntity() = MarkerEntity(id, title, description, latitude, longitude)
}