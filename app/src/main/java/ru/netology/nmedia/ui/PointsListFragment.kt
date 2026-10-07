package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nmedia.databinding.FragmentPointsListBinding
import ru.netology.nmedia.R
import ru.netology.nmedia.viemodel.MarkerViewModel

class PointsListFragment : Fragment() {

    private var _binding: FragmentPointsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MarkerViewModel by activityViewModels()

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
        binding.recyclerPoints.itemAnimator = null

        viewModel.markers.observe(viewLifecycleOwner) { markers ->
            adapter.submitList(markers)
            binding.emptyState.visibility = if (markers.isEmpty()) View.VISIBLE else View.GONE

            if (markers.isNotEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.toast_markers_loaded, markers.size),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), getString(R.string.toast_empty_list), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
