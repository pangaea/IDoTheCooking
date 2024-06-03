package com.pangaea.idothecooking.ui.recipe

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
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentRecipeIngredientsBinding
import com.pangaea.idothecooking.state.db.entities.Ingredient
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

        val btn = view.findViewById<Button>(R.id.button_new_item)
        btn.setOnClickListener {
            val recycler: RecyclerView = view.findViewById(R.id.listItemsView)
            val adapter = recycler.adapter as RecipeIngredientsAdapter
            val aa = Ingredient()
            aa.name = ""
            aa.amount = null
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
        lanuchEditDialog(null, index)
    }

    fun lanuchEditDialog(savedInstanceState: Bundle?, index: Int) {
        val list = _view.findViewById<RecyclerView>(R.id.listItemsView)
        val adapter: RecipeIngredientsAdapter = list.adapter as RecipeIngredientsAdapter
        val ingredient: Ingredient? = adapter.mItems?.get(index)
        if (ingredient != null) {
            activity?.let {
                RecipeIngredientDialog(ingredient, { dialog, id ->
                    val wholeNumView = (dialog as AlertDialog).findViewById<View>(R.id.amount_whole) as NumberPicker?
                    var amount: Double = wholeNumView?.value?.toDouble() ?: 0.00
                    val fractionView = (dialog as AlertDialog).findViewById<View>(R.id.amount_fraction) as NumberPicker?
                    val reversedFractionValues = fractionValues.reversedArray()
                    if (fractionView != null && reversedFractionValues.get(fractionView.value) < 1.00) {
                        amount = amount + reversedFractionValues.get(fractionView.value)
                    }
                    ingredient.amount = amount
                    val unitView = (dialog as AlertDialog).findViewById<View>(R.id.unit) as EditText?
                    ingredient.unit = unitView?.text.toString()
                    val nameView = (dialog as AlertDialog).findViewById<View>(R.id.name) as EditText?
                    ingredient.name = nameView?.text.toString()
                    (activity as RecipeActivity).dataDirty = true
                    adapter.notifyDataSetChanged()
                }, { dialog, id ->
                    dialog.cancel()
                }).show(childFragmentManager, null)
            }
        }
    }
}