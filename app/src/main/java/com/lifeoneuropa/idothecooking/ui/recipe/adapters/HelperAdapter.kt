package com.lifeoneuropa.idothecooking.ui.recipe.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lifeoneuropa.idothecooking.databinding.HelperSearchFragmentItemBinding
import com.lifeoneuropa.idothecooking.ui.shared.adapters.RecycleViewClickListener
import com.lifeoneuropa.idothecooking.utils.formatting.SuggestionFormatter

class HelperAdapter(private val context: Context, private val values: MutableList<HelperSuggestion>,
                    private val listener: RecycleViewClickListener) :
    RecyclerView.Adapter<HelperAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelperAdapter.ViewHolder {
        return ViewHolder(
            HelperSearchFragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = values.size

    fun removeAt(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getItem(position: Int): HelperSuggestion {
        return values[position]
    }

    override fun onBindViewHolder(holder: HelperAdapter.ViewHolder, position: Int) {
        val item = values[position]
        holder.nameView.text = SuggestionFormatter.formatDisplay(context, item)
        holder.itemView.setOnClickListener { listener.click(position) }
    }

    inner class ViewHolder(binding: HelperSearchFragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameView: TextView = binding.name
    }
}