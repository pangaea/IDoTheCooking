package com.lifeoneuropa.idothecooking.ui.recipe

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lifeoneuropa.idothecooking.R
import com.lifeoneuropa.idothecooking.databinding.FragmentRecipeDirectionsBinding
import com.lifeoneuropa.idothecooking.state.db.entities.Direction
import com.lifeoneuropa.idothecooking.ui.recipe.adapters.RecipeDirectionsAdapter
import com.lifeoneuropa.idothecooking.ui.recipe.viewmodels.SelectedRecipeModel
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.lifeoneuropa.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperCallback
import com.lifeoneuropa.idothecooking.utils.extensions.focusAndShowKeyboard

/**
 * A simple [Fragment] subclass.
 * Use the [RecipeDirectionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecipeDirectionsFragment : Fragment(), OnStartDragListener {
    private lateinit var binding: FragmentRecipeDirectionsBinding
    private var mItemTouchHelper: ItemTouchHelper? = null
    private lateinit var _view: View
    private val selectedRecipeModel: SelectedRecipeModel by activityViewModels()
    private var selfUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRecipeDirectionsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recipe_directions, container, false)
        _view = view

        view.setBackgroundResource(R.mipmap.tablecloth3)

        //val list: RecyclerView = binding.listItemsView
        val list = view.findViewById<RecyclerView>(R.id.listItemsView)
        val self = this
        with(list) {
            //setHasFixedSize(true)
            adapter = RecipeDirectionsAdapter(context, mutableListOf(), self)
            layoutManager = LinearLayoutManager(context)
            val callback: ItemTouchHelper.Callback = DraggableItemTouchHelperCallback(adapter as RecipeDirectionsAdapter)
            mItemTouchHelper = ItemTouchHelper(callback)
            mItemTouchHelper!!.attachToRecyclerView(list)
        }

        // Retrieve recipe object from model
        selectedRecipeModel.selectedRecipe.observe(viewLifecycleOwner) { recipe ->
            if (!selfUpdate) {
                val directions = recipe.directions
                val adapter = list.adapter as RecipeDirectionsAdapter
                val data = directions.toMutableList()
                data.sortWith { obj1, obj2 ->
                    Integer.valueOf(obj1.order).compareTo(Integer.valueOf(obj2.order))
                }
                adapter.setItems(data)
                adapter.notifyDataSetChanged()
                adapter.setAutoSelect(true)
            } else {
                selfUpdate = false
            }
        }

        val btn = view.findViewById<FloatingActionButton>(R.id.button_new_item)
        btn.setOnClickListener {
            lanuchEditDialog(null, null){ dialog, id ->
                val details = (dialog as AlertDialog).findViewById<View>(R.id.details) as EditText?
                //val recycler: RecyclerView = view.findViewById(R.id.listItemsView)
                val adapter = list.adapter as RecipeDirectionsAdapter
                val aa = Direction()
                aa.content = details?.text.toString()
                adapter.addNewItem(aa)
                onItemChanged()
            }
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param editMode Render in edit mode
         * @return A new instance of fragment RecipeDirectionsFragment.
         */
        @JvmStatic
        fun newInstance() =
            RecipeDirectionsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        mItemTouchHelper!!.startDrag(viewHolder!!)
    }

    override fun onItemChanged() {
        // Read in list items and reset the positions to match the ui
        val recyclerView: RecyclerView = _view.findViewById(R.id.listItemsView)
        val adapter = recyclerView.adapter as RecipeDirectionsAdapter?

        // Edit the position value of each item
        var i = 0
        val data: List<Direction>? = adapter?.mItems?.map() { it ->
            it.order = i++
            return@map it
        }

        if (data != null) {
            selfUpdate = true
            selectedRecipeModel.updateRecipeDirections(data)
        }
    }

    override fun onItemClicked(index: Int) {
        lanuchEditDialog(null, index){ dialog, id ->
            val list = _view.findViewById<RecyclerView>(R.id.listItemsView)
            val adapter: RecipeDirectionsAdapter = list.adapter as RecipeDirectionsAdapter
            val details = (dialog as AlertDialog).findViewById<View>(R.id.details) as EditText?
            val direction: Direction? = adapter.mItems?.get(index)
            direction?.content = details?.text.toString()
            if (direction?.id!! > 0) {
                // Hack: Since these objects are all created new on save, I can use the id as a status
                direction.id = direction.id.times(-1)
            }
            adapter.notifyDataSetChanged()
            onItemChanged()
        }
    }

    private fun lanuchEditDialog(savedInstanceState: Bundle?, index: Int?, callback: DialogInterface.OnClickListener) {
        val list = _view.findViewById<RecyclerView>(R.id.listItemsView)
        val adapter: RecipeDirectionsAdapter = list.adapter as RecipeDirectionsAdapter
        val direction: Direction? = index?.let { adapter.mItems?.get(it) }
        activity?.let {
            val layout: View =
                requireActivity().layoutInflater.inflate(R.layout.recipe_direction_edit, null, false)!!
            val alertBuilder = AlertDialog.Builder(requireContext())
            alertBuilder.setView(layout)
            val details = layout.findViewById<View>(R.id.details) as EditText?

            alertBuilder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            if (direction != null) {
                alertBuilder.setTitle(resources.getString(R.string.direction_edit_title))
                details?.setText(direction.content)
                alertBuilder.setPositiveButton(resources.getString(R.string.update), callback)
            } else {
                alertBuilder.setTitle(resources.getString(R.string.direction_new_title))
                alertBuilder.setPositiveButton(resources.getString(R.string.add), callback)
            }
            //details?.requestFocus()
            details?.focusAndShowKeyboard()
            alertBuilder.show()
        }
    }
}