package com.pangaea.idothecooking.ui.home

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.pangaea.idothecooking.IDoTheCookingApp
import com.pangaea.idothecooking.R
import com.pangaea.idothecooking.databinding.FragmentHomeBinding
import com.pangaea.idothecooking.state.db.entities.Recipe
import com.pangaea.idothecooking.state.db.entities.RecipeDetails
import com.pangaea.idothecooking.state.db.entities.ShoppingList
import com.pangaea.idothecooking.state.db.entities.ShoppingListDetails
import com.pangaea.idothecooking.ui.shared.NameOnlyDialog
import com.pangaea.idothecooking.ui.recipe.RecipeActivity
import com.pangaea.idothecooking.ui.recipe.RecipeViewActivity
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModel
import com.pangaea.idothecooking.ui.recipe.viewmodels.RecipeViewModelFactory
import com.pangaea.idothecooking.ui.shared.CreateRecipeDialog
import com.pangaea.idothecooking.ui.shared.ImageTool
import com.pangaea.idothecooking.ui.shared.adapters.CreateRecipeAdapter
import com.pangaea.idothecooking.ui.shoppinglist.ShoppingListActivity
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModel
import com.pangaea.idothecooking.ui.shoppinglist.viewmodels.ShoppingListViewModelFactory
import com.pangaea.idothecooking.utils.data.JsonAsyncImportTool
import com.pangaea.idothecooking.utils.extensions.readJSONFromAssets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return drawHomeScreen()
    }

    private fun drawHomeScreen() : View {
        val root: View = binding.root

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val itemsCount = sharedPreferences.getInt("recent_items_count2", 3)//!!.toInt()

        recipeViewModel = RecipeViewModelFactory(requireActivity().application, null).create(RecipeViewModel::class.java)
        recipeViewModel.getAllRecipesWithDetails().observe(viewLifecycleOwner) { recipesDetails ->
            val linearLayout = root.findViewById<LinearLayout>(R.id.recipeHolder)
            linearLayout.removeAllViews()
            for ((index, recipeDetails: RecipeDetails) in recipesDetails.withIndex()) {
                val recipe = recipeDetails.recipe
                val recipeLayout: View =
                    requireActivity().layoutInflater.inflate(R.layout.home_recent_recipe,
                                                             null,false)!!
                val image = recipeLayout.findViewById<ImageView>(R.id.recipeImage)
                if (recipe.imageUri != null && !recipe.imageUri!!.isEmpty()) {
                    ImageTool(requireActivity()).display(image, recipe.imageUri!!)
                } else {
                    image.visibility = View.GONE
                }

                val content = recipeLayout.findViewById<TextView>(R.id.content)
                content.text = recipe.name
                val description = recipeLayout.findViewById<TextView>(R.id.description)
                if (recipe.description.isEmpty()) {
                    description.text = recipeDetails.ingredients.map { it.name }.joinToString(", ")
                } else {
                    description.text = recipe.description
                }
                recipeLayout.rootView.setOnClickListener{
                    val intent = Intent(activity, RecipeViewActivity::class.java)
                    val b = Bundle()
                    b.putInt("id", recipe.id)
                    intent.putExtras(b)
                    startActivity(intent)
                }
                linearLayout.addView(recipeLayout)
                if (index >= (itemsCount-1)) break;
            }
        }

        shoppingListViewModel = ShoppingListViewModelFactory((activity?.application as IDoTheCookingApp),
                                                             null).create(ShoppingListViewModel::class.java)
        shoppingListViewModel.getAllShoppingListsWithDetails().observe(viewLifecycleOwner) { shoppingLists ->
            val linearLayout = root.findViewById<LinearLayout>(R.id.listsHolder)
            linearLayout.removeAllViews()
            for ((index, shoppingListDetails: ShoppingListDetails) in shoppingLists.withIndex()) {
                val shoppingListLayout: View =
                    requireActivity().layoutInflater.inflate(R.layout.home_recent_shopping_list,
                                                             null,false)!!
                val content = shoppingListLayout.findViewById<TextView>(R.id.content)
                content.text = shoppingListDetails.shoppingList.name

                val description = shoppingListLayout.findViewById<TextView>(R.id.description)
                description.text = shoppingListDetails.shoppingListItems.map { it.name }.joinToString(", ")

                shoppingListLayout.rootView.setOnClickListener{
                    val intent = Intent(activity, ShoppingListActivity::class.java)
                    val b = Bundle()
                    b.putInt("id", shoppingListDetails.shoppingList.id)
                    intent.putExtras(b)
                    startActivity(intent)
                }

                var isComplete = true
                shoppingListDetails.shoppingListItems.forEach { item ->
                    isComplete = isComplete && item.checked
                }

                val image = shoppingListLayout.findViewById<ImageView>(R.id.recipeImage)
                if (isComplete) {
                    image.setImageResource(android.R.drawable.checkbox_on_background)
                } else {
                    image.setImageResource(android.R.drawable.checkbox_off_background)
                }

                linearLayout.addView(shoppingListLayout)
                if (index >= (itemsCount-1)) break;
            }
        }

        binding.createNewRecipe.setOnClickListener(){
            CreateRecipeDialog(CreateRecipeAdapter(this, recipeViewModel)).show(childFragmentManager, null)
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
        drawHomeScreen()
    }
}