package ru.netology.nmedia.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.netology.nmedia.data.AppDatabase
import ru.netology.nmedia.databinding.FragmentPointsListBinding
import ru.netology.nmedia.domain.Marker
import ru.netology.nmedia.viemodel.MarkerViewModel

class PointsListFragment : Fragment() {

    private var _binding: FragmentPointsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MarkerViewModel by activityViewModels()

    // Создаём адаптер один раз
    private val adapter = PointsAdapter { marker ->
        viewModel.requestFocus(marker.id)
        findNavController().popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPointsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerPoints.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPoints.adapter = adapter
        // Отключаем анимации, чтобы исключить «пропадание» из-за аниматора
        binding.recyclerPoints.itemAnimator = null
    }

    override fun onResume() {
        super.onResume()
        loadMarkers()
    }

    private fun loadMarkers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dao = AppDatabase.getInstance(requireContext()).markerDao()
                val entities = dao.getAll().first()
                val markers = entities.map {
                    Marker(it.id, it.title, it.description, it.latitude, it.longitude)
                }

                Log.d("PointsList", ">>> DIRECT: size=${markers.size}, ids=${markers.map { it.id }}")

                if (markers.isNotEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Загружено точек: ${markers.size}\nПервая: ${markers[0].title}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(requireContext(), "Список пуст", Toast.LENGTH_SHORT).show()
                }

                adapter.submitList(markers)
                binding.recyclerPoints.scrollToPosition(0)
                binding.emptyState.visibility = if (markers.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Log.e("PointsList", ">>> ERROR loading markers", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
