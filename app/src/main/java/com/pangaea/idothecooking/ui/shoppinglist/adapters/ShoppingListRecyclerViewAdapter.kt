package com.pangaea.idothecooking.ui.shoppinglist.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.pangaea.idothecooking.R

import com.pangaea.idothecooking.databinding.FragmentShoppingListItemBinding
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.ui.shared.adapters.RecycleViewClickListener

class ShoppingListRecyclerViewAdapter(private val values: MutableList<ShoppingListDetails>,
                                      private val listener: RecycleViewClickListener
) : RecyclerView.Adapter<ShoppingListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentShoppingListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val param = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        param.marginStart = 10
        param.marginEnd = 10
        holder.itemView.layoutParams = param
        //holder.itemView.setBackgroundResource(R.mipmap.sticky_note)
        val item = values[position]
        holder.contentView.text = item.shoppingList.name
        holder.contentDesc.text = item.shoppingListItems.map { it.name }.joinToString(", ")

        var isComplete = true
        item.shoppingListItems.forEach { item ->
            isComplete = isComplete && item.checked
        }
        if (isComplete) {
            holder.contentImg.setImageResource(android.R.drawable.checkbox_on_background)
        } else {
            holder.contentImg.setImageResource(android.R.drawable.checkbox_off_background)
        }

        holder.itemView.setOnClickListener { listener.click(item.shoppingList.id) }
    }

    fun getItem(position: Int): ShoppingListDetails {
        return values[position]
    }

    fun removeAt(position: Int) {
        values.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentShoppingListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        //val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content
        val contentDesc: TextView = binding.description
        val contentImg: ImageView = binding.recipeImage3
    }

}