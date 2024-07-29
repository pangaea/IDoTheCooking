package com.pangaea.idothecooking.ui.recipe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.state.CategoryRepository
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.entities.Category
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModel
import com.pangaea.idothecooking.ui.category.viewmodels.CategoryViewModelFactory
import com.pangaea.idothecooking.ui.shared.adapters.RecycleViewClickListener
import com.pangaea.idothecooking.ui.recipe.adapters.RecipeRecyclerViewAdapter
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shared.NameOnlyDialog
import com.pangaea.idothecooking.ui.shared.adapters.swipeable.SwipeDeleteHelper


/**
 * A fragment representing a list of Recipes.
 */
class RecipesFragment : Fragment() {

    enum class SortBy {
        Name, CreatedBy, ModifiedBy
    }
    private lateinit var viewModel: RecipeViewModel
    private var sortBy: SortBy = SortBy.ModifiedBy
    private var filterCategories: List<Int> = ArrayList()

    private fun buildList() {
        viewModel = RecipeViewModelFactory(requireActivity().application, null).create(RecipeViewModel::class.java)
        viewModel.getAllRecipesWithDetails().observe(viewLifecycleOwner) { recipes ->
            var filteredList: List<RecipeDetails> = recipes
            if (filterCategories.isNotEmpty()) {
                filteredList = recipes.filter { o ->
                    o.categories.map { n -> n.category_id }
                        .any { n -> filterCategories.contains(n) }
                }
            }

            if (sortBy == SortBy.Name) {
                // Sort by recipe name
                filteredList = filteredList.sortedBy { o -> o.recipe.name }
            } else if (sortBy == SortBy.CreatedBy) {
                // Sort by recipe created
                filteredList = filteredList.sortedBy { o -> o.recipe.createdAt }.asReversed()
            }

            val list = view?.findViewById<RecyclerView>(R.id.list)
            if (list is RecyclerView) {
                with(list) {
                    //addItemDecoration(RecipeItemDecoration(this.context));
                    layoutManager = LinearLayoutManager(context)

                    val listener = object : RecycleViewClickListener() {
                        override fun click(id: Int) {
                            val intent = Intent(activity, RecipeViewActivity::class.java)
                            val b = Bundle()
                            b.putInt("id", id)
                            intent.putExtras(b)
                            startActivity(intent)
                        }
                    }
                    adapter = RecipeRecyclerViewAdapter(filteredList.map{o -> o.recipe}.toMutableList(), listener)
                }

                //list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                list.layoutManager = LinearLayoutManager(context)
                context?.let { ItemTouchHelper(SwipeDeleteHelper(it, list){ position: Int ->
                    //val list = view?.findViewById<RecyclerView>(R.id.list)
                    val adapter = list.adapter as RecipeRecyclerViewAdapter
                    val recipe: Recipe = adapter.getItem(position)
                    viewModel.delete(recipe)
                    adapter.removeAt(position)
                }).attachToRecyclerView(list) }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_list, container, false)
        setHasOptionsMenu(true)
        buildList()

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab?.setOnClickListener {
            NameOnlyDialog(R.string.create_recipe_title, null) { name ->
                val recipe = Recipe()
                recipe.name = name
                recipe.description = ""
                val details = RecipeDetails(recipe, emptyList(), emptyList(), emptyList())
                viewModel.insert(details) { id: Long ->
                    val recipeIntent = Intent(activity, RecipeActivity::class.java)
                    val bundle = Bundle()
                    bundle.putInt("id", id.toInt())
                    recipeIntent.putExtras(bundle)
                    startActivity(recipeIntent)
                }
            }.show(childFragmentManager, null)
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.recipe_filter_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_filter -> {
                val model = CategoryViewModelFactory(requireActivity().application, null).create(
                    CategoryViewModel::class.java)
                model.getAllCategories().observe(viewLifecycleOwner) { categories ->
                    RecipeFilterDialog(categories, filterCategories, sortBy) { categories: List<Category>, sortBy ->
                        this.sortBy = sortBy
                        filterCategories = categories.map{ o -> o.id} as ArrayList<Int>
                        buildList()
                    }.show(childFragmentManager, null)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}