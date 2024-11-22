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
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.shared.ImageTool
import com.pangaea.idothecooking.ui.shared.adapters.RecycleViewClickListener


/**
 * [RecyclerView.Adapter] that can display a [Recipe].
 */
class RecipeRecyclerViewAdapter(private val values: MutableList<RecipeDetails>,
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
        //holder.itemView.setBackgroundResource(R.mipmap.paper3)
//        val param = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
//        param.marginEnd = 10
//        holder.itemView.layoutParams = param
        val item = values[position]
        if (item.recipe.imageUri == null || item.recipe.imageUri!!.isEmpty()) {
            holder.imageView.visibility = View.GONE
        } else {
            ImageTool(activity).display(holder.imageView, item.recipe.imageUri!!)
//            try {
//                Glide.with(holder.imageView.context)
//                    .load(item.imageUri)
//                    .into(holder.imageView)
//            } catch(_: Exception) {
//            }
        }

        holder.contentView.text = item.recipe.name
        holder.descView.text = item.recipe.description
        if (item.recipe.description.isEmpty()) {
            holder.descView.text = item.ingredients.map { it.name }.joinToString(", ")
        } else {
            holder.descView.text = item.recipe.description
        }
        holder.itemView.setOnClickListener { listener.click(item.recipe.id) }
    }

    fun getItem(position: Int): RecipeDetails {
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