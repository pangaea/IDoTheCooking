package com.pangaea.idothecooking.ui.category.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pangaea.idothecooking.databinding.FragmentCategoryItemBinding
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.ui.shared.adapters.RecycleViewClickListener

class CategoryRecyclerViewAdapter(private val values: MutableList<Category>,
                                  private val listener: RecycleViewClickListener
) : RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentCategoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.contentView.text = item.name
        holder.itemView.setOnClickListener { listener.click(item.id) }
    }

    fun getItemById(id: Int): Category? {
        values.forEach { category: Category ->
            if (category.id == id) {
                return category
            }
        }
        return null
    }

    fun getItem(position: Int): Category {
        return values[position]
    }

    fun removeAt(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentCategoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.content
    }
}
