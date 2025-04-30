package com.pangaea.idothecooking.ui.shared.adapters.draggable

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

abstract class DraggableItemsAdapter<T, H: RecyclerView.ViewHolder>(
    checklistItems: MutableList<T>?,
    private val layout: Int,
    private val mDragStartListener: OnStartDragListener
) : RecyclerView.Adapter<H>(), DraggableItemTouchHelperAdapter {
    var mItems: MutableList<T>? = null
    private var autoSelect = false

    init {
        setItems(checklistItems)
    }

    fun setAutoSelect(autoSelect: Boolean) {
        this.autoSelect = autoSelect
    }

    fun setItems(items: MutableList<T>?) {
        mItems = items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
        val view: View =
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return createHolder(view)
    }

    abstract fun createHolder(view: View): H

    override fun onItemDismiss(position: Int) {
        mItems!!.removeAt(position)
        //notifyItemRemoved(position)
        notifyDataSetChanged()
        mDragStartListener.onItemChanged()
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(mItems, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        mDragStartListener.onItemChanged()
        return true
    }

    override fun getItemCount(): Int {
        return mItems!!.size
    }

    fun addNewItem(item: T?) {
        if (item != null) {
            mItems!!.add(item)
        }
        notifyItemInserted(mItems!!.size + 1)
        mDragStartListener.onItemChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun handleDragEvent(holder: RecyclerView.ViewHolder, handle: ImageView) {
        // Start a drag whenever the handle view it touched
        handle.setOnTouchListener { _, event ->
            Log.d(ContentValues.TAG, event.actionMasked.toString())
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                mDragStartListener.onStartDrag(holder)
            }
            false
        }
    }
}