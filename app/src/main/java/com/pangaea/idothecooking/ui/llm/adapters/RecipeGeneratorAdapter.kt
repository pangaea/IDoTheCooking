package com.pangaea.idothecooking.ui.llm.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pangaea.idothecooking.databinding.FragmentRecipeGeneratorItemBinding
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.adapters.HelperSuggestion
import com.pangaea.idothecooking.ui.shared.adapters.RecycleViewClickListener

class RecipeGeneratorAdapter(private val values: MutableList<RecipeDetails>,
                                private val listener: RecycleViewClickListener) :
    RecyclerView.Adapter<RecipeGeneratorAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeGeneratorAdapter.ViewHolder {
        return ViewHolder(
            FragmentRecipeGeneratorItemBinding.inflate(
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

    fun getItem(position: Int): RecipeDetails {
        return values[position]
    }

    override fun onBindViewHolder(holder: RecipeGeneratorAdapter.ViewHolder, position: Int) {
        val item = values[position]
        holder.nameView.text = item.recipe.name
        holder.itemView.setOnClickListener { listener.click(position) }
    }

    inner class ViewHolder(binding: FragmentRecipeGeneratorItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val nameView: TextView = binding.name
    }
}