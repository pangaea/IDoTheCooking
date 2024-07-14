package com.pangaea.idothecooking.ui.shoppinglist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.ActivityShoppingListBinding
import com.pangaea.idothecooking.state.db.entities.Ingredient
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingListItem
import com.pangaea.idothecooking.ui.recipe.RecipeActivity
import com.pangaea.idothecooking.ui.recipe.RecipeIngredientDialog
import com.pangaea.idothecooking.ui.recipe.adapters.RecipeIngredientsAdapter
import com.pangaea.idothecooking.ui.recipe.adapters.RecipePagerAdapter
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperCallback
import com.pangaea.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.pangaea.idothecooking.ui.shoppinglist.adapters.ShoppingListItemsAdapter
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel

class ShoppingListActivity : AppCompatActivity(), OnStartDragListener {
    private var shoppingListId: Int = -1
    private lateinit var viewModel: ShoppingListViewModel
    private lateinit var shoppingListDetails: ShoppingListDetails
    private var mItemTouchHelper: ItemTouchHelper? = null
    private lateinit var _view: View

    private lateinit var binding: ActivityShoppingListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingListBinding.inflate(layoutInflater)
        _view = binding.root
        setContentView(_view)
        setSupportActionBar(binding.toolbar)
        _view.setBackgroundResource(R.mipmap.tablecloth3)

        val list = _view.findViewById<RecyclerView>(R.id.listItemsView)
        list.adapter = ShoppingListItemsAdapter(list.context, mutableListOf(), this)
        list.layoutManager = LinearLayoutManager(list.context)
        val callback: ItemTouchHelper.Callback = DraggableItemTouchHelperCallback(list.adapter as ShoppingListItemsAdapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper!!.attachToRecyclerView(list)

        val bundle = intent?.extras
        if (bundle != null) {
            shoppingListId = bundle.getInt("id", -1)
        }

        viewModel = ShoppingListViewModel((application as IDoTheCookingApp), shoppingListId.toLong())
        viewModel.getDetails()?.observe(this) { shoppingLists ->
            shoppingListDetails = shoppingLists[0]
            title = resources.getString(R.string.title_activity_recipe_name)
                .replace("{0}", shoppingListDetails.shoppingList.name)

            val adapter = list.adapter as ShoppingListItemsAdapter
            val data = shoppingListDetails.shoppingListItems.toMutableList()
            data.sortWith { obj1, obj2 ->
                Integer.valueOf(obj1.order).compareTo(Integer.valueOf(obj2.order))
            }
            adapter.setItems(data)
            adapter.notifyDataSetChanged()
        }

        val btn = _view.findViewById<FloatingActionButton>(R.id.button_new_item)
        btn.setOnClickListener {
            this.let {
                RecipeIngredientDialog(null, { obj ->
                    val adapter = list.adapter as ShoppingListItemsAdapter
                    val shoppingListItem = ShoppingListItem()
                    shoppingListItem.amount = obj.amount
                    shoppingListItem.unit = obj.unit
                    shoppingListItem.name = obj.name
                    adapter.addNewItem(shoppingListItem)
                }, { dialog, _ -> dialog.cancel() })
                    .show(supportFragmentManager, null)
            }

        }
    }

    var _itemSave: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.shopping_list_menu, menu)
        val itemCancel = menu.findItem(R.id.item_cancel)
        itemCancel.setOnMenuItemClickListener { menuItem ->
            onBackPressed()
            false
        }

        val itemSave = menu.findItem(R.id.item_save)
        _itemSave = itemSave
        itemSave?.isVisible = false
        itemSave.setOnMenuItemClickListener { menuItem ->
            val recyclerView: RecyclerView = _view.findViewById(R.id.listItemsView)
            val adapter = recyclerView.adapter as ShoppingListItemsAdapter?

            // Edit the position value of each item
            var i = 0
            val data: List<ShoppingListItem>? = adapter?.mItems?.map() { it ->
                it.order = i++
                return@map it
            }
            if (data != null) {
                shoppingListDetails.shoppingListItems = data
                viewModel.update(shoppingListDetails)
            }
            //_itemSave?.setEnabled(false)
            _itemSave?.isVisible = false
            //viewModel.update(shoppingListDetails){}
//            viewModel.update(recipeDetails) {
//                onBackPressed()
//            }
            //onBackPressed()
            false
        }

        return true
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        mItemTouchHelper!!.startDrag(viewHolder!!)
    }

    override fun onItemChanged() {
        // Read in list items and reset the positions to match the ui
//        val recyclerView: RecyclerView = _view.findViewById(R.id.listItemsView)
//        val adapter = recyclerView.adapter as ShoppingListItemsAdapter?
//
//        // Edit the position value of each item
//        var i = 0
//        val data: List<ShoppingListItem>? = adapter?.mItems?.map() { it ->
//            it.order = i++
//            return@map it
//        }

        //_itemSave?.setEnabled(true)
        _itemSave?.isVisible = true

//        if (data != null) {
//            callBackListener?.onRecipeIngredientUpdate(data)
//        }
    }

    override fun onItemClicked(index: Int) {
        val list = _view.findViewById<RecyclerView>(R.id.listItemsView)
        val adapter: ShoppingListItemsAdapter = list.adapter as ShoppingListItemsAdapter
        val ingredient: ShoppingListItem? = index.let { adapter.mItems?.get(it) }
        if (ingredient != null) {
            this.let {
                RecipeIngredientDialog(ingredient, { obj ->
                    ingredient.amount = obj.amount
                    ingredient.unit = obj.unit
                    ingredient.name = obj.name
                    //(activity as RecipeActivity).dataDirty = true
                    adapter.notifyDataSetChanged()
                }, { dialog, _ -> dialog.cancel() })
                    .show(supportFragmentManager, null)
            }
        }
    }
}
