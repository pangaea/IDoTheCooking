package com.pangaea.idothecooking.ui.category.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pangaea.idothecooking.databinding.FragmentCategoryBinding
import com.pangaea.idothecooking.databinding.FragmentRecipeBinding
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.ui.recipe.adapters.RecipeRecyclerClickListener
import com.pangaea.idothecooking.ui.recipe.adapters.RecipeRecyclerViewAdapter

class CategoryRecyclerViewAdapter(private val values: MutableList<Category>,
                                  listener: RecipeRecyclerClickListener
) : RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder>() {

    private var listener: RecipeRecyclerClickListener = listener;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
//        if (item.imageUri == null || item.imageUri!!.isEmpty()) {
//            holder.imageView.visibility = View.GONE
//        } else {
//            try {
//                Glide.with(holder.imageView.context)
//                    .load(item.imageUri)
//                    .into(holder.imageView)
//            } catch (_: Exception) {
//            }
//        }

        holder.contentView.text = item.name
        holder.itemView.setOnClickListener { listener.click(item.id) }
        //holder.descView.text = item.description
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

    inner class ViewHolder(binding: FragmentCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        //val imageView: ImageView = binding.recipeImage
        val contentView: TextView = binding.content
        //val descView: TextView = binding.description
    }
}
