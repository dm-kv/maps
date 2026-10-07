package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ru.netology.nmedia.databinding.FragmentEditPointBinding
import ru.netology.nmedia.domain.Marker
import ru.netology.nmedia.viemodel.MarkerViewModel
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.domain.MarkerArgs

class EditPointFragment : Fragment() {

    private var _binding: FragmentEditPointBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MarkerViewModel by activityViewModels()

    private var markerId: Long = -1L
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var existingMarker: Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPointBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        markerId = arguments?.getLong(MarkerArgs.MARKER_ID, -1L) ?: -1L
        latitude = arguments?.getDouble(MarkerArgs.LATITUDE, 0.0) ?: 0.0
        longitude = arguments?.getDouble(MarkerArgs.LONGITUDE, 0.0) ?: 0.0

        if (markerId > 0) {
            viewModel.getById(markerId) { marker ->
                existingMarker = marker
                marker?.let {
                    binding.editTitle.setText(it.title)
                    binding.editDescription.setText(it.description)
                    latitude = it.latitude
                    longitude = it.longitude
                    binding.textCoordinates.text = "%.5f, %.5f".format(it.latitude, it.longitude)
                    binding.buttonDelete.visibility = View.VISIBLE
                }
            }
        } else {
            binding.textCoordinates.text = "%.5f, %.5f".format(latitude, longitude)
            binding.buttonDelete.visibility = View.GONE
        }

        binding.buttonSave.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val description = binding.editDescription.text.toString().trim()

            val marker = Marker(
                id = if (markerId > 0) markerId else 0,
                title = if (title.isEmpty()) requireContext().getString(R.string.marker_no_title) else title,
                description = description,
                latitude = latitude,
                longitude = longitude
            )

            Toast.makeText(requireContext(), "save: id=${marker.id}, title=${marker.title}", Toast.LENGTH_LONG).show()
            viewModel.save(marker)
            findNavController().popBackStack()
        }

        binding.buttonDelete.setOnClickListener {
            existingMarker?.let { viewModel.delete(it) }
            findNavController().popBackStack()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}