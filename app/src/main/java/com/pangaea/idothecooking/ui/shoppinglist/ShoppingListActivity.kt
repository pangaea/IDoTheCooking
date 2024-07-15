package com.pangaea.idothecooking.ui.shoppinglist

import android.os.Bundle
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.pangaea.idothecooking.ui.shared.MeasuredItemDialog
import com.pangaea.idothecooking.ui.shared.adapters.draggable.DraggableItemTouchHelperCallback
import com.pangaea.idothecooking.ui.shared.adapters.draggable.OnStartDragListener
import com.pangaea.idothecooking.ui.shoppinglist.adapters.ShoppingListItemsAdapter
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.utils.extensions.observeOnce
import com.pangaea.idothecooking.utils.extensions.setAsDisabled
import com.pangaea.idothecooking.utils.extensions.setAsEnabled

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

//            if (textWatcher != null) {
//                binding.name.removeTextChangedListener(textWatcher)
//            }
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
                MeasuredItemDialog(null, { obj ->
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
                deleteAlertBuilder.setMessage(resources.getString(R.string.exit_without_save))
                deleteAlertBuilder.setCancelable(true)
                deleteAlertBuilder.setPositiveButton(resources.getString(R.string.yes)) { _, _ -> onBackPressed() }
                deleteAlertBuilder.setNegativeButton(resources.getString(R.string.no)) { dialog, _ -> dialog.cancel() }
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
                viewModel.update(shoppingListDetails)
            }

            _itemSave?.setAsDisabled()
            false
        }

        return true
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
        val ingredient: ShoppingListItem? = index.let { adapter.mItems?.get(it) }
        if (ingredient != null) {
            this.let {
                MeasuredItemDialog(ingredient, { obj ->
                    ingredient.amount = obj.amount
                    ingredient.unit = obj.unit
                    ingredient.name = obj.name
                    _itemSave?.setAsEnabled()
                    adapter.notifyDataSetChanged()
                }, { dialog, _ -> dialog.cancel() })
                    .show(supportFragmentManager, null)
            }
        }
    }
}
