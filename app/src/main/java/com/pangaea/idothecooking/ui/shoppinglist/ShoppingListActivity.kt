package com.pangaea.idothecooking.ui.shoppinglist

import android.os.Bundle
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.ActivityShoppingListBinding
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingListItem
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shared.MeasuredItemDialog
import com.pangaea.idothecooking.ui.shared.PicklistDlg
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperCallback
import com.pangaea.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.pangaea.idothecooking.ui.shoppinglist.adapters.ShoppingListItemsAdapter
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModelFactory
import com.pangaea.idothecooking.utils.data.IngredientsMigrationTool
import com.pangaea.idothecooking.utils.extensions.observeOnce
import com.pangaea.idothecooking.utils.extensions.setAsDisabled
import com.pangaea.idothecooking.utils.extensions.setAsEnabled
import java.util.function.Consumer

class ShoppingListActivity : AppCompatActivity(), OnStartDragListener {
    private var shoppingListId: Int = -1
    private lateinit var viewModel: ShoppingListViewModel
    private lateinit var shoppingListDetails: ShoppingListDetails
    private var mItemTouchHelper: ItemTouchHelper? = null
    private lateinit var _view: View
    var _itemSave: MenuItem? = null

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

        //var textWatcher: TextWatcher? = null
        viewModel = ShoppingListViewModel((application as IDoTheCookingApp), shoppingListId.toLong())
        viewModel.getDetails()?.observeOnce(this) { shoppingLists ->
            shoppingListDetails = shoppingLists[0]
            title = resources.getString(R.string.title_activity_recipe_name)
                .replace("{0}", shoppingListDetails.shoppingList.name)
            binding.name.setText(shoppingListDetails.shoppingList.name)
            /*textWatcher = */binding.name.doAfterTextChanged() {
                _itemSave?.setAsEnabled()
            }

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
                MeasuredItemDialog(R.string.shopping_list_new_title, null, { obj ->
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.shopping_list_menu, menu)
        val itemCancel = menu.findItem(R.id.item_cancel)
        itemCancel.setOnMenuItemClickListener { menuItem ->
            if (_itemSave?.isEnabled == true) {
                val deleteAlertBuilder = AlertDialog.Builder(this)
                    .setMessage(resources.getString(R.string.exit_with_save))
                    .setCancelable(true)
                    .setNegativeButton(resources.getString(R.string.no)) { dialog, _ -> onBackPressed() }
                    .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                        saveShoppingList() { onBackPressed() }
                    }
                val deleteAlert = deleteAlertBuilder.create()
                deleteAlert.show()
            } else {
                onBackPressed()
            }
            //onBackPressed()
            false
        }

        val itemSave = menu.findItem(R.id.item_save)
        _itemSave = itemSave
        _itemSave?.setAsDisabled()
        itemSave.setOnMenuItemClickListener { menuItem ->
            saveShoppingList() { _itemSave?.setAsDisabled() }
            false
        }

        // Import recipe ingredients
        val importFromRecipe = menu.findItem(R.id.import_from_recipe)
        importFromRecipe.setOnMenuItemClickListener { menuItem ->
            val model = RecipeViewModelFactory(application, null).create(RecipeViewModel::class.java)
            model.getAllRecipes().observeOnce(this) { recipes ->
                PicklistDlg(getString(R.string.export_to_shopping_list),
                            recipes.map() { o ->
                                Pair(o.id.toString(), o.name)
                            }) { recipe: Pair<String, String> ->
                    val list = _view.findViewById<RecyclerView>(R.id.listItemsView)
                    val adapter = list.adapter as ShoppingListItemsAdapter
                    val data = shoppingListDetails.shoppingListItems.toMutableList()
                    IngredientsMigrationTool(application, this, recipe.first.toInt(), "", 1.0,
                                             shoppingListDetails.shoppingList.id).mergeShoppingList(adapter.mItems!!) { items ->
                        //Toast.makeText(baseContext, getString(R.string.success_export_to_shopping_list), Toast.LENGTH_LONG).show()
                        adapter.setItems(items)
                        _itemSave?.setAsEnabled()
                        adapter.notifyDataSetChanged()
                    }
                }.show(this.supportFragmentManager, null)
            }
            false
        }

        return true
    }

    private fun saveShoppingList(callback: Consumer<Long>) {
        shoppingListDetails.shoppingList.name = binding.name.text.toString()
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
            viewModel.update(shoppingListDetails, callback)
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        mItemTouchHelper!!.startDrag(viewHolder!!)
    }

    override fun onItemChanged() {
        _itemSave?.setAsEnabled()
    }

    override fun onItemClicked(index: Int) {
        val list = _view.findViewById<RecyclerView>(R.id.listItemsView)
        val adapter: ShoppingListItemsAdapter = list.adapter as ShoppingListItemsAdapter
        val shoppingListItem: ShoppingListItem? = index.let { adapter.mItems?.get(it) }
        if (shoppingListItem != null) {
            this.let {
                MeasuredItemDialog(R.string.shopping_list_edit_title, shoppingListItem, { obj ->
                    shoppingListItem.amount = obj.amount
                    shoppingListItem.unit = obj.unit
                    shoppingListItem.name = obj.name
                    _itemSave?.setAsEnabled()
                    adapter.notifyDataSetChanged()
                }, { dialog, _ -> dialog.cancel() })
                    .show(supportFragmentManager, null)
            }
        }
    }
}
