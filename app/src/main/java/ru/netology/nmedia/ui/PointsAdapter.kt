package ru.netology.nmedia.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.ItemPointBinding
import ru.netology.nmedia.domain.Marker

class PointsAdapter(
    private val onClick: (Marker) -> Unit
) : RecyclerView.Adapter<PointsAdapter.PointViewHolder>() {

    private var items: List<Marker> = emptyList()

    fun submitList(newItems: List<Marker>) {
        Log.d("PointsAdapter", ">>> submitList: received=${newItems.size}")
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        val binding = ItemPointBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PointViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        val item = items[position]
        Log.d("PointsAdapter", ">>> BIND pos=$position, id=${item.id}, title='${item.title}'")
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        Log.d("PointsAdapter", ">>> getItemCount = ${items.size}")
        return items.size
    }

    inner class PointViewHolder(
        private val binding: ItemPointBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(marker: Marker) {
            binding.textTitle.text = marker.title
            binding.textDescription.text = marker.description.ifEmpty {
                "%.5f, %.5f".format(marker.latitude, marker.longitude)
            }
            binding.root.setOnClickListener { onClick(marker) }
        }
    }
}