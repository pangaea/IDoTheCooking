package com.pangaea.idothecooking.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentRecipeDirectionsBinding
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.adapters.RecipeDirectionsAdapter
import com.pangaea.idothecooking.ui.recipe.adapters.RecipeIngredientsAdapter
import com.pangaea.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperCallback

//private const val RECIPE_DIRECTIONS = "recipeDirections"

/**
 * A simple [Fragment] subclass.
 * Use the [RecipeDirectionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecipeDirectionsFragment : Fragment(), OnStartDragListener {
    private lateinit var binding: FragmentRecipeDirectionsBinding
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var callBackListener: RecipeCallBackListener? = null
    private lateinit var _view: View

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

        //val recyclerView: RecyclerView = binding.listItemsView
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

        if (activity is RecipeCallBackListener) callBackListener = activity as RecipeCallBackListener?
        val recipe: RecipeDetails? = callBackListener?.getRecipeDetails()
        recipe?.let {
            val directions = it.directions
            if (directions != null) {
                val adapter = list.adapter as RecipeDirectionsAdapter
                val data = directions.toMutableList()
                data.sortWith(Comparator { obj1, obj2 -> // ## Ascending order
                    Integer.valueOf(obj1.order).compareTo(Integer.valueOf(obj2.order))
                })
                adapter.setItems(data)
                adapter.notifyDataSetChanged()
                adapter.setAutoSelect(true)
            }
        }

        val btn = view.findViewById<Button>(R.id.button_new_item)
        btn.setOnClickListener {
            val recycler: RecyclerView = view.findViewById(R.id.listItemsView)
            val adapter = recycler.adapter as RecipeDirectionsAdapter
            val aa = Direction()
            aa.content = ""
            adapter.addNewItem(aa)
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
        fun newInstance(recipe: RecipeDetails) =
            RecipeDirectionsFragment().apply {
                arguments = Bundle().apply {
                    //putStringArrayList(RECIPE_DIRECTIONS, ArrayList(recipe.directions.map() { it.content}))
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
            callBackListener?.onRecipeDirectionUpdate(data)
        }
    }

    override fun onItemClicked(index: Int) {
        TODO("Not yet implemented")
    }
}