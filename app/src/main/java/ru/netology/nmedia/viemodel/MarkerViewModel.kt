package ru.netology.nmedia.viemodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.data.AppDatabase
import ru.netology.nmedia.repository.MarkerRepository
import ru.netology.nmedia.domain.Marker

class MarkerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MarkerRepository(
        AppDatabase.getInstance(application).markerDao()
    )

    val markers: LiveData<List<Marker>> = repository.allMarkers.asLiveData()

    // Для фокуса на маркере при возврате из списка
    private val _focusMarkerId = MutableLiveData<Long?>()
    val focusMarkerId: LiveData<Long?> = _focusMarkerId

    fun save(marker: Marker) {
        viewModelScope.launch {
            Log.d("MarkerVM", "[SAVE] Пытаемся сохранить: id=${marker.id}, title=${marker.title}")
            try {
                repository.save(marker)
                Log.d("MarkerVM", "[SAVE] Сохранено в репозитории. Ждём обновления LiveData...")
            } catch (e: Exception) {
                Log.e("MarkerVM", "[SAVE] Ошибка", e)
            }
        }
    }

    fun delete(marker: Marker) {
        viewModelScope.launch { repository.delete(marker) }
    }

    fun getById(id: Long, onResult: (Marker?) -> Unit) {
        viewModelScope.launch { onResult(repository.getById(id)) }
    }

    fun requestFocus(markerId: Long) {
        _focusMarkerId.value = markerId
    }

    fun focusConsumed() {
        _focusMarkerId.value = null
    }
}