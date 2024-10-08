package com.pangaea.idothecooking.ui.recipe.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentRecipeItemBinding
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.ui.shared.ImageTool
import com.pangaea.idothecooking.ui.shared.adapters.RecycleViewClickListener


/**
 * [RecyclerView.Adapter] that can display a [Recipe].
 */
class RecipeRecyclerViewAdapter(private val values: MutableList<Recipe>,
                                private val listener: RecycleViewClickListener,
                                private val activity: Activity) :
    RecyclerView.Adapter<RecipeRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentRecipeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setBackgroundResource(R.mipmap.paper3)
        val item = values[position]
        if (item.imageUri == null || item.imageUri!!.isEmpty()) {
            holder.imageView.visibility = View.GONE
        } else {
            ImageTool(holder.imageView, activity).display(item.imageUri!!)
//            try {
//                Glide.with(holder.imageView.context)
//                    .load(item.imageUri)
//                    .into(holder.imageView)
//            } catch(_: Exception) {
//            }
        }

        holder.contentView.text = item.name
        holder.descView.text = item.description
        holder.itemView.setOnClickListener { listener.click(item.id) }
    }

    fun getItem(position: Int): Recipe {
        return values[position]
    }

    fun removeAt(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentRecipeItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.recipeImage
        val contentView: TextView = binding.content
        val descView: TextView = binding.description
    }

}