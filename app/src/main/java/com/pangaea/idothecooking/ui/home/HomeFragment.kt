package com.pangaea.idothecooking.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentHomeBinding
import com.pangaea.idothecooking.state.RecipeRepository
import com.pangaea.idothecooking.state.db.AppDatabase
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.ui.shared.NameOnlyDialog
import com.pangaea.idothecooking.ui.recipe.RecipeActivity
import com.pangaea.idothecooking.ui.recipe.RecipeViewActivity
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shoppinglist.ShoppingListActivity
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var shoppingListViewModel: ShoppingListViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recipeViewModel = RecipeViewModelFactory(requireActivity().application, null).create(RecipeViewModel::class.java)
        recipeViewModel.getAllRecipes().observe(viewLifecycleOwner) { recipes ->
            val linearLayout = root.findViewById<LinearLayout>(R.id.recipeHolder)
            linearLayout.removeAllViews()
            for ((index, recipe: Recipe) in recipes.withIndex()) {
                val recipeLayout: View =
                    requireActivity().layoutInflater.inflate(R.layout.home_recent_recipe,
                                                             null,false)!!
                val image = recipeLayout.findViewById<ImageView>(R.id.recipeImage)
                if (recipe.imageUri != null && !recipe.imageUri!!.isEmpty()) {
                    try {
                        Glide.with(requireActivity().baseContext)
                            .load(recipe.imageUri)
                            .into(image)
                    } catch(_: Exception) {}
                } else {
                    image.visibility = View.GONE
                }

                val content = recipeLayout.findViewById<TextView>(R.id.content)
                content.text = recipe.name
                val description = recipeLayout.findViewById<TextView>(R.id.description)
                description.text = recipe.description
                recipeLayout.rootView.setOnClickListener{
                    val intent = Intent(activity, RecipeViewActivity::class.java)
                    val b = Bundle()
                    b.putInt("id", recipe.id)
                    intent.putExtras(b)
                    startActivity(intent)
                }
                linearLayout.addView(recipeLayout)
                if (index >= 2) break;
            }
        }

        shoppingListViewModel = ShoppingListViewModelFactory((activity?.application as IDoTheCookingApp),
                                                             null).create(ShoppingListViewModel::class.java)
        shoppingListViewModel.getAllShoppingLists().observe(viewLifecycleOwner) { shoppingLists ->
            val linearLayout = root.findViewById<LinearLayout>(R.id.listsHolder)
            linearLayout.removeAllViews()
            for ((index, shoppingList: ShoppingList) in shoppingLists.withIndex()) {
                val shoppingListLayout: View =
                    requireActivity().layoutInflater.inflate(R.layout.home_recent_shopping_list,
                                                             null,false)!!
                val content = shoppingListLayout.findViewById<TextView>(R.id.content)
                content.text = shoppingList.name
                shoppingListLayout.rootView.setOnClickListener{
                    val intent = Intent(activity, ShoppingListActivity::class.java)
                    val b = Bundle()
                    b.putInt("id", shoppingList.id)
                    intent.putExtras(b)
                    startActivity(intent)
                }
                linearLayout.addView(shoppingListLayout)
                if (index >= 2) break;
            }
        }

        binding.createNewRecipe.setOnClickListener(){
            NameOnlyDialog(R.string.create_recipe_title, null) { name ->
                val recipe = Recipe()
                recipe.name = name
                recipe.description = ""
                val details = RecipeDetails(recipe, emptyList(), emptyList(), emptyList())
                recipeViewModel.insert(details) { id: Long ->
                    val recipeIntent = Intent(activity, RecipeActivity::class.java)
                    val bundle = Bundle()
                    bundle.putInt("id", id.toInt())
                    recipeIntent.putExtras(bundle)
                    startActivity(recipeIntent)
                }
            }.show(childFragmentManager, null)
        }

        binding.createNewList.setOnClickListener(){
            NameOnlyDialog(R.string.create_shopping_list_title, null) { name ->
                val shoppingList = ShoppingList()
                shoppingList.name = name
                shoppingList.description = ""
                val details = ShoppingListDetails(shoppingList, emptyList())
                shoppingListViewModel.insert(details) { id: Long ->
                    val shoppingListIntent = Intent(activity, ShoppingListActivity::class.java)
                    val bundle = Bundle()
                    bundle.putInt("id", id.toInt())
                    shoppingListIntent.putExtras(bundle)
                    startActivity(shoppingListIntent)
                }
            }.show(childFragmentManager, null)
        }

        binding.viewRecipes.setOnClickListener(){
            activity?.findNavController(R.id.nav_host_fragment_content_main)?.navigate(R.id.nav_recipes)
        }
        binding.viewShoppingLists.setOnClickListener(){
            activity?.findNavController(R.id.nav_host_fragment_content_main)?.navigate(R.id.nav_shopping_lists)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
    }
}