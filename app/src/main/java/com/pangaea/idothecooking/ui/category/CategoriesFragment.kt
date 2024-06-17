package com.pangaea.idothecooking.ui.category

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.CategoryRepository
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.ui.category.adapters.CategoryRecyclerViewAdapter
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModel
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModelFactory
import com.pangaea.idothecooking.ui.recipe.adapters.RecipeRecyclerClickListener
import com.pangaea.idothecooking.ui.shared.adapters.swipeable.SwipeDeleteHelper
import java.util.function.Consumer


class CategoriesFragment : Fragment() {

    private lateinit var viewModel: CategoryViewModel

    private fun launchCreateModal(title: Int, field: Int, content: String, callback: Consumer<String?>) {
        val alertBuilder = AlertDialog.Builder(requireContext())
        alertBuilder.setTitle(resources.getString(title))
        val input = EditText(context)
        input.setText(content)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setHint(field)
        input.setPaddingRelative(60, 20, 60, 20)
//        input.isFocusableInTouchMode = true
//        input.setFocusable(true)
        alertBuilder.setView(input)

        input.requestFocus()
//        val imm = getSystemService(requireContext(), InputMethodManager::class.java)
//        imm!!.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        alertBuilder.setPositiveButton(resources.getString(R.string.save)) { _, _ ->
            callback.accept(input.text.toString()) }
        alertBuilder.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel() }
        alertBuilder.show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_category_list, container, false)
        view.setBackgroundResource(R.mipmap.tablecloth3)
        val db: AppDatabase = (activity?.application as IDoTheCookingApp).getDatabase()
        val categoryRepo = db.categoryDao()?.let { CategoryRepository(it) }
        viewModel = categoryRepo?.let { CategoryViewModelFactory(it, null).create(CategoryViewModel::class.java) }!!
        viewModel.getAllCategories().observe(viewLifecycleOwner) { categories ->
            val list = view.findViewById<RecyclerView>(R.id.list)
            if (list is RecyclerView) {
                with(list) {
                    //addItemDecoration(RecipeItemDecoration(this.context));
                    layoutManager = LinearLayoutManager(context)
                    val listener = object : RecipeRecyclerClickListener() {
                        override fun click(id: Int) {
                            val category: Category? = (adapter as CategoryRecyclerViewAdapter).getItemById(id)
                            if (category != null) {
                                launchCreateModal(
                                    R.string.update_category_title,
                                    R.string.update_category_msg,
                                    category.name
                                ) { name ->
                                    if (name != null) {
                                        category.name = name
                                        viewModel.update(category)
                                    }
                                }
                            }
                        }
                    }
                    adapter = CategoryRecyclerViewAdapter(categories.toMutableList(), listener)

                    // Add line separator
                    list.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                }

                context?.let { ItemTouchHelper(SwipeDeleteHelper(it, list){position: Int ->
                    val adapter = list.adapter as CategoryRecyclerViewAdapter
                    val category: Category = adapter.getItem(position)
                    viewModel.delete(category)
                    adapter.removeAt(position)
                }).attachToRecyclerView(list) }
            }

            val fab = view.findViewById<FloatingActionButton>(R.id.fab)
            fab?.setOnClickListener {
                launchCreateModal(
                    R.string.create_category_title,
                    R.string.create_category_msg,
                    ""
                ) { name ->
                    if (name != null) {
                        val category = Category()
                        category.name = name
                        viewModel.insert(category){}
                    }
                }
            }
        }
        return view
    }
}