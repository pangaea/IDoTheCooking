package com.pangaea.idothecooking.ui.shared.adapters.swipeable

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.pangaea.idothecooking.R
import java.util.function.Consumer

class SwipeDeleteHelper(val context: Context, val recyclerView: RecyclerView,
                        val callback: Consumer<Int>?) : SwipeHelper(recyclerView) {

    override fun instantiateUnderlayButton(position: Int): List<UnderlayButton> {
        val deleteButton = deleteButton(position)
        val cancelButton = cancelButton(position)
        return listOf(deleteButton, cancelButton)
    }

    private fun deleteButton(position: Int) : UnderlayButton {
        return context.let {
            UnderlayButton(
                it,
                context.resources.getString(R.string.delete),
                14.0f,
                android.R.color.holo_red_light,
                object : UnderlayButtonClickListener {
                    override fun onClick() {
                        callback?.accept(position)
                    }
                })
        }
    }

    private fun cancelButton(position: Int) : UnderlayButton {
        return context.let {
            UnderlayButton(
                it,
                context.resources.getString(R.string.cancel),
                14.0f,
                android.R.color.holo_green_light,
                object : UnderlayButtonClickListener {
                    override fun onClick() {
                        recyclerView.invalidate()
                    }
                })
        }
    }
}