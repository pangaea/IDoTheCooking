package com.lifeoneuropa.idothecooking.ui.shoppinglist

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lifeoneuropa.idothecooking.IDoTheCookingApp
import com.lifeoneuropa.idothecooking.R
import com.lifeoneuropa.idothecooking.state.db.entities.ShoppingList
import com.lifeoneuropa.idothecooking.state.db.entities.ShoppingListDetails
import com.lifeoneuropa.idothecooking.ui.shared.NameOnlyDialog
import com.lifeoneuropa.idothecooking.ui.shared.adapters.RecycleViewClickListener
import com.lifeoneuropa.idothecooking.ui.shared.adapters.swipeable.SwipeDeleteHelper
import com.lifeoneuropa.idothecooking.ui.shoppinglist.adapters.ShoppingListRecyclerViewAdapter
import com.lifeoneuropa.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.lifeoneuropa.idothecooking.utils.extensions.startActivityWithBundle

/**
 * A fragment representing a list of Items.
 */
class ShoppingListsFragment : Fragment() {

    private var columnCount = 1
    private lateinit var viewModel: ShoppingListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list_list, container, false)
        val list = view.findViewById<RecyclerView>(R.id.list)
        if (list is RecyclerView) {
            context?.let { ItemTouchHelper(SwipeDeleteHelper(it, list){ position: Int ->
                val adapter = list.adapter as ShoppingListRecyclerViewAdapter
                val shoppingList: ShoppingList = adapter.getItem(position).shoppingList
                viewModel.delete(shoppingList)
                adapter.removeAt(position)
            }).attachToRecyclerView(list) }

            viewModel = ShoppingListViewModel(activity?.application as IDoTheCookingApp, null)
            viewModel.getAllShoppingListsWithDetails().observe(viewLifecycleOwner) {
                with(list) {
                    layoutManager = when {
                        columnCount <= 1 -> LinearLayoutManager(context)
                        else -> GridLayoutManager(context, columnCount)
                    }

                    val listener = object : RecycleViewClickListener() {
                        override fun click(id: Int) {
                            startActivityWithBundle(ShoppingListActivity::class.java, "id", id)
                        }
                    }
                    adapter = ShoppingListRecyclerViewAdapter(it.toMutableList(), listener)
                }
            }
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab?.setOnClickListener {
            NameOnlyDialog(R.string.create_shopping_list_title, null) { name ->
                val shoppingList = ShoppingList()
                shoppingList.name = name
                shoppingList.description = ""
                val details = ShoppingListDetails(shoppingList, emptyList())
                viewModel.insert(details) { id: Long ->
                    val shoppingListIntent = Intent(activity, ShoppingListActivity::class.java)
                    val bundle = Bundle()
                    bundle.putInt("id", id.toInt())
                    shoppingListIntent.putExtras(bundle)
                    startActivity(shoppingListIntent)
                }
            }.show(childFragmentManager, null)
        }
        return view
    }
}