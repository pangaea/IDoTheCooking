package com.pangaea.idothecooking.ui.shoppinglist.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.pangaea.idothecooking.R

import com.pangaea.idothecooking.databinding.FragmentShoppingListItemBinding
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.ui.shared.adapters.RecycleViewClickListener

class ShoppingListRecyclerViewAdapter(private val values: MutableList<ShoppingList>,
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
        holder.itemView.setBackgroundResource(R.mipmap.paper2)
        val item = values[position]
        holder.contentView.text = item.name
        holder.itemView.setOnClickListener { listener.click(item.id) }
    }

    fun getItem(position: Int): ShoppingList {
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
    }

}