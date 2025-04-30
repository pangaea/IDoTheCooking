package com.pangaea.idothecooking.ui.recipe

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.pangaea.idothecooking.R

internal class RecipeItemDecoration(context: Context?) :
    ItemDecoration() {
    private val drawable: Drawable?

    init {
        drawable = ContextCompat.getDrawable(context!!, R.drawable.line_divider)
    }

//    override fun getItemOffsets(
//        outRect: Rect, view: View,
//        parent: RecyclerView,
//        state: RecyclerView.State
//    ) {
//        with(outRect) {
//            if (parent.getChildAdapterPosition(view) == 0) {
//                top = 10
//            }
//            left = 10
//            right = 10
//            bottom = 10
//        }
//    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + drawable!!.intrinsicHeight
            drawable.setBounds(left, top, right, bottom)
            drawable.draw(canvas)
        }
    }
}