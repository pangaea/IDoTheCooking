package com.pangaea.idothecooking.ui.shoppinglist

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
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.ui.shared.NameOnlyDialog
import com.pangaea.idothecooking.ui.shared.adapters.RecycleViewClickListener
import com.pangaea.idothecooking.ui.shared.adapters.swipeable.SwipeDeleteHelper
import com.pangaea.idothecooking.ui.shoppinglist.adapters.ShoppingListRecyclerViewAdapter
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel

/**
 * A fragment representing a list of Items.
 */
class ShoppingListsFragment : Fragment() {

    private var columnCount = 1
    private lateinit var viewModel: ShoppingListViewModel

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        arguments?.let {
//            columnCount = it.getInt(ARG_COLUMN_COUNT)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list_list, container, false)
        viewModel = ShoppingListViewModel(activity?.application as IDoTheCookingApp, null)
        viewModel.getAllShoppingLists().observe(viewLifecycleOwner) {
            val list = view.findViewById<RecyclerView>(R.id.list)
            if (list is RecyclerView) {
                with(list) {
                    layoutManager = when {
                        columnCount <= 1 -> LinearLayoutManager(context)
                        else -> GridLayoutManager(context, columnCount)
                    }

                    val listener = object : RecycleViewClickListener() {
                        override fun click(id: Int) {
    //                            val shoppingList: ShoppingList? = (adapter as ShoppingListRecyclerViewAdapter).getItemById(id)
    //                            assert (shoppingList != null)
                            val shoppingListIntent = Intent(activity, ShoppingListActivity::class.java)
                            val bundle = Bundle()
                            bundle.putInt("id", id.toInt())
                            shoppingListIntent.putExtras(bundle)
                            startActivity(shoppingListIntent)
                        }
                    }
                    adapter = ShoppingListRecyclerViewAdapter(it.toMutableList(), listener)

    //                    adapter = ShoppingListRecyclerViewAdapter(it.toMutableList(), RecipeRecyclerClickListener(){
    //                        override fun click(id: Int) {}
    //
    //                    })
                }
            }
            context?.let { ItemTouchHelper(SwipeDeleteHelper(it, list){ position: Int ->
                val adapter = list.adapter as ShoppingListRecyclerViewAdapter
                val shoppingList: ShoppingList = adapter.getItem(position)
                viewModel.delete(shoppingList)
                adapter.removeAt(position)
            }).attachToRecyclerView(list) }
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