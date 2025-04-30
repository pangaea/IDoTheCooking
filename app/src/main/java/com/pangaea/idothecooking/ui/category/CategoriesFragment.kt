package com.pangaea.idothecooking.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.ui.category.adapters.CategoryRecyclerViewAdapter
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModel
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModelFactory
import com.pangaea.idothecooking.ui.shared.adapters.RecycleViewClickListener
import com.pangaea.idothecooking.ui.shared.NameOnlyDialog
import com.pangaea.idothecooking.ui.shared.adapters.swipeable.SwipeDeleteHelper
import com.pangaea.idothecooking.utils.extensions.observeOnce


class CategoriesFragment : Fragment() {

    private lateinit var viewModel: CategoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_category_list, container, false)
        view.setBackgroundResource(R.mipmap.tablecloth3)
        viewModel = CategoryViewModelFactory(requireActivity().application, null).create(CategoryViewModel::class.java)

        // Create touch listener
        val list = view.findViewById<RecyclerView>(R.id.list)
        ItemTouchHelper(SwipeDeleteHelper(requireContext(), list) { position: Int ->
            val adapter = list.adapter as CategoryRecyclerViewAdapter
            val category: Category = adapter.getItem(position)
            viewModel.getAllLinkedRecipe(category.id).observeOnce(requireActivity()) { links ->
                if (links.isNotEmpty()) {
                    val msg = getString(R.string.category_referenced_warning).replace("{{0}}", links.size.toString())
                    val deleteAlertBuilder = AlertDialog.Builder(requireContext())
                        .setMessage(msg)
                        .setCancelable(true)
                        .setNegativeButton(resources.getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
                        .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                            viewModel.delete(category)
                            adapter.removeAt(position)
                        }
                    val deleteAlert = deleteAlertBuilder.create()
                    deleteAlert.show()
                } else {
                    viewModel.delete(category)
                    adapter.removeAt(position)
                }
            }
        }).attachToRecyclerView(list)

        viewModel.getAllCategories().observe(viewLifecycleOwner) { categories ->
            //val list = view.findViewById<RecyclerView>(R.id.list)
            if (list is RecyclerView) {
                with(list) {
                    //addItemDecoration(RecipeItemDecoration(this.context));
                    layoutManager = LinearLayoutManager(context)
                    val listener = object : RecycleViewClickListener() {
                        override fun click(id: Int) {
                            val category: Category? = (adapter as CategoryRecyclerViewAdapter).getItemById(id)
                            if (category != null) {
                                NameOnlyDialog(R.string.update_category_msg, category.name) { name ->
                                    category.name = name
                                    viewModel.update(category)
                                }.show(childFragmentManager, null)
                            }
                        }
                    }
                    adapter = CategoryRecyclerViewAdapter(categories.toMutableList(), listener)

                    // Add line separator
                    list.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                }
            }
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab?.setOnClickListener {
            NameOnlyDialog(R.string.create_category_msg, null) { name ->
                val category = Category()
                category.name = name
                viewModel.insert(category){}
            }.show(childFragmentManager, null)
        }

        return view
    }
}