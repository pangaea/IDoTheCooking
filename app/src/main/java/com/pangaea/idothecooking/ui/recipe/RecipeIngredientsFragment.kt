package com.pangaea.idothecooking.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentRecipeIngredientsBinding
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.recipe.adapters.RecipeIngredientsAdapter
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.SelectedRecipeModel
import com.pangaea.idothecooking.ui.shared.MeasuredItemDialog
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperCallback
import com.pangaea.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.pangaea.idothecooking.utils.extensions.observeOnce


/**
 * A simple [Fragment] subclass.
 * Use the [RecipeIngredientsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecipeIngredientsFragment : Fragment(), OnStartDragListener {
    private lateinit var binding: FragmentRecipeIngredientsBinding
    private var mItemTouchHelper: ItemTouchHelper? = null
    private lateinit var _view: View
    private val selectedRecipeModel: SelectedRecipeModel by activityViewModels()
    private var selfUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRecipeIngredientsBinding.inflate(layoutInflater)
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recipe_ingredients, container, false)
        _view = view

        view.setBackgroundResource(R.mipmap.tablecloth3)

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

        // Retrieve recipe object from model
        selectedRecipeModel.selectedRecipe.observe(viewLifecycleOwner) { recipe ->
            if (!selfUpdate) {
                val ingredients = recipe.ingredients
                val adapter = list.adapter as RecipeIngredientsAdapter
                val data = ingredients.toMutableList()
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
            activity?.let {
                MeasuredItemDialog(R.string.ingredient_new_title, null, { obj ->
                    val adapter = list.adapter as RecipeIngredientsAdapter
                    val ingredient = Ingredient()
                    ingredient.amount = obj.amount
                    ingredient.unit = obj.unit
                    ingredient.name = obj.name
                    adapter.addNewItem(ingredient)
                    onItemChanged()
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
        fun newInstance() =
            RecipeIngredientsFragment().apply {
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
        val adapter = recyclerView.adapter as RecipeIngredientsAdapter?

        // Edit the position value of each item
        var i = 0
        val data: List<Ingredient>? = adapter?.mItems?.map() { it ->
            it.order = i++
            return@map it
        }

        if (data != null) {
            //callBackListener?.onRecipeIngredientUpdate(data)
            selfUpdate = true
            selectedRecipeModel.updateRecipeIngredients(data)
        }
    }

    override fun onItemClicked(index: Int) {
        val list = _view.findViewById<RecyclerView>(R.id.listItemsView)
        val adapter: RecipeIngredientsAdapter = list.adapter as RecipeIngredientsAdapter
        val ingredient: Ingredient? = index.let { adapter.mItems?.get(it) }
        if (ingredient != null) {
            activity?.let {
                MeasuredItemDialog(R.string.ingredient_edit_title, ingredient, { obj ->
                    ingredient.amount = obj.amount
                    ingredient.unit = obj.unit
                    ingredient.name = obj.name
                    if (ingredient.id > 0) {
                        // Hack: Since these objects are all created new on save, I can use the id as a status
                        ingredient.id = ingredient.id.times(-1)
                    }
                    adapter.notifyDataSetChanged()
                    onItemChanged()
                }, { dialog, _ -> dialog.cancel() })
                    .show(childFragmentManager, null)
            }
        }
    }
}