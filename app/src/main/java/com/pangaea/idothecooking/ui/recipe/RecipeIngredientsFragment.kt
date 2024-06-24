package com.pangaea.idothecooking.ui.recipe

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentRecipeIngredientsBinding
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Direction
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.adapters.RecipeIngredientsAdapter
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperCallback
import com.pangaea.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.pangaea.idothecooking.utils.extensions.fractionValues


/**
 * A simple [Fragment] subclass.
 * Use the [RecipeIngredientsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecipeIngredientsFragment : Fragment(), OnStartDragListener {
    private lateinit var binding: FragmentRecipeIngredientsBinding
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var callBackListener: RecipeCallBackListener? = null
    private lateinit var _view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRecipeIngredientsBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recipe_ingredients, container, false)
        _view = view

        view.setBackgroundResource(R.mipmap.tablecloth3)

        //val list: RecyclerView = binding.listItemsView
        val list = view.findViewById<RecyclerView>(R.id.listItemsView)
        val self = this
        with(list) {
            //setHasFixedSize(true)
            adapter = RecipeIngredientsAdapter(context, mutableListOf(), self)
            layoutManager = LinearLayoutManager(context)
            val callback: ItemTouchHelper.Callback = DraggableItemTouchHelperCallback(adapter as RecipeIngredientsAdapter)
            mItemTouchHelper = ItemTouchHelper(callback)
            mItemTouchHelper!!.attachToRecyclerView(list)
        }

        if (activity is RecipeCallBackListener) callBackListener = activity as RecipeCallBackListener?
        val recipe: RecipeDetails? = callBackListener?.getRecipeDetails()
        recipe?.let {
            val ingredients = it.ingredients
            if (ingredients != null) {
                val adapter = list.adapter as RecipeIngredientsAdapter
                val data = ingredients.toMutableList()
                data.sortWith(Comparator { obj1, obj2 -> // ## Ascending order
                    Integer.valueOf(obj1.order).compareTo(Integer.valueOf(obj2.order))
                })
                adapter.setItems(data)
                adapter.notifyDataSetChanged()
                adapter.setAutoSelect(true)
            }
        }

        val btn = view.findViewById<FloatingActionButton>(R.id.button_new_item)
        btn.setOnClickListener {
            activity?.let {
                    RecipeIngredientDialog(null, { obj ->
                    val recycler: RecyclerView = view.findViewById(R.id.listItemsView)
                    val adapter = recycler.adapter as RecipeIngredientsAdapter
                    adapter.addNewItem(obj)
                }, { dialog, _ -> dialog.cancel() })
                    .show(childFragmentManager, null)
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
        fun newInstance(recipe: RecipeDetails) =
            RecipeIngredientsFragment().apply {
                arguments = Bundle().apply {
                    //putStringArrayList(RECIPE_INGREDIENTS, ArrayList(recipe.ingredients.map() { it.name}))
                }
            }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        mItemTouchHelper!!.startDrag(viewHolder!!)
    }

    override fun onItemChanged() {
        // Read in list items and reset the positions to match the ui
        val recyclerView: RecyclerView = _view.findViewById(R.id.listItemsView)
        val adapter = recyclerView.adapter as RecipeIngredientsAdapter?

        // Edit the position value of each item
        var i = 0
        val data: List<Ingredient>? = adapter?.mItems?.map() { it ->
            it.order = i++
            return@map it
        }

        if (data != null) {
            callBackListener?.onRecipeIngredientUpdate(data)
        }
    }

    override fun onItemClicked(index: Int) {
        val list = _view.findViewById<RecyclerView>(R.id.listItemsView)
        val adapter: RecipeIngredientsAdapter = list.adapter as RecipeIngredientsAdapter
        val ingredient: Ingredient? = index.let { adapter.mItems?.get(it) }
        if (ingredient != null) {
            activity?.let {
                RecipeIngredientDialog(ingredient, { obj ->
                    ingredient.amount = obj.amount
                    ingredient.unit = obj.unit
                    ingredient.name = obj.name
                    (activity as RecipeActivity).dataDirty = true
                    adapter.notifyDataSetChanged()
                }, { dialog, _ -> dialog.cancel() })
                    .show(childFragmentManager, null)
            }
        }
    }
}